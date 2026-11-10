# Vins Office 📱

WPS Office jaisa Android app — **Word (.docx), Excel (.xlsx), PDF** open aur edit karo, plus **custom fonts** (Shruti, LMG Arun, Mangal) support.

## Features
- 📄 Word documents open/edit/save (Apache POI)
- 📊 Excel spreadsheets with formula support (=SUM, =A1+B2...), grid UI with headers
- 📕 PDF viewer + text add/edit (pdfbox-android)
- 🔤 Custom font manager — bundled + user-imported TTF
- 🔁 Unicode ↔ Krutidev (LMG) converter

## Full Feature Checklist
👉 [FEATURES.md](FEATURES.md) — Excel ke saare functions/features ki complete roadmap

## Setup
1. Android Studio me open karo (Kotlin, minSdk 26)
2. Fonts `app/src/main/assets/fonts/` me daalo: Shruti.ttf, LMG Arun.ttf, Mangal.ttf
3. Run ▶️ ya GitHub Actions se APK banao

## Build
- GitHub Actions: Actions tab → "Build Debug APK" → Run workflow → Artifacts me APK
- Local: `./gradlew assembleDebug`

> Note: Shruti/Mangal Microsoft fonts hain — redistribution se pehle license check karein.
