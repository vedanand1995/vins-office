# Vins Office 📱

WPS Office jaisa Android app — **Word (.docx), Excel (.xlsx), PDF** open aur edit karo, plus **custom fonts** (Shruti, LMG Arun, Mangal) support.

## Features
- 📄 Word documents open/edit/save (Apache POI)
- 📊 Excel spreadsheets with formula support (=SUM, =A1+B2...)
- 📕 PDF viewer + text add/edit (pdfbox-android)
- 🔤 Custom font manager — bundled + user-imported TTF
- 🔁 Unicode ↔ Krutidev (LMG) converter

## Setup
1. Android Studio me open karo (Kotlin, minSdk 24)
2. Fonts `app/src/main/assets/fonts/` me daalo: Shruti.ttf, LMG Arun.ttf, Mangal.ttf
3. Run ▶️

## Roadmap

| Phase | Kaam |
|---|---|
| 1 | Setup, file picker, font manager |
| 2 | Word editor |
| 3 | Excel grid + formulas |
| 4 | PDF viewer + edit |
| 5 | Font import + Krutidev converter |
| 6 | Testing + Play Store build |

> Note: Shruti/Mangal Microsoft fonts hain — redistribution se pehle license check karein.
