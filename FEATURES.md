# 📊 Vins Office — Excel Feature Checklist

Microsoft Excel (Desktop + Web + Android) ke saare functions/features ki complete list — apni app ke roadmap ke liye.
✅ = implemented | 🔨 = in progress | ⬜ = pending

---

## 1. File Management
- [x] Open Workbook (XLSX)
- [x] Save
- [x] Save As (new file)
- [ ] Create New Workbook
- [ ] Close Workbook
- [ ] Auto Save
- [ ] Save a Copy
- [ ] Rename File
- [ ] Delete File
- [ ] Restore File
- [ ] Import File
- [ ] Export File
- [ ] Share File
- [ ] Download File
- [ ] Print File
- [ ] File Properties
- [ ] Version History
- [ ] Recent Files
- [ ] Pin Files
- [ ] Offline Files
- [ ] File Format Conversion
- [ ] Password Protected File
- [ ] Read-only Mode
- [ ] Recover Unsaved Workbook
- [ ] Workbook Information

**Supported formats:** XLS ⬜ | XLSX ✅ | XLSM ⬜ | XLSB ⬜ | CSV ⬜ | TXT ⬜ | ODS ⬜ | XML ⬜ | PDF Export ⬜

## 2. Worksheet Management
- [x] Add Sheet
- [ ] Delete Sheet
- [x] Rename Sheet
- [ ] Duplicate Sheet
- [ ] Copy Sheet
- [ ] Move Sheet
- [ ] Hide Sheet
- [ ] Unhide Sheet
- [ ] Protect Sheet
- [ ] Unprotect Sheet
- [ ] Change Sheet Tab Color
- [ ] Reorder Sheets
- [ ] Select Multiple Sheets
- [ ] Group Sheets
- [ ] Navigate Between Sheets
- [ ] Sheet Zoom
- [ ] Sheet View
- [ ] Show/Hide Gridlines
- [ ] Show/Hide Headings
- [ ] Set Sheet Background
- [ ] Freeze Sheet Panes
- [ ] Print Sheet
- [ ] Clear Sheet Contents

## 3. Cell & Range Operations
- [x] Select Cell
- [ ] Select Multiple Cells
- [ ] Select Entire Row
- [ ] Select Entire Column
- [ ] Select Entire Sheet
- [ ] Insert Cell
- [ ] Delete Cell
- [x] Insert Row
- [x] Delete Row
- [x] Insert Column
- [x] Delete Column
- [ ] Copy
- [ ] Cut
- [ ] Paste
- [ ] Paste Special
- [ ] Paste Values
- [ ] Paste Formulas
- [ ] Paste Formatting
- [ ] Fill Down
- [ ] Fill Up
- [ ] Fill Right
- [ ] Fill Left
- [ ] Auto Fill
- [ ] Flash Fill
- [ ] Drag & Drop Cells
- [ ] Move Cell
- [x] Clear Contents (cell empty karo)
- [ ] Clear Formatting
- [ ] Clear All
- [ ] Find Cell
- [ ] Replace Cell
- [ ] Go To
- [ ] Go To Special
- [ ] Name Range
- [ ] Named Ranges
- [ ] Merge Cells
- [ ] Unmerge Cells
- [ ] Split Text into Columns
- [ ] Insert Hyperlink
- [ ] Remove Hyperlink

## 4. Text & Number Formatting
- [x] Font Family
- [x] Font Size
- [x] Bold
- [x] Italic
- [x] Underline
- [ ] Double Underline
- [ ] Strikethrough
- [x] Font Color
- [x] Cell Fill Color
- [ ] Background Color
- [ ] Border Color
- [ ] Border Style
- [ ] All Borders
- [ ] Outside Borders
- [ ] Inside Borders
- [ ] Top Border
- [ ] Bottom Border
- [ ] Left Border
- [ ] Right Border
- [x] Horizontal Alignment
- [ ] Vertical Alignment
- [x] Left Align
- [x] Center Align
- [x] Right Align
- [ ] Top Align
- [ ] Middle Align
- [ ] Bottom Align
- [ ] Wrap Text
- [ ] Shrink to Fit
- [ ] Merge & Center
- [ ] Text Rotation
- [ ] Indentation
- [ ] Increase Indent
- [ ] Decrease Indent
- [ ] Format Painter
- [ ] Cell Styles
- [ ] Number Format (General)
- [ ] Currency
- [ ] Accounting
- [ ] Percentage
- [ ] Fraction
- [ ] Scientific
- [ ] Date Format
- [ ] Time Format
- [ ] Custom Number Format
- [ ] Decimal Increase
- [ ] Decimal Decrease
- [ ] Thousands Separator

