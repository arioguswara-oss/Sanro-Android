# SANRO Android V1.0.6 — Required AndroidManifest permissions

Baseline V1.0.5 yang tersimpan di repo adalah snapshot parsial dan tidak memuat `AndroidManifest.xml` lengkap. Jangan membuat manifest baru dari asumsi.

Pada source Android Studio lengkap yang menjadi source of truth saat build V1.0.6, tambahkan hanya permission berikut di level `<manifest>` dan pertahankan seluruh permission/intent-filter/theme existing lainnya tanpa perubahan:

```xml
<uses-permission
    android:name="android.permission.BLUETOOTH"
    android:maxSdkVersion="30" />
<uses-permission
    android:name="android.permission.BLUETOOTH_ADMIN"
    android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

Catatan scope:
- V1.0.6 hanya membaca perangkat yang SUDAH di-pair melalui Setting Bluetooth Android.
- Tidak melakukan discovery/scan perangkat baru, sehingga `BLUETOOTH_SCAN` tidak diperlukan untuk scope ini.
- Android 12 / API 31+ meminta runtime permission `BLUETOOTH_CONNECT` melalui Native Bridge.
- Android 11 ke bawah menggunakan permission Bluetooth legacy yang dibatasi sampai API 30.
- Jangan mengubah permission kamera, storage/download, network security, launcher, orientation, atau intent-filter existing.
