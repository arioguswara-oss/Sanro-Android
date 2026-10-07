# SANRO Android — V1.0.5 Full Recovered Candidate

Date: 2026-10-07
Status: **CANDIDATE / BUILD GATE PENDING**

## Result

The complete SanroPOS and SanroStock V1.0.5 source trees have been recovered from the connected Google Drive source lineage and assembled into an Android-Studio-ready candidate.

This does **not** mark a new WORK/LOCK baseline. The clean-build + real-device regression gate is still required.

## Provenance

1. Base source archives retrieved unchanged from Google Drive:
   - `SanroPOS_Android_V1_0_5.zip`
   - `SanroStock_Android_V1_0_5.zip`
2. The archived ZIPs contain the application source/resources but omit Gradle Wrapper files.
3. `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, and `gradle/wrapper/gradle-wrapper.properties` were retrieved from the corresponding expanded V1.0.5 project folders in the same Google Drive lineage. No wrapper was invented/re-generated.
4. Only the latest protected `WORK_V1_0_5` MainActivity files were overlaid onto the recovered app source to carry the later orientation baseline.

## Exact protected-file verification

- SanroPOS `MainActivity.java` candidate Git blob: `6ddca4944c370a0ac0cf9aeff9b458816c29a0f9`
- GitHub `WORK_V1_0_5` SanroPOS MainActivity blob: `6ddca4944c370a0ac0cf9aeff9b458816c29a0f9`
- SanroStock `MainActivity.java` candidate Git blob: `2923d4a0adb6ce19c920c3299bd59cf0e719d381`
- GitHub `WORK_V1_0_5` SanroStock MainActivity blob: `2923d4a0adb6ce19c920c3299bd59cf0e719d381`
- SanroPOS `app/build.gradle` candidate Git blob: `7aeffe9d6303e8c9bac93e130091b6bb3a5be622`
- SanroStock `app/build.gradle` candidate Git blob: `b9587de22b0208fb3adefa282edb3e9d17116394`

Therefore the protected MainActivity overlays are byte-for-byte identical to the current GitHub WORK snapshots.

## Project metadata

Both candidates include Gradle Wrapper files from the same Drive project lineage.

- Android Gradle Plugin: `8.7.3`
- Gradle wrapper distribution: `9.3.0`
- compileSdk: `35`
- minSdk: `24`
- targetSdk: `35`
- Java source/target: `17`
- versionCode: `5`
- versionName: `1.0.5`

Static sanity checks performed:
- Android/resource XML parses successfully.
- Gradle Wrapper JAR is readable.
- candidate archive integrity test passes.

## Reference APKs

Existing Google Drive release artifacts remain available as lineage references:

SanroPOS:
- applicationId `com.sanrotech.pos`
- versionCode `5`
- versionName `1.0.5`
- APK size `362140` bytes
- SHA-256 `71efb1f87c3ece7fae1f1c1c3281108d311364ea87db081630b2d8e5fd722afd`

SanroStock:
- applicationId `com.sanrotech.stock`
- versionCode `5`
- versionName `1.0.5`
- APK size `362156` bytes
- SHA-256 `56e7fe7384db0539e40fda8652ca30b6bb112cbd032065553853c3b42789beb7`

These APKs predate the later protected orientation WORK overlay, so they are reference artifacts only and do not prove that the recovered candidate has been rebuilt.

## Build attempt in current execution environment

A Gradle invocation was attempted, but the environment cannot perform the Android build because:

1. Android SDK is not installed.
2. JDK 17 is not installed; only JDK 21 is present.
3. Gradle 9.3.0 is not cached.
4. The execution container cannot resolve `services.gradle.org`, so the wrapper cannot download the distribution.

This is an **environment blocker**, not a compile failure. Do not mark the candidate as FAILED/REGRESSION and do not mark it as build PASS.

## Required acceptance gate

1. Open each recovered candidate in Android Studio.
2. Use JDK 17 and Android SDK 35.
3. Gradle Sync.
4. Clean build SanroPOS and SanroStock without functional changes.
5. Install/update on a real device.
6. Regression-check protected V1.0.5 behavior: login/logout, session reopen, WebView/navigation, camera scan, file chooser/download, print, external links, safe area, phone portrait lock, tablet user rotation, and Back behavior.
7. If all PASS, accept the full-source candidate as the protected baseline for Stage B.
8. If regression appears, mark FAILED/REGRESSION and restore the pre-change V1.0.5 source checkpoint.

## Scope protection

No Room/SQLite, Hybrid routing, Device Connector implementation, Bluetooth printer driver, Payment Engine, Hosting frontend/backend, or database schema/data was changed while producing this recovery candidate.

Production impact:
- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Node.js restart: NO
- Android candidate rollback: prepared locally, but remains `PENDING-BUILD` until the acceptance gate passes.
