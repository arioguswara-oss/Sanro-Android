# SANRO Android — V1.0.5 FULL Source Recovery from Google Drive

Date: 2026-10-07
Status: **SOURCE RECOVERED / ANDROID STUDIO BUILD GATE**
Scope: audit/documentation only. No Android source mutation in GitHub, no Hosting/frontend/backend/database mutation.

## Finding

A connected Google Drive contains a combined folder `SANRO_ANDROID_POS_STOCK_V1_0_5` with separate:

- `SanroPOS_Android_V1_0_5/SanroPOS_Android`
- `SanroStock_Android_V1_0_5/SanroStock_Android`

These are materially more complete than GitHub `WORK_V1_0_5` delta folders.

## SanroPOS full-project evidence

The Drive project contains:

- root `settings.gradle`;
- root `build.gradle`;
- `gradle.properties`;
- `gradlew` and `gradlew.bat`;
- `gradle/wrapper/gradle-wrapper.jar`;
- `gradle/wrapper/gradle-wrapper.properties`;
- `app/build.gradle`;
- `app/proguard-rules.pro`;
- `app/src/main/AndroidManifest.xml`;
- `app/src/main/java/.../MainActivity.java`;
- complete resource tree including drawable, values, xml, adaptive launcher resources and density mipmaps;
- README identifying SanroPOS Android v1.0.5, package `com.sanrotech.pos`, JDK 17 and SDK 35 build instructions;
- an existing `app/release/app-release.apk` plus `output-metadata.json`, proving this stored project had produced a release artifact.

The Drive `app/build.gradle` is 582 bytes and its Git blob hash is `7aeffe9d6303e8c9bac93e130091b6bb3a5be622`, identical to the latest GitHub WORK V1.0.5 SanroPOS `app/build.gradle` blob.

## SanroStock full-project evidence

The Drive project likewise contains:

- root Gradle/settings/properties/wrapper structure;
- `app/build.gradle` and `proguard-rules.pro`;
- `app/src/main/AndroidManifest.xml`;
- Java source and resources;
- README identifying SanroStock Android v1.0.5, package `com.sanrotech.stock`, JDK 17 and SDK 35 build instructions;
- an existing `app/release/app-release.apk` plus `output-metadata.json`.

## Important lineage timing

The Drive full projects were stored/built on 24 September 2026.

The protected phone/tablet orientation WORK was added later on 2 October 2026 in GitHub commits:

- SanroPOS orientation WORK: `e090cb055c40c2ab01414c9a6db45a2ff5e751e8`
- SanroStock orientation WORK: `e00a6b96155a7edf12294c382389a66901063878`

Therefore the Drive full project is the complete V1.0.5 project base, but its `MainActivity.java` predates the later orientation WORK patch.

Do **not** call the 24 September Drive MainActivity the latest protected MainActivity.

## Deterministic recovery rule

The safe candidate for Android Studio is:

1. use the complete Drive V1.0.5 project tree as the full project base;
2. keep its Manifest, resources, wrapper, root Gradle/settings, launcher assets and other complete project files;
3. replace only the app-level files for which a later protected GitHub WORK file is proven newer;
4. for current evidence, use the latest GitHub `WORK_V1_0_5` MainActivity for each app so the 2 October phone/tablet orientation WORK is included;
5. `app/build.gradle` must remain byte/semantic equivalent to protected V1.0.5 metadata (SanroPOS Drive build.gradle already proves exact blob identity);
6. do not introduce Room, Bluetooth, Hybrid routing, refactor, cleanup or other functionality during this recovery/build gate.

This is a deterministic merge of a complete V1.0.5 source base with later exact WORK delta files, not a reconstruction from V1.0.2 and not speculative coding.

## Android Studio gate

The source blocker is now changed from **FULL SOURCE MISSING** to:

**FULL SOURCE RECOVERED — READY FOR UNCHANGED/RECOVERY BUILD VALIDATION IN ANDROID STUDIO.**

Required sequence before Stage B:

1. materialize/download the full Drive projects locally without altering source;
2. apply only the proven later WORK MainActivity orientation delta/current WORK MainActivity files;
3. create FULL Android source checkpoint + rollback from that candidate;
4. Gradle Sync using JDK 17 / SDK 35;
5. clean build both apps;
6. install/update and run protected smoke test;
7. if any WORK behavior regresses, stop and restore the candidate checkpoint;
8. only after clean-build + smoke PASS accept this as the protected full-source baseline for Stage B.

## Still not authorized

This recovery does NOT authorize:

- Room implementation;
- Bluetooth printer implementation;
- local transaction routing;
- Hybrid pairing/sync;
- Play Store release;
- any Hosting production change.

Those begin only in separately checkpointed stages after the unchanged build gate passes.

## Rollback/deployment status

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO (documentation/audit only)
- Restart Node.js: NO
- Production deployment: NONE
