package com.sanrotech.pos;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BluetoothPrinterBridge {
    public interface EventSink {
        void emit(String json);
    }

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String PREFS_NAME = "sanro_native_printer";
    private static final String PREF_SELECTED_ADDRESS = "selected_bluetooth_printer_address";
    private static final int MAX_RAW_PRINT_BYTES = 512 * 1024;
    private static final int MAX_TEXT_CHARS = 32768;

    private final Activity activity;
    private final int permissionRequestCode;
    private final EventSink eventSink;
    private final SharedPreferences preferences;
    private final ExecutorService printerExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean permissionRequestInFlight = new AtomicBoolean(false);
    private final BluetoothAdapter bluetoothAdapter;
    private final String bridgeToken = UUID.randomUUID().toString();

    public BluetoothPrinterBridge(Activity activity, int permissionRequestCode, EventSink eventSink) {
        this.activity = activity;
        this.permissionRequestCode = permissionRequestCode;
        this.eventSink = eventSink;
        this.preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        BluetoothManager manager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = manager == null ? null : manager.getAdapter();
    }

    String getBridgeToken() {
        return bridgeToken;
    }

    @JavascriptInterface
    public String getCapabilities(String token) {
        if (!authorized(token)) return unauthorized("getCapabilities").toString();
        JSONObject result = baseResult(true);
        try {
            result.put("bridgeVersion", "1.0.6");
            result.put("bluetoothClassic", bluetoothAdapter != null);
            result.put("pairedDeviceListing", true);
            result.put("persistentSelection", true);
            result.put("testPrint", true);
            result.put("rawEscPosBase64", true);
            result.put("plainTextPrint", true);
            result.put("permissionGranted", hasConnectPermission());
            result.put("eventName", "sanro-native-printer");
        } catch (JSONException ignored) {}
        return result.toString();
    }

    @JavascriptInterface
    public String listPairedPrinters(String token) {
        if (!authorized(token)) return unauthorized("listPairedPrinters").toString();
        String readinessError = readinessError(true);
        if (readinessError != null) return errorResult("listPairedPrinters", readinessError).toString();

        JSONArray printers = new JSONArray();
        String selectedAddress = preferences.getString(PREF_SELECTED_ADDRESS, null);
        try {
            for (BluetoothDevice device : bondedDevices()) {
                printers.put(deviceJson(device, device.getAddress().equalsIgnoreCase(selectedAddress)));
            }
            JSONObject result = baseResult(true);
            result.put("action", "listPairedPrinters");
            result.put("printers", printers);
            result.put("selectedAddress", selectedAddress == null ? JSONObject.NULL : selectedAddress);
            return result.toString();
        } catch (Exception e) {
            return errorResult("listPairedPrinters", "BLUETOOTH_READ_FAILED").toString();
        }
    }

    @JavascriptInterface
    public String getSelectedPrinter(String token) {
        if (!authorized(token)) return unauthorized("getSelectedPrinter").toString();
        String readinessError = readinessError(true);
        if (readinessError != null) return errorResult("getSelectedPrinter", readinessError).toString();
        String address = preferences.getString(PREF_SELECTED_ADDRESS, null);
        if (address == null || address.isEmpty()) return errorResult("getSelectedPrinter", "NO_PRINTER_SELECTED").toString();
        BluetoothDevice device = findBondedDevice(address);
        if (device == null) return errorResult("getSelectedPrinter", "SELECTED_PRINTER_NOT_PAIRED").toString();
        try {
            JSONObject result = baseResult(true);
            result.put("action", "getSelectedPrinter");
            result.put("printer", deviceJson(device, true));
            return result.toString();
        } catch (JSONException e) {
            return errorResult("getSelectedPrinter", "BLUETOOTH_READ_FAILED").toString();
        }
    }

    @JavascriptInterface
    public String selectPrinter(String token, String address) {
        if (!authorized(token)) return unauthorized("selectPrinter").toString();
        String readinessError = readinessError(true);
        if (readinessError != null) return errorResult("selectPrinter", readinessError).toString();
        String normalized = normalizeAddress(address);
        if (normalized == null) return errorResult("selectPrinter", "INVALID_PRINTER_ADDRESS").toString();
        BluetoothDevice device = findBondedDevice(normalized);
        if (device == null) return errorResult("selectPrinter", "PRINTER_NOT_PAIRED").toString();
        preferences.edit().putString(PREF_SELECTED_ADDRESS, normalized).apply();
        try {
            JSONObject result = baseResult(true);
            result.put("action", "selectPrinter");
            result.put("printer", deviceJson(device, true));
            emit(result);
            return result.toString();
        } catch (JSONException e) {
            return errorResult("selectPrinter", "BLUETOOTH_READ_FAILED").toString();
        }
    }

    @JavascriptInterface
    public String clearSelectedPrinter(String token) {
        if (!authorized(token)) return unauthorized("clearSelectedPrinter").toString();
        preferences.edit().remove(PREF_SELECTED_ADDRESS).apply();
        JSONObject result = baseResult(true);
        try { result.put("action", "clearSelectedPrinter"); } catch (JSONException ignored) {}
        emit(result);
        return result.toString();
    }

    @JavascriptInterface
    public String requestBluetoothPermission(String token) {
        if (!authorized(token)) return unauthorized("requestBluetoothPermission").toString();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || hasConnectPermission()) {
            JSONObject result = baseResult(true);
            try {
                result.put("action", "requestBluetoothPermission");
                result.put("permissionGranted", true);
            } catch (JSONException ignored) {}
            return result.toString();
        }
        if (permissionRequestInFlight.compareAndSet(false, true)) {
            activity.runOnUiThread(() -> activity.requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, permissionRequestCode));
        }
        JSONObject result = baseResult(false);
        try {
            result.put("action", "requestBluetoothPermission");
            result.put("error", "BLUETOOTH_PERMISSION_REQUIRED");
            result.put("permissionRequested", true);
        } catch (JSONException ignored) {}
        return result.toString();
    }

    @JavascriptInterface
    public void testPrint(String token) {
        if (!authorized(token)) return;
        enqueuePrint("testPrint", buildTestPrintPayload());
    }

    @JavascriptInterface
    public void printText(String token, String text) {
        if (!authorized(token)) return;
        if (text == null || text.isEmpty() || text.length() > MAX_TEXT_CHARS) {
            emit(errorResult("printText", "INVALID_PRINT_TEXT"));
            return;
        }
        enqueuePrint("printText", buildPlainTextPayload(text));
    }

    @JavascriptInterface
    public void printEscPosBase64(String token, String base64Payload) {
        if (!authorized(token)) return;
        if (base64Payload == null || base64Payload.isEmpty()) {
            emit(errorResult("printEscPosBase64", "INVALID_PRINT_PAYLOAD"));
            return;
        }
        try {
            byte[] payload = Base64.decode(base64Payload, Base64.DEFAULT);
            if (payload.length == 0 || payload.length > MAX_RAW_PRINT_BYTES) {
                emit(errorResult("printEscPosBase64", "INVALID_PRINT_PAYLOAD"));
                return;
            }
            enqueuePrint("printEscPosBase64", payload);
        } catch (IllegalArgumentException e) {
            emit(errorResult("printEscPosBase64", "INVALID_PRINT_PAYLOAD"));
        }
    }

    public void onPermissionResult(boolean granted) {
        permissionRequestInFlight.set(false);
        JSONObject result = baseResult(granted);
        try {
            result.put("action", "bluetoothPermissionResult");
            result.put("permissionGranted", granted);
            if (!granted) result.put("error", "BLUETOOTH_PERMISSION_DENIED");
        } catch (JSONException ignored) {}
        emit(result);
    }

    public void shutdown() {
        printerExecutor.shutdownNow();
    }

    private boolean authorized(String token) {
        if (token == null) return false;
        byte[] expected = bridgeToken.getBytes(StandardCharsets.US_ASCII);
        byte[] supplied = token.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, supplied);
    }

    private JSONObject unauthorized(String action) {
        return errorResult(action, "BRIDGE_UNAUTHORIZED");
    }

    private void enqueuePrint(String action, byte[] payload) {
        String readinessError = readinessError(true);
        if (readinessError != null) {
            if ("BLUETOOTH_PERMISSION_REQUIRED".equals(readinessError)) requestBluetoothPermission(bridgeToken);
            emit(errorResult(action, readinessError));
            return;
        }
        String address = preferences.getString(PREF_SELECTED_ADDRESS, null);
        if (address == null || address.isEmpty()) {
            emit(errorResult(action, "NO_PRINTER_SELECTED"));
            return;
        }
        BluetoothDevice device = findBondedDevice(address);
        if (device == null) {
            emit(errorResult(action, "SELECTED_PRINTER_NOT_PAIRED"));
            return;
        }
        printerExecutor.execute(() -> {
            try {
                writeToPrinter(device, payload);
                JSONObject result = baseResult(true);
                result.put("action", action);
                result.put("printer", deviceJson(device, true));
                emit(result);
            } catch (Exception e) {
                emit(errorResult(action, "PRINTER_CONNECTION_FAILED"));
            }
        });
    }

    private void writeToPrinter(BluetoothDevice device, byte[] payload) throws IOException {
        BluetoothSocket socket = null;
        IOException firstFailure = null;
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
        } catch (IOException e) {
            firstFailure = e;
            closeQuietly(socket);
            socket = null;
        }
        if (socket == null || !socket.isConnected()) {
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                socket.connect();
            } catch (IOException secondFailure) {
                closeQuietly(socket);
                if (firstFailure != null) secondFailure.addSuppressed(firstFailure);
                throw secondFailure;
            }
        }
        try (BluetoothSocket activeSocket = socket; OutputStream output = activeSocket.getOutputStream()) {
            output.write(payload);
            output.flush();
        }
    }

    private List<BluetoothDevice> bondedDevices() {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> result = devices == null ? new ArrayList<>() : new ArrayList<>(devices);
        result.sort(Comparator.comparing(this::safeDeviceName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(BluetoothDevice::getAddress));
        return result;
    }

    private BluetoothDevice findBondedDevice(String address) {
        if (address == null) return null;
        for (BluetoothDevice device : bondedDevices()) {
            if (address.equalsIgnoreCase(device.getAddress())) return device;
        }
        return null;
    }

    private JSONObject deviceJson(BluetoothDevice device, boolean selected) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", safeDeviceName(device));
        json.put("address", device.getAddress());
        json.put("selected", selected);
        return json;
    }

    private String safeDeviceName(BluetoothDevice device) {
        try {
            String name = device.getName();
            return name == null || name.trim().isEmpty() ? "Bluetooth Printer" : name.trim();
        } catch (SecurityException e) {
            return "Bluetooth Printer";
        }
    }

    private String readinessError(boolean requireEnabled) {
        if (bluetoothAdapter == null) return "BLUETOOTH_NOT_SUPPORTED";
        if (!hasConnectPermission()) return "BLUETOOTH_PERMISSION_REQUIRED";
        try {
            if (requireEnabled && !bluetoothAdapter.isEnabled()) return "BLUETOOTH_DISABLED";
        } catch (SecurityException e) {
            return "BLUETOOTH_PERMISSION_REQUIRED";
        }
        return null;
    }

    private boolean hasConnectPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || activity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private String normalizeAddress(String address) {
        if (address == null) return null;
        String normalized = address.trim().toUpperCase();
        return BluetoothAdapter.checkBluetoothAddress(normalized) ? normalized : null;
    }

    private byte[] buildTestPrintPayload() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, new byte[]{0x1B, 0x40});
        write(out, new byte[]{0x1B, 0x61, 0x01});
        write(out, new byte[]{0x1B, 0x45, 0x01});
        write(out, "SANRO POS\n".getBytes(StandardCharsets.US_ASCII));
        write(out, new byte[]{0x1B, 0x45, 0x00});
        write(out, "Bluetooth Printer Test\n".getBytes(StandardCharsets.US_ASCII));
        write(out, "Native Bridge V1.0.6\n".getBytes(StandardCharsets.US_ASCII));
        write(out, "-------------------------------\n".getBytes(StandardCharsets.US_ASCII));
        write(out, "Jika teks ini tercetak, koneksi OK.\n\n\n".getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private byte[] buildPlainTextPayload(String text) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, new byte[]{0x1B, 0x40});
        write(out, text.getBytes(StandardCharsets.UTF_8));
        if (!text.endsWith("\n")) write(out, new byte[]{0x0A});
        write(out, new byte[]{0x0A, 0x0A});
        return out.toByteArray();
    }

    private void write(ByteArrayOutputStream out, byte[] bytes) {
        out.write(bytes, 0, bytes.length);
    }

    private JSONObject baseResult(boolean ok) {
        JSONObject result = new JSONObject();
        try { result.put("ok", ok); } catch (JSONException ignored) {}
        return result;
    }

    private JSONObject errorResult(String action, String error) {
        JSONObject result = baseResult(false);
        try {
            result.put("action", action);
            result.put("error", error);
        } catch (JSONException ignored) {}
        return result;
    }

    private void emit(JSONObject payload) {
        if (eventSink != null) eventSink.emit(payload.toString());
    }

    private void closeQuietly(BluetoothSocket socket) {
        if (socket == null) return;
        try { socket.close(); } catch (IOException ignored) {}
    }
}
