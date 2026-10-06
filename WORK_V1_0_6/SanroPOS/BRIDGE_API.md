# SANRO Android V1.0.6 — SanroNative Bluetooth Printer Bridge

WebView exposes the supported native API as:

```js
window.SanroNative
```

`window.SanroNative` is injected only into the top-level SANRO POS document after page load. The underlying raw JavaScript interface uses a per-Activity random token and must not be called directly. This prevents an embedded cross-origin frame from invoking Bluetooth printer operations without the wrapper token.

The legacy `window.AndroidBridge.printPage()` remains unchanged for Android PrintManager fallback.

## Synchronous methods

```js
SanroNative.getCapabilities()
SanroNative.listPairedPrinters()
SanroNative.getSelectedPrinter()
SanroNative.selectPrinter(macAddress)
SanroNative.clearSelectedPrinter()
SanroNative.requestBluetoothPermission()
```

Each synchronous method returns a JSON string. Parse it with `JSON.parse(...)`.

Example paired-printer response:

```json
{
  "ok": true,
  "action": "listPairedPrinters",
  "printers": [
    {
      "name": "PRJ-80BT",
      "address": "00:11:22:33:44:55",
      "selected": true
    }
  ],
  "selectedAddress": "00:11:22:33:44:55"
}
```

Printer selection is stored in Android `SharedPreferences`, so it persists per installed app/device.

## Asynchronous print methods

```js
SanroNative.testPrint()
SanroNative.printText(text)
SanroNative.printEscPosBase64(base64EscPosBytes)
```

Print work runs off the UI thread. Result is returned through a browser event:

```js
window.addEventListener('sanro-native-printer', (event) => {
  console.log(event.detail);
});
```

Successful example:

```json
{
  "ok": true,
  "action": "testPrint",
  "printer": {
    "name": "PRJ-80BT",
    "address": "00:11:22:33:44:55",
    "selected": true
  }
}
```

Known error codes are intentionally sanitized:

- `BRIDGE_UNAUTHORIZED`
- `BLUETOOTH_NOT_SUPPORTED`
- `BLUETOOTH_PERMISSION_REQUIRED`
- `BLUETOOTH_PERMISSION_DENIED`
- `BLUETOOTH_DISABLED`
- `NO_PRINTER_SELECTED`
- `PRINTER_NOT_PAIRED`
- `SELECTED_PRINTER_NOT_PAIRED`
- `INVALID_PRINTER_ADDRESS`
- `INVALID_PRINT_TEXT`
- `INVALID_PRINT_PAYLOAD`
- `BLUETOOTH_READ_FAILED`
- `PRINTER_CONNECTION_FAILED`

## Scope contract

- Bluetooth Classic only.
- RFCOMM/SPP UUID: `00001101-0000-1000-8000-00805F9B34FB`.
- ESC/POS raw-byte printing supported through Base64 payload.
- Target initial printer: PRJ-80BT / compatible ESC/POS 80 mm.
- No Bluetooth discovery/scan is performed. Pair printer first in Android Settings.
- No SANRO Stock source is changed in V1.0.6.
- No POS web production source is changed by this Android source batch.
