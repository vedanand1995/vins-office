package com.vedanand.vinsoffice

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class ExcelEditorActivity : AppCompatActivity() {

    private lateinit var grid: TableLayout
    private lateinit var cellRef: TextView
    private lateinit var formulaBar: EditText
    private lateinit var sheetTabs: LinearLayout
    private lateinit var zoomLabel: TextView
    private lateinit var statusText: TextView

    private var workbook: Workbook? = null
    private var sheet: Sheet? = null
    private var file: File? = null
    private var currentSheetIndex = 0

    private var selRow = 0
    private var selCol = 0
    private var baseTextSize = 13f

    private val cellViews = mutableMapOf<String, EditText>()
    private val undoStack = ArrayDeque<Triple<Int, Int, String>>()
    private val redoStack = ArrayDeque<Triple<Int, Int, String>>()

    private val rows = 40
    private val cols = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excel_editor)
        grid = findViewById(R.id.grid)
        cellRef = findViewById(R.id.cellRef)
        formulaBar = findViewById(R.id.formulaBar)
        sheetTabs = findViewById(R.id.sheetTabs)
        zoomLabel = findViewById(R.id.btnZoom)
        statusText = findViewById(R.id.statusText)

        try {
            setupTopBar()
            setupToolbar()

            formulaBar.setOnEditorActionListener { _, action, _ ->
                if (action == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    pushUndo(selRow, selCol)
                    applyFormulaBar()
                    true
                } else false
            }

            // Turant empty grid — white screen kabhi na aaye
            workbook = XSSFWorkbook()
            sheet = workbook!!.createSheet("Sheet1")
            renderAll()

            intent.getStringExtra("path")?.let {
                file = File(it)
                loadWorkbook(file!!)
            }
        } catch (t: Throwable) {
            statusText.text = "Init error: ${t.javaClass.simpleName}: ${t.message}"
        }
    }

    private fun setStatus(msg: String) {
        runOnUiThread { statusText.text = msg }
    }

    // ---------- Top bar ----------

    private fun setupTopBar() {
        findViewById<TextView>(R.id.btnDone).setOnClickListener {
            save()
            Toast.makeText(this, "Done ✅", Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.btnSaveTop).setOnClickListener { save() }
        findViewById<TextView>(R.id.btnUndo).setOnClickListener { undo() }
        findViewById<TextView>(R.id.btnRedo).setOnClickListener { redo() }
        findViewById<TextView>(R.id.btnClose).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnZoom).setOnClickListener {
            baseTextSize = when (baseTextSize) {
                11f -> 13f; 13f -> 16f; 16f -> 20f; else -> 11f
            }
            zoomLabel.text = "${(baseTextSize / 13f * 100).toInt()}%"
            renderGrid()
        }
    }

    private fun undo() {
        val last = undoStack.removeLastOrNull() ?: return
        val (r, c, old) = last
        redoStack.addLast(Triple(r, c, cellText(getCell(r, c))))
        setCell(r, c, old)
        refreshCellView(r, c)
    }

    private fun redo() {
        val last = redoStack.removeLastOrNull() ?: return
        val (r, c, v) = last
        undoStack.addLast(Triple(r, c, cellText(getCell(r, c))))
        setCell(r, c, v)
        refreshCellView(r, c)
    }

    private fun pushUndo(r: Int, c: Int) {
        undoStack.addLast(Triple(r, c, cellText(getCell(r, c))))
        if (undoStack.size > 100) undoStack.removeFirst()
        redoStack.clear()
    }

    // ---------- Bottom toolbar ----------

    private fun setupToolbar() {
        fun t(id: String) = findViewById<TextView>(resources.getIdentifier(id, "id", packageName))
        t("btnBold").setOnClickListener { toggleFontStyle { it.bold = !it.bold } }
        t("btnItalic").setOnClickListener { toggleFontStyle { it.italic = !it.italic } }
        t("btnUnderline").setOnClickListener { toggleFontStyle {
            it.underline = if (it.underline == Font.U_SINGLE) Font.U_NONE else Font.U_SINGLE
        } }
        t("btnTextColor").setOnClickListener { pickColor("Font Color") { argb ->
            styleSelectedCell { (font, _) -> font.color = rgbToPoi(argb) }
        } }
        t("btnFillColor").setOnClickListener { pickColor("Fill Color") { argb ->
            styleSelectedCell { (_, style) ->
                style.fillPattern = FillPatternType.SOLID_FOREGROUND
                style.fillForegroundColor = rgbToPoi(argb)
            }
        } }
        t("btnClear").setOnClickListener {
            pushUndo(selRow, selCol)
            val cell = getCell(selRow, selCol)
            cell.cellType = CellType.BLANK
            refreshCellView(selRow, selCol)
        }
        t("btnMerge").setOnClickListener { mergeSelected() }
        t("btnWrap").setOnClickListener {
            styleSelectedCell { (_, style) -> style.wrapText = !style.wrapText }
        }
        t("btnInsertRow").setOnClickListener { pushUndoAll(); sheet?.shiftRows(selRow, sheet!!.lastRowNum, 1); renderAll() }
        t("btnDeleteRow").setOnClickListener { deleteRow() }
        t("btnInsertCol").setOnClickListener { sheet?.shiftColumns(selCol, (sheet!!.getRow(0)?.lastCellNum?.toInt() ?: cols), 1); renderAll() }
        t("btnDeleteCol").setOnClickListener { deleteCol() }
        t("btnMore").setOnClickListener { showInsertMenu() }
    }

    private fun pushUndoAll() { /* row/col ops undo-simple: skip */ }

    private fun deleteRow() {
        val sh = sheet ?: return
        if (selRow <= sh.lastRowNum) {
            sh.removeRow(sh.getRow(selRow))
            if (selRow + 1 <= sh.lastRowNum) sh.shiftRows(selRow + 1, sh.lastRowNum, -1)
        }
        renderAll()
    }

    private fun deleteCol() {
        val sh = sheet ?: return
        val last = (sh.getRow(0)?.lastCellNum?.toInt() ?: 0) - 1
        if (selCol <= last) sh.shiftColumns(selCol + 1, last, -1)
        renderAll()
    }

    private fun showInsertMenu() {
        val options = arrayOf("🅰 Font Size +", "🅰 Font Size -", "⬅ Align Left", "⬌ Align Center", "➡ Align Right",
            "📄 Add Sheet", "✏️ Rename Sheet")
        AlertDialog.Builder(this).setTitle("More Actions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> styleSelectedCell { (font, _) -> font.fontHeightInPoints = (font.fontHeightInPoints + 2).coerceAtMost(72).toShort() }
                    1 -> styleSelectedCell { (font, _) -> font.fontHeightInPoints = (font.fontHeightInPoints - 2).coerceAtLeast(6).toShort() }
                    2 -> setAlignment(HorizontalAlignment.LEFT)
                    3 -> setAlignment(HorizontalAlignment.CENTER)
                    4 -> setAlignment(HorizontalAlignment.RIGHT)
                    5 -> addSheet()
                    6 -> renameSheet()
                }
            }.show()
    }

    private fun pickColor(title: String, apply: (Int) -> Unit) {
        val names = arrayOf("Black", "Red", "Blue", "Green", "Yellow", "Orange", "White")
        val colors = intArrayOf(
            0xFF000000.toInt(), 0xFFE53935.toInt(), 0xFF1E88E5.toInt(), 0xFF43A047.toInt(),
            0xFFFDD835.toInt(), 0xFFFB8C00.toInt(), 0xFFFFFFFF.toInt())
        AlertDialog.Builder(this).setTitle(title)
            .setItems(names) { _, which -> apply(colors[which]) }.show()
    }

    /** XSSF extended color set karna — indexed color se approximate */
    private fun rgbToPoi(argb: Int): Short {
        val rgb = argb and 0xFFFFFF
        val nearest = mapOf(
            0x000000 to 0, 0xFFFFFF to 1, 0xFF0000 to 10, 0x00FF00 to 11,
            0x0000FF to 12, 0xFFFF00 to 13, 0xFF00FF to 14, 0x00FFFF to 15,
            0xE53935 to 10, 0x1E88E5 to 12, 0x43A047 to 11, 0xFDD835 to 13, 0xFB8C00 to 13
        )
        var best = 0; var bestDist = Int.MAX_VALUE
        for ((k, v) in nearest) {
            val d = colorDist(rgb, k)
            if (d < bestDist) { bestDist = d; best = v }
        }
        return best.toShort()
    }

    private fun colorDist(a: Int, b: Int): Int {
        val r = ((a shr 16) and 0xFF) - ((b shr 16) and 0xFF)
        val g = ((a shr 8) and 0xFF) - ((b shr 8) and 0xFF)
        val bl = (a and 0xFF) - (b and 0xFF)
        return r * r + g * g + bl * bl
    }

    // ---------- Styles ----------

    private fun styleSelectedCell(modify: (Pair<Font, CellStyle>) -> Unit) {
        val wb = workbook ?: return
        val cell = getCell(selRow, selCol)
        val oldStyle = cell.cellStyle
        val oldFont = wb.getFontAt(oldStyle.fontIndexAsInt)
        val newFont = wb.createFont()
        newFont.bold = oldFont.bold
        newFont.italic = oldFont.italic
        newFont.underline = oldFont.underline
        newFont.fontHeightInPoints = oldFont.fontHeightInPoints
        newFont.fontName = oldFont.fontName
        newFont.color = oldFont.color
        val newStyle = wb.createCellStyle()
        newStyle.cloneStyleFrom(oldStyle)
        modify(Pair(newFont, newStyle))
        newStyle.setFont(newFont)
        cell.cellStyle = newStyle
        refreshCellView(selRow, selCol)
    }

    private fun toggleFontStyle(mod: (Font) -> Unit) = styleSelectedCell { (font, _) -> mod(font) }

    private fun setAlignment(align: HorizontalAlignment) =
        styleSelectedCell { (_, style) -> style.alignment = align }

    private fun mergeSelected() {
        val sh = sheet ?: return
        try {
            sh.addMergedRegion(CellRangeAddress(selRow, selRow, selCol, selCol + 1))
            renderGrid()
            Toast.makeText(this, "Merged", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Merge failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- Sheets ----------

    private fun addSheet() {
        val wb = workbook ?: return
        wb.createSheet("Sheet${wb.numberOfSheets + 1}")
        currentSheetIndex = wb.numberOfSheets - 1
        sheet = wb.getSheetAt(currentSheetIndex)
        renderAll()
    }

    private fun renameSheet() {
        val et = EditText(this)
        et.setText(sheet?.sheetName ?: "")
        AlertDialog.Builder(this).setTitle("Rename Sheet").setView(et)
            .setPositiveButton("OK") { _, _ ->
                workbook?.setSheetName(currentSheetIndex, et.text.toString().ifBlank { "Sheet1" })
                renderAll()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun renderSheetTabs() {
        sheetTabs.removeAllViews()
        val wb = workbook ?: return
        for (i in 0 until wb.numberOfSheets) {
            val b = TextView(this)
            b.text = wb.getSheetName(i)
            b.textSize = 12f
            b.setPadding(20, 8, 20, 8)
            if (i == currentSheetIndex) {
                b.background = GradientDrawable().apply { setColor(Color.parseColor("#1976D2")); cornerRadius = 12f }
                b.setTextColor(Color.WHITE)
            } else {
                b.setTextColor(Color.parseColor("#37474F"))
            }
            b.setOnClickListener {
                currentSheetIndex = i
                sheet = wb.getSheetAt(i)
                renderAll()
            }
            sheetTabs.addView(b)
        }
    }

    // ---------- Load / Save ----------

    private fun loadWorkbook(f: File) = CoroutineScope(Dispatchers.IO).launch {
        try {
            setStatus("Loading ${f.name}...")
            val wb = f.inputStream().use { WorkbookFactory.create(it) }
            workbook = wb
            sheet = wb.getSheetAt(0)
            withContext(Dispatchers.Main) {
                renderAll()
                setStatus("✅ ${f.name} — ${sheet?.physicalNumberOfRows ?: 0} rows loaded")
            }
        } catch (t: Throwable) {
            withContext(Dispatchers.Main) {
                setStatus("❌ Load error: ${t.javaClass.simpleName}: ${t.message}")
                Toast.makeText(this@ExcelEditorActivity,
                    "Load failed: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun save() = CoroutineScope(Dispatchers.IO).launch {
        val f = file ?: File(getExternalFilesDir(null), "new_sheet.xlsx").also { file = it }
        try {
            workbook?.creationHelper?.createFormulaEvaluator()?.evaluateAll()
            f.outputStream().use { workbook?.write(it) }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExcelEditorActivity, "Saved ✅", Toast.LENGTH_SHORT).show()
            }
        } catch (t: Throwable) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExcelEditorActivity, "Save failed: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ---------- Grid ----------

    private fun renderAll() {
        try {
            renderSheetTabs()
            renderGrid()
            setStatus("Grid rendered: ${rows} rows × ${cols} cols")
        } catch (t: Throwable) {
            setStatus("❌ Render error: ${t.javaClass.simpleName}: ${t.message}")
        }
    }

    private fun renderGrid() {
        grid.removeAllViews()
        cellViews.clear()
        val sh = sheet

        val header = TableRow(this)
        header.addView(headerCell(""))
        for (c in 0 until cols) header.addView(headerCell(cellName(c)))
        grid.addView(header)

        for (r in 0 until rows) {
            val row = sh?.getRow(r)
            val tr = TableRow(this)
            tr.addView(headerCell((r + 1).toString()))

            for (c in 0 until cols) {
                val cell = row?.getCell(c)
                val et = EditText(this)
                et.setSingleLine(true)
                et.textSize = baseTextSize
                et.setPadding(10, 8, 10, 8)
                et.minWidth = px(100)
                et.gravity = Gravity.CENTER_VERTICAL
                et.setTextColor(Color.BLACK)
                et.hint = ""
                et.background = borderedBg(Color.WHITE)
                et.setText(cellText(cell))
                applyCellStyleToView(et, cell)
                et.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        pushUndo(r, c)
                        setCell(r, c, et.text.toString())
                    } else {
                        selRow = r; selCol = c
                        updateFormulaBar(r, c)
                    }
                }
                et.setOnClickListener {
                    selRow = r; selCol = c
                    updateFormulaBar(r, c)
                    for ((_, v) in cellViews) v.background = borderedBg(Color.WHITE)
                    et.background = borderedBg(Color.parseColor("#BBDEFB"))
                }
                cellViews["${r},${c}"] = et
                tr.addView(et)
            }
            grid.addView(tr)
        }
    }

    private fun borderedBg(fill: Int): GradientDrawable = GradientDrawable().apply {
        setColor(fill)
        setStroke(px(1), Color.parseColor("#C9C9C9"))
    }

    private fun applyCellStyleToView(et: EditText, cell: Cell?) {
        cell ?: return
        try {
            val style = cell.cellStyle
            val font = workbook?.getFontAt(style.fontIndexAsInt) ?: return
            var t = 0
            if (font.bold) t = t or Typeface.BOLD
            if (font.italic) t = t or Typeface.ITALIC
            // Custom font render (Shruti, LMG Arun, Mangal, etc.)
            val customTf = FontManager.load(this, font.fontName)
            et.setTypeface(customTf, t)
            et.setTextColor(fontColorArgb(font))
            et.textSize = baseTextSize * (font.fontHeightInPoints / 11f)
            if (style.alignment == HorizontalAlignment.CENTER) et.gravity = Gravity.CENTER
            else if (style.alignment == HorizontalAlignment.RIGHT) et.gravity = Gravity.END
            else et.gravity = Gravity.START or Gravity.CENTER_VERTICAL

            var fill = Color.WHITE
            if (style.fillPattern == FillPatternType.SOLID_FOREGROUND) {
                fill = xssfRgb(style.fillForegroundColorColor)
                    ?: indexedToArgb(style.fillForegroundColor)
            }

            val hasBorder = style.borderTop != org.apache.poi.ss.usermodel.BorderStyle.NONE ||
                style.borderBottom != org.apache.poi.ss.usermodel.BorderStyle.NONE ||
                style.borderLeft != org.apache.poi.ss.usermodel.BorderStyle.NONE ||
                style.borderRight != org.apache.poi.ss.usermodel.BorderStyle.NONE
            val borderColor = if (hasBorder) Color.parseColor("#555555") else Color.parseColor("#C9C9C9")
            val strokeW = if (hasBorder) 2 else 1

            et.background = GradientDrawable().apply {
                setColor(fill)
                setStroke(px(strokeW), borderColor)
            }
        } catch (_: Exception) {}
    }

    private fun xssfRgb(color: Any?): Int? = try {
        val c = color as? org.apache.poi.xssf.usermodel.XSSFColor ?: return null
        val rgb = c.rgb ?: return null
        if (rgb.size >= 3)
            (0xFF shl 24) or (rgb[0].toInt() and 0xFF shl 16) or (rgb[1].toInt() and 0xFF shl 8) or (rgb[2].toInt() and 0xFF)
        else null
    } catch (_: Exception) { null }

    private fun indexedToArgb(idx: Short): Int {
        val i = idx.toInt()
        val palette = mapOf(
            0 to 0xFF000000.toInt(), 1 to 0xFFFFFFFF.toInt(), 2 to 0xFFFF0000.toInt(),
            3 to 0xFF00FF00.toInt(), 4 to 0xFF0000FF.toInt(), 5 to 0xFFFFFF00.toInt(),
            6 to 0xFFFF00FF.toInt(), 7 to 0xFF00FFFF.toInt(), 16 to 0xFF800000.toInt(),
            17 to 0xFF008000.toInt(), 18 to 0xFF000080.toInt(), 19 to 0xFF808000.toInt(),
            20 to 0xFF800080.toInt(), 21 to 0xFF008080.toInt(), 22 to 0xFFC0C0C0.toInt(),
            23 to 0xFF808080.toInt(), 24 to 0xFF9999FF.toInt(), 40 to 0xFFCCFFFF.toInt(),
            41 to 0xFFCCFFCC.toInt(), 42 to 0xFFFFFF99.toInt(), 43 to 0xFFFFCC99.toInt(),
            44 to 0xFFCC99FF.toInt(), 45 to 0xFFFF99CC.toInt(), 46 to 0xFF99CCFF.toInt(),
            47 to 0xFFFFCCFF.toInt()
        )
        return palette[i] ?: Color.WHITE
    }

    private fun fontColorArgb(font: Font): Int = try {
        xssfRgb((font as? org.apache.poi.xssf.usermodel.XSSFFont)?.xssfColor)
            ?: indexedToArgb(font.color)
    } catch (_: Exception) { Color.BLACK }

    private fun refreshCellView(r: Int, c: Int) {
        val et = cellViews["${r},${c}"] ?: return
        val cell = getCell(r, c)
        et.setText(cellText(cell))
        applyCellStyleToView(et, cell)
    }

    private fun updateFormulaBar(r: Int, c: Int) {
        cellRef.text = cellName(c) + (r + 1)
        formulaBar.setText(cellText(getCell(r, c)))
    }

    private fun applyFormulaBar() {
        setCell(selRow, selCol, formulaBar.text.toString())
        refreshCellView(selRow, selCol)
    }

    private fun cellText(cell: Cell?): String {
        cell ?: return ""
        return when (cell.cellType) {
            CellType.FORMULA -> try { "=" + cell.cellFormula } catch (e: Exception) { cell.toString() }
            CellType.NUMERIC -> {
                val v = cell.numericCellValue
                if (v == Math.floor(v) && !v.isInfinite()) v.toLong().toString() else v.toString()
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> cell.stringCellValue ?: ""
        }
    }

    private fun headerCell(text: String): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 12f
        tv.setTypeface(null, Typeface.BOLD)
        tv.setPadding(16, 10, 16, 10)
        tv.gravity = Gravity.CENTER
        tv.minWidth = px(44)
        tv.setTextColor(Color.WHITE)
        tv.background = GradientDrawable().apply { setColor(Color.parseColor("#1976D2")) }
        return tv
    }

    private fun cellName(c: Int): String {
        var n = c
        val sb = StringBuilder()
        while (n >= 0) {
            sb.insert(0, ('A' + n % 26))
            n = n / 26 - 1
        }
        return sb.toString()
    }

    private fun getCell(r: Int, c: Int): Cell {
        val sh = sheet ?: throw IllegalStateException("No sheet")
        val row = sh.getRow(r) ?: sh.createRow(r)
        return row.getCell(c) ?: row.createCell(c)
    }

    private fun setCell(r: Int, c: Int, value: String) {
        val cell = getCell(r, c)
        if (value.startsWith("=")) {
            cell.cellFormula = value.removePrefix("=")
            cell.cellType = CellType.FORMULA
        } else if (value.toDoubleOrNull() != null) {
            cell.cellType = CellType.NUMERIC
            cell.setCellValue(value.toDouble())
        } else {
            cell.cellType = CellType.STRING
            cell.setCellValue(value)
        }
    }

    private fun px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
