# SANRO Android V1.0.4

Status: WORK / device-test candidate.

## Fixes
- Launcher icon SanroPOS dan SanroStock memakai logo SANRO asli yang sudah diberikan user.
- Placeholder adaptive icon huruf S dihapus dari paket build.
- Status bar / display cutout safe area ditambahkan agar header WebView tidak tertabrak jam, sinyal, baterai, atau punch-hole.
- Area status bar tetap putih dengan ikon sistem gelap sesuai theme Android; header web dimulai di bawah safe area.
- Build fixes V1.0.3 (PrintManager dan Safe Browsing) dipertahankan.
- versionCode 4 / versionName 1.0.4.

## App targets
- SanroPOS -> https://pos.sanrotech.com/sanro-pos
- SanroStock -> https://pos.sanrotech.com/sanro-stock

## Icon asset manifest
Original user logo SHA-256:
`b1a14e93c539cb523a2824398edd19d078235939675f2418faea3c8f66be7500`

Generated adaptive foreground SHA-256:
`f797ae5ec5cb5ef1f4527c088bf66342af4327679426c5b17df4b91bb88bde1f`

Generated xxxhdpi launcher SHA-256:
`e53fadf6855a452a0fa62cfb47857732e853ca36d41ba8b8451532e3124160e4`

Binary launcher assets are included in the V1.0.4 build package. This version is not LOCKED until Android Studio build and real-device tests pass.
