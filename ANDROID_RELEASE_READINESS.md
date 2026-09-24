# SANRO Android — Release Readiness

Last reviewed: 24 September 2026

## Current app status
- SanroPOS: **V1.0.5 WORK / real-device smoke PASS**
- SanroStock: **V1.0.5 WORK / real-device smoke PASS**
- versionCode: **5**
- versionName: **1.0.5**
- SanroPOS applicationId: `com.sanrotech.pos`
- SanroStock applicationId: `com.sanrotech.stock`
- SanroPOS target: `https://pos.sanrotech.com/sanro-pos`
- SanroStock target: `https://pos.sanrotech.com/sanro-stock`

## Confirmed from repository
The latest changelog states that top/bottom Android safe-area behavior and launcher identity are WORK on the tested Xiaomi device.

**Release APK/signing has not been completed yet.**

## Repository structure note
The repository currently contains:
- base binary source package: `SANRO_ANDROID_POS_STOCK_V1_0_2.zip`
- incremental source deltas under `WORK_V1_0_3/`, `WORK_V1_0_4/`, and `WORK_V1_0_5/`

Before generating a release build, reconstruct and verify the complete V1.0.5 project tree. Do not treat only the WORK delta folders as a complete Android project.

## Release gate
1. Reconstruct full SanroPOS and SanroStock V1.0.5 source.
2. Run clean Gradle build.
3. Verify both debug builds again if reconstruction changes file layout.
4. Create release signing outside the repository.
5. Never commit keystore, store password, key password, signing token, or local signing properties.
6. Build release APK/AAB for both apps.
7. Install release build on a second device and verify:
   - launcher opens correct app,
   - login,
   - safe area top/bottom,
   - navigation/back behavior,
   - camera/barcode if used,
   - print flow if used,
   - web updates load correctly.
8. Only after release-device QA PASS may V1.0.5 be marked LOCKED.

## Signing policy
Use a persistent production keystore stored outside GitHub with a secure backup. Signing configuration should read secrets from local/CI environment or an ignored local properties file. Do not hard-code credentials in Gradle.

## Relationship with SANRO POS web
These Android apps are WebView/PWA wrappers; normal SANRO POS/SANRO STOCK web behavior continues to follow the hosted web application. Native Android release changes should remain limited to device integration, wrapper behavior, signing, and distribution unless explicitly approved.
