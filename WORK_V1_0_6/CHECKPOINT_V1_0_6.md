# SANRO Android V1.0.6 — Checkpoint Bluetooth Printer Native Bridge

Tanggal: 6 Oktober 2026

Status: IMPLEMENTED / source candidate. Belum Android build, belum APK, belum real-device TESTING.

## Source of truth
- Repo: `arioguswara-oss/Sanro-Android`
- Development branch: `codex/android-v1.0.6-bluetooth-printer`
- Baseline source branch sebelum pekerjaan: `main`
- Baseline commit: `1c223c86ed15130ecd3a470d6b7efe7dbcafe87a`
- Rollback branch: `rollback/android-v1.0.5-work-20261006`
- Protected baseline: `WORK_V1_0_5` = WORK / PROTECTED BASELINE

## Scope V1.0.6
Hanya SanroPOS Android Native Bridge Bluetooth Printer:
- paired Bluetooth Classic devices;
- printer name/address exposure ke WebView;
- pilih + simpan printer per device/app;
- Test Print;
- RFCOMM/SPP direct connection;
- ESC/POS raw Base64 print;
- plain-text helper;
- Android 12+ `BLUETOOTH_CONNECT` runtime permission.

Tidak termasuk:
- SanroStock source;
- SQLite/Room local-first;
- payment/QRIS;
- perubahan SANRO POS web production;
- Superadmin/Ticket/project lain.

## WORK / LOCK protection
Dipertahankan dari V1.0.5:
- phone portrait only;
- tablet USER orientation / auto-rotate sesuai setting Android;
- safe-area system bars + cutout;
- camera/barcode permission flow;
- file chooser/download behavior;
- URL/navigation restrictions;
- Android PrintManager fallback melalui `AndroidBridge.printPage()`.

## Source baru
- `SanroPOS/app/build.gradle` -> versionCode 6 / versionName 1.0.6.
- `BluetoothPrinterBridge.java` -> Bluetooth Classic/RFCOMM/SPP + paired listing + SharedPreferences + async printing.
- `MainActivity.java` -> memasang bridge ke WebView.
- `BRIDGE_API.md` -> kontrak API web/native.
- `MANIFEST_PATCH.md` -> permission manifest minimal yang harus diterapkan ke source lengkap saat build.

## Security boundary
Raw Java interface dipasang sebagai `SanroNativeBridge`, tetapi seluruh method membutuhkan random per-Activity token. Token hanya dimasukkan ke wrapper top-level `window.SanroNative` saat page finish. Embedded cross-origin frame tidak diberi token untuk memanggil operasi printer.

## Current API
Supported web API:
- `SanroNative.getCapabilities()`
- `SanroNative.listPairedPrinters()`
- `SanroNative.getSelectedPrinter()`
- `SanroNative.selectPrinter(address)`
- `SanroNative.clearSelectedPrinter()`
- `SanroNative.requestBluetoothPermission()`
- `SanroNative.testPrint()`
- `SanroNative.printText(text)`
- `SanroNative.printEscPosBase64(base64)`

Async result event: `sanro-native-printer`.

## Current blocker
Repository V1.0.5 adalah snapshot parsial, bukan Android Studio project lengkap. Snapshot tidak menyediakan `AndroidManifest.xml`, root Gradle/settings/wrapper, proguard file, dan seluruh resource build yang diperlukan untuk membuktikan APK build.

JANGAN membuat file baseline yang hilang dengan asumsi. Gunakan source Android Studio V1.0.5 lengkap yang benar-benar menghasilkan APK WORK terakhir, lalu apply delta V1.0.6 ini.

## Validation yang sudah dilakukan
- Git compare branch terhadap `main`: hanya 6 file di `WORK_V1_0_6`, seluruhnya SanroPOS/docs; tidak ada perubahan SanroStock atau baseline V1.0.5.
- Static source review: permission flow, paired-only behavior, bounded raw payload, background print executor, sanitized errors, persistent selection, secure->insecure RFCOMM fallback, token-gated JS bridge.
- Android compile/build: BELUM DAPAT DIBUKTIKAN karena source project lengkap tidak tersedia di repo.
- Real printer test: BELUM.

## Acceptance berikutnya
1. Ambil full source Android Studio V1.0.5 yang benar-benar sesuai APK WORK terakhir.
2. Buat checkpoint/rollback dari source lengkap tersebut.
3. Apply delta V1.0.6 dan permission manifest minimal.
4. Build debug APK.
5. Pair PRJ-80BT di Android Settings.
6. Test permission + list paired + select/persist.
7. Test Print fisik.
8. Test raw ESC/POS struk.
9. Regression V1.0.5: login, orientation, safe-area, camera/barcode, download, navigation, PrintManager fallback.
10. User test. Hanya setelah PASS boleh naik ke WORK; 100% hanya jika user eksplisit LOCK BASELINE.

## Rollback
- Frontend/Web POS rollback: TIDAK.
- Android source rollback: YA -> branch `rollback/android-v1.0.5-work-20261006` / commit `1c223c86ed15130ecd3a470d6b7efe7dbcafe87a`.
- Backend rollback: TIDAK.
- Database rollback: TIDAK.
- Restart Node.js: TIDAK.
