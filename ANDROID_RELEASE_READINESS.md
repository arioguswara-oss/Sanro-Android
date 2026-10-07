# SANRO Android — Release Readiness

Last reviewed: 7 October 2026

## Current app status
- SanroPOS: **V1.0.5 WORK / real-device smoke PASS**
- SanroStock: **V1.0.5 WORK / real-device smoke PASS**
- versionCode: **5**
- versionName: **1.0.5**
- SanroPOS applicationId: `com.sanrotech.pos`
- SanroStock applicationId: `com.sanrotech.stock`
- SanroPOS target: `https://pos.sanrotech.com/sanro-pos`
- SanroStock target: `https://pos.sanrotech.com/sanro-stock`

## Source-of-truth rule
Hosting/user-verified behavior and latest WORK/LOCK outrank old Android packages or historical repository reconstruction.

The current `WORK_V1_0_5` directory is an incremental source snapshot, not a complete Android Studio project. Do not silently reconstruct V1.0.5 from `SANRO_ANDROID_POS_STOCK_V1_0_2.zip` and assume parity with the user-tested WORK APK.

See `V1_0_5_SOURCE_COMPLETENESS_AUDIT_2026_10_07.md` for the exact repository completeness audit.

## Current repository evidence
The repository contains:
- historical/base binary source package: `SANRO_ANDROID_POS_STOCK_V1_0_2.zip`;
- incremental protected source snapshots under `WORK_V1_0_3/`, `WORK_V1_0_4/`, and `WORK_V1_0_5/`;
- V1.0.5 WORK delta containing app-level Gradle metadata and MainActivity behavior, but not the complete Gradle/Manifest/resources/wrapper project required for a deterministic standalone build.

**Release APK/signing has not been completed yet.**

## Mandatory gate before Android Studio implementation
1. Obtain/prove the FULL V1.0.5 source/project that corresponds to the user-tested WORK APK lineage.
2. Verify root Gradle/settings/wrapper, both manifests, complete resources, package IDs, launcher assets, app Gradle files, and signing-neutral structure.
3. Build V1.0.5 unchanged first.
4. Perform real-device regression before introducing Room/Bluetooth/Hybrid changes.
5. Create a complete Android checkpoint + rollback.
6. Only then open the next implementation stage.

Do not change protected WebView/session, camera/scan, upload/file chooser, download, safe browsing, SSL guard, external URL handling, PrintManager, safe-area, launcher identity, Back/navigation behavior, or orientation during source recovery.

## Public Play Store gate
Before public Google Play release, SANRO POS must also satisfy the mandatory Hybrid/local-first architecture rules in `SANRO_DEVELOPMENT_RULES.md`, including:
- Native Connector/Bridge foundation;
- Room/SQLite local operational database for Free/Starter;
- offline transaction capability for supported local-first mode;
- sync/identity/outbox foundation where applicable;
- Professional+ Hybrid hooks for Perangkat Utama/Kasir;
- device and connection-status architecture;
- backup/recovery requirements appropriate to the plan.

Public release readiness is therefore broader than merely rebuilding the current V1.0.5 WebView wrapper.

## Signing policy
Use a persistent production keystore stored outside GitHub with a secure backup. Never commit keystore, store password, key password, signing token, OAuth secret, or local signing properties. Signing configuration must read secrets from secure local/CI configuration.

## Release QA after implementation is ready
Before release candidate promotion, verify at minimum:
- launcher opens the correct app;
- login/session;
- safe area top/bottom;
- navigation/Back behavior;
- camera/scan;
- file upload/chooser and downloads;
- external URL and SSL failure handling;
- print flow;
- orientation rules;
- web updates load correctly;
- local database migration/health where local-first is active;
- offline transaction behavior for eligible plans;
- sync/idempotency/recovery behavior where applicable.

Only after release-device QA PASS and explicit owner approval may a baseline be raised according to project status rules. **100% means LOCK BASELINE.**
