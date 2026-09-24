# SANRO Android V1.0.3

Status: WORK / build-fix candidate.

## Fixes
- Correct Android Print API import from `android.app.PrintManager` to `android.print.PrintManager`.
- Correct Safe Browsing call to `WebSettings#setSafeBrowsingEnabled(true)` for API 26+.
- Bump `versionCode` to 3 and `versionName` to 1.0.3 for both apps.

## Scope
No URL, package ID, WebView navigation policy, login/session behavior, camera permission flow, download handling, or app identity changes.

## Target
- SanroPOS -> https://pos.sanrotech.com/sanro-pos
- SanroStock -> https://pos.sanrotech.com/sanro-stock

## QA status
Static source correction completed. Real Android Studio build/device test is pending and must pass before LOCKED.