## 5. Row & Column Functions
- [ ] Adjust Column Width
- [ ] Adjust Row Height
- [ ] AutoFit Column Width
- [ ] AutoFit Row Height
- [ ] Hide Rows
- [ ] Unhide Rows
- [ ] Hide Columns
- [ ] Unhide Columns
- [ ] Group Rows
- [ ] Ungroup Rows
- [ ] Group Columns
- [ ] Ungroup Columns
- [ ] Outline Levels
- [x] Row Insert/Delete (POI se possible — UI pending)
- [x] Column Insert/Delete (POI se possible — UI pending)
- [ ] Resize Multiple Rows
- [ ] Resize Multiple Columns
- [ ] Freeze Top Row
- [ ] Freeze First Column
- [ ] Freeze Panes
- [ ] Split Window

## 6. Formula & Function System

### Mathematical
- [x] SUM (POI formula engine)
- [ ] SUMIF
- [ ] SUMIFS
- [ ] SUMPRODUCT
- [ ] PRODUCT
- [ ] SUBTOTAL
- [ ] AGGREGATE
- [ ] ABS
- [ ] ROUND
- [ ] ROUNDUP
- [ ] ROUNDDOWN
- [ ] INT
- [ ] TRUNC
- [ ] MOD
- [ ] QUOTIENT
- [ ] POWER
- [ ] SQRT
- [ ] CEILING
- [ ] FLOOR
- [ ] RAND
- [ ] RANDBETWEEN
- [ ] MROUND
- [ ] SIGN
- [ ] PI

> Note: POI ka formula evaluator upar ke kaafi functions already support karta hai — UI me formula bar aane pe automatically ✅ ho jayenge.

### Statistical
- [ ] AVERAGE / AVERAGEIF / AVERAGEIFS
- [ ] COUNT / COUNTA / COUNTBLANK
- [ ] COUNTIF / COUNTIFS
- [ ] MAX / MIN
- [ ] MEDIAN / MODE
- [ ] STDEV / STDEV.S / STDEV.P
- [ ] VAR / VAR.S / VAR.P
- [ ] LARGE / SMALL
- [ ] RANK / PERCENTILE / QUARTILE
- [ ] FREQUENCY

### Logical
- [ ] IF / IFS / IFERROR / IFNA
- [ ] AND / OR / NOT / XOR
- [ ] TRUE / FALSE
- [ ] SWITCH / LET

### Text
- [ ] LEFT / RIGHT / MID / LEN / TRIM / CLEAN
- [ ] UPPER / LOWER / PROPER
- [ ] CONCAT / CONCATENATE / TEXTJOIN
- [ ] FIND / SEARCH / REPLACE / SUBSTITUTE / EXACT
- [ ] TEXT / VALUE / REPT / CHAR / CODE / UNICHAR / UNICODE
- [ ] TEXTBEFORE / TEXTAFTER / TEXTSPLIT

### Date & Time
- [ ] TODAY / NOW / DATE / TIME
- [ ] DAY / MONTH / YEAR / HOUR / MINUTE / SECOND
- [ ] WEEKDAY / WEEKNUM / ISOWEEKNUM
- [ ] DAYS / DATEDIF / DAYS360
- [ ] EDATE / EOMONTH
- [ ] WORKDAY / WORKDAY.INTL / NETWORKDAYS / NETWORKDAYS.INTL / YEARFRAC

### Lookup & Reference
- [ ] VLOOKUP / HLOOKUP / XLOOKUP / LOOKUP
- [ ] INDEX / MATCH / XMATCH
- [ ] CHOOSE / OFFSET / INDIRECT / ADDRESS
- [ ] ROW / ROWS / COLUMN / COLUMNS
- [ ] TRANSPOSE
- [ ] FILTER / SORT / SORTBY / UNIQUE
- [ ] TAKE / DROP / CHOOSEROWS / CHOOSECOLS / VSTACK / HSTACK

### Information
- [ ] ISBLANK / ISNUMBER / ISTEXT / ISNONTEXT
- [ ] ISERROR / ISERR / ISNA
- [ ] ISEVEN / ISODD / ISFORMULA / ISLOGICAL
- [ ] TYPE / CELL / INFO / NA / ERROR.TYPE

### Financial
- [ ] PMT / IPMT / PPMT
- [ ] FV / PV / NPV / XNPV
- [ ] IRR / XIRR / RATE / NPER
- [ ] SLN / DB / DDB

## 7. Data Management
- [ ] Sort A to Z
- [ ] Sort Z to A
- [ ] Sort Smallest to Largest
- [ ] Sort Largest to Smallest
- [ ] Custom Sort
- [ ] Multi-level Sort
- [ ] Filter
- [ ] Auto Filter
- [ ] Advanced Filter
- [ ] Clear Filter
- [ ] Reapply Filter
- [ ] Search in Filter
- [ ] Filter by Color
- [ ] Filter by Date
- [ ] Filter by Number
- [ ] Remove Duplicates
- [ ] Data Validation
- [ ] Dropdown List
- [ ] Input Message
- [ ] Error Alert
- [ ] Text to Columns
- [ ] Flash Fill
- [ ] Consolidate Data
- [ ] Subtotal
- [ ] Group Data / Ungroup Data
- [ ] Data Outline
- [ ] What-if Analysis
- [ ] Goal Seek
- [ ] Scenario Manager
- [ ] Data Table

