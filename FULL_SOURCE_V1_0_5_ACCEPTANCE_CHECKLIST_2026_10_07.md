# SANRO Android — FULL V1.0.5 Source Acceptance Checklist

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Purpose: define the fail-closed gate for accepting a FULL Android Studio V1.0.5 source package before any Room/Bluetooth/Hybrid implementation.

This checklist does **not** authorize reconstruction from V1.0.2 or any older source. `WORK_V1_0_3`, `WORK_V1_0_4`, and `WORK_V1_0_5` are incremental deltas/evidence only, not standalone authoritative projects.

## 1. Project completeness — mandatory

A candidate FULL project must contain, as applicable to its actual Gradle layout:

- `settings.gradle` or `settings.gradle.kts`;
- root `build.gradle` or `build.gradle.kts`;
- `gradle.properties`;
- `gradlew` and `gradlew.bat`;
- `gradle/wrapper/*` including wrapper properties/JAR;
- all referenced module build files;
- `AndroidManifest.xml` for every app/module that requires one;
- complete `src/main/res` resources referenced by Manifest/source;
- complete Java/Kotlin source tree;
- `proguard-rules.pro` where referenced;
- launcher/adaptive-icon resources and referenced drawables/mipmaps;
- any XML/provider/network/file-path resources required by the Manifest/source.

Missing referenced files = **REJECT / NOT READY**. Do not fill gaps from V1.0.2 by assumption.

## 2. Identity/version invariants from protected V1.0.5 delta

SanroPOS must preserve:

- namespace/applicationId: `com.sanrotech.pos`;
- compileSdk: `35`;
- targetSdk: `35`;
- minSdk: `24`;
- versionCode: `5`;
- versionName: `1.0.5`;
- Java compatibility: `17`;
- start URL: `https://pos.sanrotech.com/sanro-pos`;
- trusted WebView host: `pos.sanrotech.com`.

SanroStock must preserve:

- namespace/applicationId: `com.sanrotech.stock`;
- compileSdk: `35`;
- targetSdk: `35`;
- minSdk: `24`;
- versionCode: `5`;
- versionName: `1.0.5`;
- Java compatibility: `17`;
- start URL: `https://pos.sanrotech.com/sanro-stock`;
- trusted WebView host: `pos.sanrotech.com`.

Any mismatch must be explained by newer authoritative build evidence before acceptance.

## 3. Protected runtime behavior that must match V1.0.5 WORK

Before opening Stage B, unchanged V1.0.5 must preserve:

- WebView login/session/cookies;
- same-host navigation to `pos.sanrotech.com`;
- external/intent URL handling;
- SSL error fail-closed behavior;
- Safe Browsing where supported;
- camera permission only for trusted HTTPS origin;
- Scan Product/camera flow;
- file chooser/upload;
- DownloadManager flow with session cookie/User-Agent;
- native Android PrintManager bridge for `window.print()`;
- top/bottom/display-cutout safe area;
- launcher identity/icons;
- Android Back behavior baseline;
- phone portrait rule for `<600dp` smallest width;
- tablet `>=600dp` follows Android/user orientation.

Current V1.0.5 native bridge evidence only exposes `printPage()`. Do not claim Room, Bluetooth ESC/POS, sync engine, local service, pairing, or Hybrid transaction routing already exists.

## 4. Lineage verification

Candidate FULL source must be checked against the known deltas:

- V1.0.3 delta: app build metadata + MainActivity changes;
- V1.0.4 delta: MainActivity/build metadata plus adaptive launcher XML evidence;
- V1.0.5 delta: MainActivity/build metadata and safe-area/work behavior;
- later orientation WORK commits: phone portrait / tablet user orientation.

The delta folders are validation evidence only. They are not permission to reconstruct missing files blindly.

## 5. Security/signing hygiene

The accepted source package must not require committing or sharing:

- production keystore;
- keystore password;
- key password;
- signing token;
- private API credentials;
- local secret properties.

Signing secrets remain outside GitHub and are injected through local/CI secret configuration.

## 6. Build acceptance sequence

Only after completeness passes:

1. create a FULL immutable checkpoint of the candidate source;
2. record hashes/file manifest;
3. build V1.0.5 **unchanged** with clean Gradle build;
4. do not add Room/Bluetooth/Hybrid code yet;
5. install/update over the existing WORK app where the test device permits, preserving app data/session;
6. run protected regression smoke;
7. compare launcher/package/version identity;
8. only after PASS mark the full source as the accepted source baseline for the next implementation stage.

Build failure, missing file, package mismatch, session loss caused by source/signing mismatch, or protected behavior regression = **FAILED / NOT ACCEPTED**.

## 7. Regression smoke before Stage B

Minimum smoke for both relevant apps:

- cold launch;
- existing session/login behavior;
- navigation and Back;
- safe-area top/bottom;
- phone/tablet orientation rule;
- camera/scan permission and flow;
- file chooser/upload;
- download;
- external URL/intent behavior;
- SSL safety behavior where testable safely;
- PrintManager print flow;
- SanroPOS opens `/sanro-pos`;
- SanroStock opens `/sanro-stock`.

Any WORK regression stops Stage B.

## 8. Checkpoint/rollback before first source mutation

Before Room/Bluetooth/Hybrid source changes:

- create FULL Android source checkpoint from the accepted V1.0.5 project;
- create a FULL Android rollback package/source snapshot;
- preserve the exact accepted file manifest/hash list;
- keep production web/backend/database untouched unless a later explicitly scoped stage requires them.

## 9. Acceptance result states

- incomplete / uncertain lineage → **NOT READY**;
- complete but not clean-build/smoke verified → **TESTING GATE ONLY**;
- unchanged clean build + regression smoke PASS → source baseline accepted for controlled Stage B work;
- Stage B itself starts separately and does not inherit WORK/LOCK status merely from source acceptance.

## Rollback/deployment impact of this document

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO
- Restart Node.js: NO
- Production deployment: NONE
