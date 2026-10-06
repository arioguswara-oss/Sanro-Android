package com.sanrotech.pos;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.print.PrintManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.URISyntaxException;

public class MainActivity extends Activity {
    private static final String START_URL = "https://pos.sanrotech.com/sanro-pos";
    private static final String ALLOWED_HOST = "pos.sanrotech.com";
    private static final int FILE_CHOOSER_REQUEST = 2001;
    private static final int CAMERA_PERMISSION_REQUEST = 2002;
    private static final int BLUETOOTH_CONNECT_PERMISSION_REQUEST = 2003;
    private static final int TABLET_MIN_SMALLEST_WIDTH_DP = 600;

    private WebView webView;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> filePathCallback;
    private PermissionRequest pendingWebPermissionRequest;
    private BluetoothPrinterBridge bluetoothPrinterBridge;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyDeviceOrientationPolicy();
        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        root.addView(webView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(3), Gravity.TOP);
        root.addView(progressBar, pp);
        root.setBackgroundColor(Color.WHITE);
        setContentView(root);
        applySystemBarSafeArea(root);
        configureWebView();
        if (savedInstanceState == null) webView.loadUrl(START_URL); else webView.restoreState(savedInstanceState);
    }

    private void applyDeviceOrientationPolicy() {
        int smallestWidthDp = getResources().getConfiguration().smallestScreenWidthDp;
        boolean isTablet = smallestWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP;
        setRequestedOrientation(isTablet
                ? ActivityInfo.SCREEN_ORIENTATION_USER
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private void applySystemBarSafeArea(View root) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                int leftInset = 0;
                int topInset = 0;
                int rightInset = 0;
                int bottomInset = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Insets safeInsets = insets.getInsets(
                            WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                    leftInset = safeInsets.left;
                    topInset = safeInsets.top;
                    rightInset = safeInsets.right;
                    bottomInset = safeInsets.bottom;
                } else {
                    leftInset = insets.getSystemWindowInsetLeft();
                    topInset = insets.getSystemWindowInsetTop();
                    rightInset = insets.getSystemWindowInsetRight();
                    bottomInset = insets.getSystemWindowInsetBottom();
                }
                v.setPadding(leftInset, topInset, rightInset, bottomInset);
                return insets;
            });
            root.requestApplyInsets();
        }
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setUseWideViewPort(true);
        s.setTextZoom(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) s.setSafeBrowsingEnabled(true);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        bluetoothPrinterBridge = new BluetoothPrinterBridge(
                this,
                BLUETOOTH_CONNECT_PERMISSION_REQUEST,
                this::emitNativePrinterEvent);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.addJavascriptInterface(bluetoothPrinterBridge, "SanroNative");

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return handleNavigation(request.getUrl()); }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) { return handleNavigation(Uri.parse(url)); }
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) { super.onPageStarted(view,url,favicon); progressBar.setVisibility(View.VISIBLE); }
            @Override public void onPageFinished(WebView view, String url) { super.onPageFinished(view,url); progressBar.setVisibility(View.GONE); CookieManager.getInstance().flush(); injectNativePrintBridge(); }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) { super.onReceivedError(view,request,error); if (request.isForMainFrame()) showConnectionError(); }
            @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) { handler.cancel(); Toast.makeText(MainActivity.this,"Koneksi aman gagal diverifikasi.",Toast.LENGTH_LONG).show(); }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int p) { progressBar.setProgress(p); progressBar.setVisibility(p >= 100 ? View.GONE : View.VISIBLE); }
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = cb;
                try { startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST); return true; }
                catch (ActivityNotFoundException e) { filePathCallback = null; Toast.makeText(MainActivity.this,"Tidak ada aplikasi pemilih file.",Toast.LENGTH_SHORT).show(); return false; }
            }
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean asksCamera = false;
                    for (String r : request.getResources()) if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) { asksCamera = true; break; }
                    if (!asksCamera || !isTrustedWebOrigin(request.getOrigin())) { request.deny(); return; }
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                    else { pendingWebPermissionRequest = request; requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST); }
                });
            }
        });

        webView.setDownloadListener((url,ua,cd,mime,len) -> {
            try {
                DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                req.addRequestHeader("User-Agent", ua);
                String cookie = CookieManager.getInstance().getCookie(url); if (cookie != null) req.addRequestHeader("Cookie", cookie);
                req.setMimeType(mime);
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, android.webkit.URLUtil.guessFileName(url, cd, mime));
                ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(req);
                Toast.makeText(this,"File sedang diunduh.",Toast.LENGTH_SHORT).show();
            } catch(Exception e) { openExternal(Uri.parse(url)); }
        });
    }

    private boolean isTrustedWebOrigin(Uri origin) { return origin != null && "https".equalsIgnoreCase(origin.getScheme()) && ALLOWED_HOST.equalsIgnoreCase(origin.getHost()); }
    private boolean handleNavigation(Uri uri) {
        if (uri == null) return true;
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        if ("https".equals(scheme) && ALLOWED_HOST.equals(host)) return false;
        if ("intent".equals(scheme)) {
            try { Intent i = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME); if (i.resolveActivity(getPackageManager()) != null) startActivity(i); }
            catch (URISyntaxException ignored) {}
            return true;
        }
        openExternal(uri); return true;
    }
    private void openExternal(Uri uri) { try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch(Exception e) { Toast.makeText(this,"Tidak ada aplikasi untuk membuka tautan ini.",Toast.LENGTH_SHORT).show(); } }
    private void injectNativePrintBridge() {
        String js = "(function(){if(window.__sanroNativePrintInstalled)return;window.__sanroNativePrintInstalled=true;window.__sanroNativePrinterBridgeVersion='1.0.6';window.__sanroOriginalPrint=window.print;window.print=function(){try{AndroidBridge.printPage();}catch(e){if(window.__sanroOriginalPrint)window.__sanroOriginalPrint();}};})();";
        webView.evaluateJavascript(js, null);
    }
    private void emitNativePrinterEvent(String json) {
        runOnUiThread(() -> {
            if (webView == null) return;
            String script = "window.dispatchEvent(new CustomEvent('sanro-native-printer',{detail:" + json + "}));";
            webView.evaluateJavascript(script, null);
        });
    }
    private void showConnectionError() {
        String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><style>body{font-family:sans-serif;padding:32px;color:#111827;background:#fff}button{padding:12px 18px;border:0;border-radius:8px;background:#111827;color:#fff;font-size:16px}p{line-height:1.5;color:#4b5563}</style></head><body><h2>Koneksi gagal</h2><p>Periksa internet lalu coba lagi.</p><button onclick=\"location.href='" + START_URL + "'\">Coba Lagi</button></body></html>";
        webView.loadDataWithBaseURL(START_URL, html, "text/html", "UTF-8", null);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == CAMERA_PERMISSION_REQUEST && pendingWebPermissionRequest != null) {
            PermissionRequest r = pendingWebPermissionRequest; pendingWebPermissionRequest = null;
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) r.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
            else { r.deny(); Toast.makeText(this,"Izin kamera diperlukan untuk scan barcode.",Toast.LENGTH_LONG).show(); }
            return;
        }
        if (requestCode == BLUETOOTH_CONNECT_PERMISSION_REQUEST && bluetoothPrinterBridge != null) {
            boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
            bluetoothPrinterBridge.onPermissionResult(granted);
        }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == FILE_CHOOSER_REQUEST) { if (filePathCallback == null) return; filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data)); filePathCallback = null; }
    }
    @Override protected void onSaveInstanceState(Bundle outState) { webView.saveState(outState); super.onSaveInstanceState(outState); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) { webView.goBack(); return true; } return super.onKeyDown(keyCode,event); }
    @Override protected void onDestroy() {
        if (bluetoothPrinterBridge != null) {
            bluetoothPrinterBridge.shutdown();
            bluetoothPrinterBridge = null;
        }
        if (webView != null) {
            webView.removeJavascriptInterface("SanroNative");
            webView.removeJavascriptInterface("AndroidBridge");
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }

    private final class AndroidBridge {
        @JavascriptInterface public void printPage() { runOnUiThread(() -> { try { PrintManager pm=(PrintManager)getSystemService(Context.PRINT_SERVICE); String name=getString(R.string.app_name)+" - Cetak"; pm.print(name,webView.createPrintDocumentAdapter(name),null); } catch(Exception e) { Toast.makeText(MainActivity.this,"Fitur cetak Android tidak tersedia.",Toast.LENGTH_LONG).show(); } }); }
    }
}