## 8. Tables
- [ ] Create Table
- [ ] Convert Range to Table
- [ ] Table Design / Table Name
- [ ] Header Row / Total Row
- [ ] Banded Rows / Banded Columns
- [ ] First/Last Column Highlight
- [ ] Table Styles
- [ ] Resize / Expand Table
- [ ] Remove Duplicates
- [ ] Convert Table to Range
- [ ] Table Filters / Table Sorting
- [ ] Structured References
- [ ] Insert/Delete Table Row

## 9. Charts & Graphs
- [ ] Column Chart
- [ ] Bar Chart
- [ ] Line Chart
- [ ] Pie Chart
- [ ] Doughnut Chart
- [ ] Area Chart
- [ ] Scatter Chart
- [ ] Bubble Chart
- [ ] Radar Chart
- [ ] Surface Chart
- [ ] Stock Chart
- [ ] Combo Chart
- [ ] Histogram
- [ ] Pareto Chart
- [ ] Waterfall Chart
- [ ] Funnel Chart
- [ ] Treemap
- [ ] Sunburst
- [ ] Map Chart
- [ ] Pivot Chart
- [ ] Chart Title / Axis Titles / Legend / Data Labels
- [ ] Chart Styles / Colors
- [ ] Move / Resize / Copy / Delete Chart
- [ ] Switch Row/Column
- [ ] Select Data Source
- [ ] Trendline / Error Bars

## 10. Conditional Formatting
- [ ] Highlight Cell Rules (Greater Than, Less Than, Between, Equal To)
- [ ] Text Contains
- [ ] Duplicate Values / Unique Values
- [ ] Date Occurring
- [ ] Top 10 / Bottom 10 Items
- [ ] Above / Below Average
- [ ] Data Bars
- [ ] Color Scales
- [ ] Icon Sets
- [ ] Formula-Based Formatting
- [ ] Manage / Edit / Delete / Clear Rules

## 11. Pivot Table
- [ ] Create Pivot Table
- [ ] Pivot Fields (Rows / Columns / Values / Filters)
- [ ] Aggregations (Sum / Count / Average / Max / Min / Product)
- [ ] Percentage of Grand Total
- [ ] Group Dates / Group Numbers / Ungroup
- [ ] Sort / Filter Pivot Data
- [ ] Slicers / Timelines
- [ ] Refresh / Refresh All
- [ ] Change Data Source
- [ ] Calculated Field
- [ ] Expand/Collapse / Show Details
- [ ] Pivot Chart

## 12. View & Navigation
- [x] Status Bar (status text)
- [ ] Normal View
- [ ] Page Layout View
- [ ] Page Break Preview
- [ ] Zoom In / Out / Percentage / 100%
- [ ] Freeze Panes
- [ ] Split Window
- [ ] New Window / Arrange All / Switch Windows
- [ ] Full Screen
- [x] Formula Bar
- [ ] Gridlines / Headings toggle
- [ ] Navigation Pane
- [ ] Search Box / Name Box

## 13. Review & Protection
- [ ] Spelling Check
- [ ] Translate
- [ ] Comments (New / Edit / Delete / Reply / Resolve / Show)
- [ ] Notes (New / Edit / Delete)
- [ ] Track Changes
- [ ] Protect Workbook / Protect Sheet
- [ ] Lock / Unlock Cells
- [ ] Allow Edit Ranges
- [ ] Password Protection
- [ ] Mark as Final
- [ ] Accessibility Check

## 14. Printing & Page Setup
- [ ] Print Preview / Print
- [ ] Print Selection / Active Sheet / Entire Workbook
- [ ] Page Size / Orientation (Portrait / Landscape)
- [ ] Margins / Custom Margins
- [ ] Scaling / Fit to Page / Fit All Columns on One Page
- [ ] Print Area (Set / Clear)
- [ ] Page Breaks (Insert / Remove)
- [ ] Repeat Header Rows
- [ ] Header / Footer / Page Number / Date & Time
- [ ] Print Gridlines / Print Headings
- [ ] Center on Page

## 15. Collaboration & Sharing
- [ ] Share Workbook / Invite People
- [ ] Copy Sharing Link
- [ ] Manage Access / Permissions
- [ ] Real-time Collaboration / Co-authoring
- [ ] Comments / @Mentions
- [ ] Version History / Restore Previous Version
- [ ] Sync Changes / Conflict Resolution

