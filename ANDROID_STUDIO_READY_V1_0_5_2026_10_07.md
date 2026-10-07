# SANRO Android — V1.0.5 Android Studio Readiness

Date: 2026-10-07
Status: **READY TO PROCESS IN ANDROID STUDIO — BUILD/REGRESSION GATE**

This status means the source lineage needed to open/process V1.0.5 in Android Studio has been recovered and grounded. It does **not** mean Stage B Room/Hybrid is implemented or accepted.

## Evidence

Google Drive contains complete V1.0.5 Android Studio project trees for both SanroPOS and SanroStock, including root Gradle/settings, wrapper, app build files, Manifest, Java source, full resource trees, launcher assets and existing release build artifacts.

Both stored projects contain `app-release.apk` and `output-metadata.json` from previous successful release builds.

Release metadata confirms:

- SanroPOS applicationId `com.sanrotech.pos`, versionCode `5`, versionName `1.0.5`.
- SanroStock applicationId `com.sanrotech.stock`, versionCode `5`, versionName `1.0.5`.

The Drive SanroPOS `app/build.gradle` Git blob hash exactly matches the latest protected GitHub WORK V1.0.5 build file: `7aeffe9d6303e8c9bac93e130091b6bb3a5be622`.

The Drive SanroStock `app/build.gradle` Git blob hash exactly matches the latest protected GitHub WORK V1.0.5 build file: `b9587de22b0208fb3adefa282edb3e9d17116394`.

## Later protected delta

The Drive full projects were stored/built on 24 September 2026. The protected phone/tablet orientation behavior was added later on 2 October 2026.

Therefore Android Studio candidate preparation must use:

- Drive full V1.0.5 project as complete source base;
- latest GitHub WORK V1.0.5 MainActivity for SanroPOS;
- latest GitHub WORK V1.0.5 MainActivity for SanroStock;
- no unrelated refactor or functionality change.

This is an exact lineage completion using a full V1.0.5 source base plus later protected WORK delta, not a V1.0.2 reconstruction.

## Next gate in Android Studio

1. Materialize/download the complete Drive projects locally.
2. Preserve a FULL untouched checkpoint of each recovered project.
3. Apply only the latest protected WORK MainActivity files required to include the orientation baseline.
4. Create a FULL rollback of that candidate before any new feature work.
5. Open in Android Studio.
6. Use JDK 17 and Android SDK 35.
7. Gradle Sync.
8. Clean build SanroPOS and SanroStock without Room/Bluetooth/Hybrid changes.
9. Install/update and regression-test protected V1.0.5 behavior.
10. Only after PASS accept this candidate as the full protected source baseline for Stage B.

## Protected smoke minimum

- package/launcher identity;
- login/session/reopen;
- WebView routes;
- phone/tablet orientation policy;
- top/bottom safe area;
- Android Back behavior;
- camera/Scan Product;
- file chooser/upload;
- download with session;
- external/intent link handling;
- SSL fail-closed behavior;
- Android PrintManager bridge;
- SanroPOS and SanroStock target their correct URLs.

Regression = FAILED. Stop and restore full candidate rollback.

## Scope status

- V1.0.5 source discovery blocker: **RESOLVED**.
- Android Studio processing/build gate: **READY**.
- Clean-build verification: **PENDING**.
- Device smoke after rebuilt candidate: **PENDING**.
- Stage B Room foundation: **NOT STARTED**.
- LIVE Hosting/database schema audit: still separate/pending and required before later transaction mapping stages.

## Production impact

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO for this documentation step
- Restart Node.js: NO
- Production deployment: NONE
