# SANRO Android V1.0.6 — Native Bridge Bluetooth Printer

Status: IMPLEMENTED / source candidate. Belum build APK dan belum real-device TESTING.

## Protected baseline
- V1.0.5 tetap WORK / PROTECTED BASELINE.
- Rule orientation LOCKED tetap dipertahankan: phone portrait only; tablet mengikuti USER orientation Android.
- Safe-area system bars/cutout V1.0.5 dipertahankan.
- Existing Android PrintManager bridge tetap dipertahankan sebagai fallback.
- SanroStock tidak diubah.

## Implemented source
- Native object `SanroNative` ditanam langsung di WebView SanroPOS.
- Membaca Bluetooth Classic devices yang sudah paired di Android Settings.
- Mengembalikan nama + MAC printer ke web.
- Memilih printer dan menyimpan pilihan per device/app melalui SharedPreferences.
- Runtime permission `BLUETOOTH_CONNECT` untuk Android 12+.
- Test Print ESC/POS.
- Direct Bluetooth RFCOMM/SPP printing.
- Raw ESC/POS Base64 printing untuk integrasi struk web.
- Plain-text print helper.
- Print job berjalan di background single-thread executor agar UI/WebView tidak diblokir.
- Secure RFCOMM dicoba lebih dulu, lalu fallback insecure RFCOMM untuk kompatibilitas printer thermal paired.
- Result print dikirim ke web melalui event `sanro-native-printer`.
- Error ke web disanitasi; exception/provider detail tidak diekspos.

## Target printer awal
- PRJ-80BT / thermal ESC/POS 80 mm.
- Bluetooth Classic RFCOMM/SPP UUID standar `00001101-0000-1000-8000-00805F9B34FB`.

## Build blocker yang masih faktual
Snapshot V1.0.5 pada GitHub tidak memuat project Android Studio lengkap (`AndroidManifest.xml`, root Gradle/settings/wrapper, dan seluruh resource build). Karena itu branch ini belum boleh diklaim buildable atau TESTING hanya dari source delta.

Permission manifest yang harus diterapkan ke source Android Studio lengkap dicatat di `SanroPOS/MANIFEST_PATCH.md`. Jangan membuat manifest baru dengan menebak isi baseline.

## Acceptance berikutnya
1. Gunakan source Android Studio V1.0.5 lengkap yang benar-benar menghasilkan APK WORK terakhir.
2. Apply hanya delta V1.0.6 ini + permission manifest minimal.
3. Build debug/release candidate.
4. Pair PRJ-80BT dari Android Settings.
5. Verifikasi permission Bluetooth Android 12+.
6. Verifikasi daftar paired printer dan persistence pilihan.
7. Test Print fisik.
8. Verifikasi raw ESC/POS receipt print.
9. Regression: login/WebView, safe area, orientation, camera/barcode, download, Android PrintManager fallback.
10. User test sebelum status WORK; LOCK hanya setelah approval eksplisit user.