## 16. Advanced Features
- [ ] Power Query (Get Data, Import CSV/Text/Web/DB)
- [ ] Data Transformation / Merge / Append Queries
- [ ] Power Pivot / Data Model / Relationships / DAX
- [ ] What-if Analysis / Goal Seek / Solver
- [ ] Macros / VBA Editor / Record / Run / Edit
- [ ] Office Scripts
- [ ] External Data Connections / Refresh Data
- [ ] XML Mapping
- [ ] Developer Tab
- [ ] Form Controls / ActiveX Controls

## 17. Keyboard Shortcuts
- [ ] Ctrl+C / Ctrl+V / Ctrl+X
- [ ] Ctrl+Z / Ctrl+Y
- [ ] Ctrl+S / Ctrl+P
- [ ] Ctrl+F / Ctrl+H
- [ ] Ctrl+A / Ctrl+B / Ctrl+I / Ctrl+U
- [ ] Ctrl+1 / Ctrl+N / Ctrl+O / Ctrl+W
- [ ] F2 / F4
- [ ] Alt+Enter
- [ ] Ctrl+; / Ctrl+Shift+;
- [ ] Ctrl+Space / Shift+Space
- [ ] Ctrl+Home / Ctrl+End / Ctrl+Arrow

## 18. Android Mobile-Specific
- [x] Touch-based Cell Selection
- [ ] Long Press Menu
- [x] Mobile Formula Bar
- [ ] Custom Mobile Keyboard
- [ ] Formula Suggestions
- [ ] Touch Drag & Drop
- [ ] Row/Column Resize by Touch
- [x] Sheet Switching
- [ ] Pinch to Zoom
- [ ] Mobile Ribbon
- [ ] Floating Formatting Toolbar
- [ ] Undo/Redo
- [x] Open from File Manager
- [x] Save to Device
- [ ] Save to Cloud (OneDrive/Drive)
- [ ] Offline Editing
- [ ] Android Print
- [ ] PDF Export
- [ ] Mobile Search / Filter / Sort
- [ ] Chart Viewing / Editing
- [ ] Dark Mode Support
- [ ] External Keyboard Support
- [ ] Copy/Paste from Other Apps
- [x] Open Recent Files (file picker)
- [ ] File Sharing
- [ ] Multi-window Support

## 19. Viewer (Read-only) Functions
- [x] XLSX File Open
- [x] Multiple Sheet Viewing (Sheet1)
- [x] Cell Value Display
- [x] Formula Result Display (evaluateAll)
- [x] Row Number Display / Column Letters
- [ ] CSV File Open
- [ ] Workbook Preview
- [x] Sheet Switching
- [x] Formula Bar
- [x] Gridlines
- [ ] Zoom In/Out
- [ ] Freeze Panes
- [ ] Hidden Rows/Columns Handling
- [ ] Merged Cell Display
- [ ] Cell Formatting Display (Font / Color / Border / Alignment)
- [ ] Number / Date Formatting
- [ ] Hyperlink Opening
- [ ] Search / Find Next / Find Previous
- [ ] Filter / Sort Viewing
- [ ] Chart Viewing
- [ ] Image Viewing
- [ ] Print Preview
- [ ] Export to PDF
- [ ] Share File
- [ ] Copy Cell Value / Row / Column
- [ ] Select Range
- [ ] Error Value Display

## 20. Editor — Essential Editing
- [x] Edit Cell
- [x] Add / Delete Data
- [x] Formula Input (=SUM etc. — POI engine)
- [x] Save Changes / Save As
- [ ] Edit Formula (formula bar)
- [ ] Insert/Delete Row & Column (UI)
- [ ] Copy/Paste / Cut/Paste
- [ ] Undo / Redo
- [ ] Format Cells
- [ ] Font / Font Size / Bold / Italic / Underline
- [x] Font Color / Cell Color
- [ ] Borders
- [ ] Merge Cells
- [ ] Wrap Text
- [ ] Alignment
- [ ] Number Format
- [ ] Formula AutoComplete
- [ ] AutoSum
- [ ] Find & Replace
- [ ] Sort / Filter
- [ ] Data Validation / Dropdown
- [ ] Conditional Formatting
- [ ] Create Charts
- [ ] Add/Delete/Rename Sheets
- [ ] Freeze Panes
- [ ] Protect Sheet
- [ ] Export PDF

---

## 🎯 Priority Levels

| Level | Features | Status |
|---|---|---|
| **Basic** (MVP) | Viewer, Cell Editing, Sheets, Basic Formatting | ✅ Ho gaya |
| **Advanced** | Formula Bar, Sort, Filter, Charts, Formatting UI, PDF Export | 🔨 Next |
| **Professional** | Pivot Table, Power Query, Macros, Collaboration | ⬜ Baad me |
