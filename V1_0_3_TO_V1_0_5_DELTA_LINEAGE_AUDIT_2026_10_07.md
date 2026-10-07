# SANRO Android — V1.0.3 to V1.0.5 Delta Lineage Audit

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Scope: repository lineage evidence only. No reconstruction, build, Android source mutation, Hosting deploy, or database change.

## Purpose

Record exactly what the repository WORK folders contain so a future FULL V1.0.5 project can be verified against known delta lineage without treating old source as authority.

## WORK_V1_0_3

Recursive tree evidence shows the snapshot contains, for both SanroPOS and SanroStock:

- `CHANGELOG.md`
- `app/build.gradle`
- `app/src/main/java/.../MainActivity.java`

No full root Gradle/settings/wrapper, Manifest, or complete resource tree is present in the WORK_V1_0_3 folder.

## WORK_V1_0_4

Recursive tree evidence shows the same delta structure plus adaptive launcher XML resources:

For SanroPOS:
- `app/build.gradle`
- `app/src/main/java/com/sanrotech/pos/MainActivity.java`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

For SanroStock:
- `app/build.gradle`
- `app/src/main/java/com/sanrotech/stock/MainActivity.java`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

The V1.0.4 folder still is not a complete standalone Android Studio project.

## WORK_V1_0_5

Recursive tree evidence shows:

- `CHANGELOG.md`
- SanroPOS `app/build.gradle`
- SanroPOS `MainActivity.java`
- SanroStock `app/build.gradle`
- SanroStock `MainActivity.java`

The adaptive icon XML visible in the V1.0.4 delta is not copied into the V1.0.5 WORK folder. This does NOT prove the tested V1.0.5 APK lacked the launcher resources; it proves only that WORK folders are incremental/delta snapshots and cannot be interpreted as complete release source.

## Interpretation rule

Do not merge WORK folders mechanically and call the result authoritative V1.0.5.

Reasons:

1. a delta folder may intentionally omit unchanged files;
2. binary launcher assets referenced by XML may have existed only in the actual build package/project and are not proven by these folders;
3. manifests, root Gradle/settings/wrapper, strings/resources and signing-neutral project structure remain absent;
4. the user-tested APK behavior is higher-priority evidence than an inferred reconstruction from historical pieces.

## Safe use of this lineage

When a candidate FULL V1.0.5 project becomes available:

- compare app Gradle metadata with V1.0.5 WORK;
- compare MainActivity behavior with V1.0.5 WORK;
- verify V1.0.4 launcher/adaptive icon behavior is still represented in the full project;
- verify safe-area/orientation changes from V1.0.5 lineage are present;
- verify package IDs and version metadata;
- build unchanged and regression-test against the installed WORK behavior.

If the candidate differs materially, stop and determine whether it is an older/newer source lineage before any Room/Bluetooth/Hybrid work.

## Decision

Repository delta lineage is now documented, but **Android Studio readiness remains NOT READY** until a complete buildable source project matching the user-tested V1.0.5 WORK lineage is available/proven.

## Rollback / deployment impact

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO
- Restart Node.js: NO
- Production deployment: NONE
