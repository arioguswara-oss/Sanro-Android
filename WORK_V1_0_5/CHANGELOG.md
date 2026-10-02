# SANRO Android V1.0.5

Status: WORK / device smoke test PASS — release APK belum dibuat.

## Fix
- Safe area Android diperluas dari hanya status bar atas menjadi seluruh system bars + display cutout.
- WebView sekarang mendapat inset kiri/atas/kanan/bawah.
- Tujuan utama: tombol paling bawah seperti Keluar/Logout tidak tertutup navigation bar / gesture area Android dan tetap bisa diklik.
- Fix top safe area V1.0.4 tetap dipertahankan agar header tidak menabrak jam/sinyal/baterai/punch-hole.
- Logo SANRO asli tetap dipakai.
- Build fix PrintManager dan Safe Browsing tetap dipertahankan.
- versionCode 5 / versionName 1.0.5.

## Target
- SanroPOS -> https://pos.sanrotech.com/sanro-pos
- SanroStock -> https://pos.sanrotech.com/sanro-stock

## View orientation rule
- **LOCKED rule**: phone (`smallestScreenWidthDp < 600`) selalu portrait.
- Tablet (`smallestScreenWidthDp >= 600`) menggunakan orientasi USER Android sehingga auto-rotate tetap bekerja saat diaktifkan dan orientation lock perangkat tetap dihormati.
- Implementasi diterapkan sama pada SanroPOS dan SanroStock.
- Status implementasi source: WORK; perlu build APK + real-device QA sebelum baseline aplikasi dinyatakan LOCKED.

## QA status
- SanroPOS V1.0.5: real-device smoke test PASS pada Xiaomi user.
- SanroStock V1.0.5: real-device smoke test PASS pada Xiaomi user.
- Safe-area atas/bawah dan launcher identity dikonfirmasi WORK.
- Release APK/signing belum dibuat.
- Belum LOCKED sampai release APK selesai dan fungsi perangkat penting (terutama print/barcode bila dipakai) divalidasi.
