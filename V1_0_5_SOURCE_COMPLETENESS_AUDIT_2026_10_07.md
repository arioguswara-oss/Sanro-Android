# SANRO Android — V1.0.5 Source Completeness Audit

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Scope: repository/documentation audit only. No Android source/runtime/Hosting/database mutation.

## Source-of-truth protection

This audit does NOT authorize reconstruction of the current Android WORK baseline from V1.0.2 or other stale packages.

Current protected user-tested behavior remains V1.0.5 WORK. A complete source tree must be proven before Android Studio implementation proceeds.

## Exact WORK_V1_0_5 repository contents

Recursive tree audit of `WORK_V1_0_5` shows only:

- `CHANGELOG.md`
- `SanroPOS/app/build.gradle`
- `SanroPOS/app/src/main/java/com/sanrotech/pos/MainActivity.java`
- `SanroStock/app/build.gradle`
- `SanroStock/app/src/main/java/com/sanrotech/stock/MainActivity.java`

The delta snapshot does NOT contain a standalone buildable Android Studio project.

Missing from this WORK snapshot include at minimum:

- root `settings.gradle` / `settings.gradle.kts`
- root `build.gradle` / `build.gradle.kts`
- `gradle.properties`
- Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`)
- each app `AndroidManifest.xml`
- full `res/` trees, including `R.string.app_name` referenced by current Java code
- launcher resources currently used by the tested APK
- `proguard-rules.pro` although referenced by app Gradle
- signing-neutral complete project layout

Therefore `WORK_V1_0_5` must be treated as an incremental/protected source snapshot, not a complete rollback/build source.

## Confirmed SanroPOS build metadata

From `WORK_V1_0_5/SanroPOS/app/build.gradle`:

- namespace/applicationId: `com.sanrotech.pos`
- compileSdk: 35
- minSdk: 24
- targetSdk: 35
- versionCode: 5
- versionName: `1.0.5`
- Java source/target: 17
- release minify disabled

## Confirmed SanroStock build metadata

From `WORK_V1_0_5/SanroStock/app/build.gradle`:

- namespace/applicationId: `com.sanrotech.stock`
- compileSdk: 35
- minSdk: 24
- targetSdk: 35
- versionCode: 5
- versionName: `1.0.5`
- Java source/target: 17
- release minify disabled

## Protected MainActivity behavior observed in snapshot

Both apps currently implement the same wrapper pattern with different START_URL/package:

- SanroPOS START_URL: `https://pos.sanrotech.com/sanro-pos`
- SanroStock START_URL: `https://pos.sanrotech.com/sanro-stock`
- allowed WebView host restricted to `pos.sanrotech.com`
- JavaScript + DOM storage enabled
- WebView database setting enabled (this is NOT Room/SQLite local-first implementation)
- file access disabled; content access allowed
- mixed content never allowed
- Safe Browsing enabled where supported
- cookies and third-party cookies accepted for WebView session compatibility
- camera Web permission granted only for trusted HTTPS origin after Android CAMERA permission
- SSL errors are cancelled/fail-closed
- external/non-allowed URLs leave the WebView
- file chooser supported
- DownloadManager supported
- system-bar/display-cutout safe-area padding
- phone `<600dp` portrait; tablet `>=600dp` follows Android USER orientation
- Android PrintManager bridge intercepts `window.print()`
- WebView state saved/restored

These behaviors must be regression-tested when a complete source is recovered.

## Native bridge boundary in V1.0.5

Observed `AndroidBridge` currently exposes only native page printing (`printPage()`).

No repository evidence in the V1.0.5 WORK delta shows implementation of:

- Room database
- SQLite operational repository
- transactional outbox/sync queue
- device identity/Hybrid pairing service
- local API for Perangkat Utama/Kasir
- Bluetooth Classic ESC/POS printer bridge
- USB/LAN printer connector
- Google Drive encrypted backup/restore

Therefore these remain future implementation stages and must not be reported as implemented.

## Important verification note — Back behavior

The Java snapshot contains a WebView history back handler (`webView.goBack()` when WebView can go back). Existing user-tested Android/PWA behavior and protected web navigation rules take priority over assumptions from this one delta file. Do not alter Back behavior during source recovery. First reproduce the current APK behavior unchanged on a real device; only a verified bug plus explicit scope may open this area.

## Required complete-source acceptance before Stage B

A candidate FULL V1.0.5 project is accepted only if all are true:

1. complete Gradle/settings/wrapper structure exists;
2. both manifests and complete resources exist;
3. package/application IDs match protected apps;
4. build metadata matches V1.0.5 unless proven otherwise by the tested build lineage;
5. launcher identity/assets match tested apps;
6. no secrets/keystore committed;
7. clean build succeeds without inventing missing runtime behavior;
8. debug/install can update over the existing V1.0.5 app where signing lineage permits;
9. real-device regression passes login/session, WebView, scan/camera, file chooser/upload, download, external link handling, SSL guard, PrintManager, safe area, launcher, orientation, and Back/navigation behavior;
10. a complete Android checkpoint + rollback is created before any Room/Bluetooth/Hybrid change.

## Decision

**Android Studio implementation readiness: NOT READY.**

Reason: complete buildable V1.0.5 source that deterministically represents the user-tested WORK APK is not yet available/proven.

Do not use `SANRO_ANDROID_POS_STOCK_V1_0_2.zip` as authority to silently reconstruct V1.0.5.

## Rollback / deployment impact

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO
- Restart Node.js: NO
- Production deployment: NONE
