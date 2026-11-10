package com.vedanand.vinsoffice

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class ExcelEditorActivity : AppCompatActivity() {

    private lateinit var grid: TableLayout
    private lateinit var statusText: TextView
    private lateinit var cellRef: TextView
    private lateinit var formulaBar: EditText
    private lateinit var sheetTabs: LinearLayout

    private var workbook: Workbook? = null
    private var sheet: Sheet? = null
    private var file: File? = null
    private var currentSheetIndex = 0

    // Currently selected cell
    private var selRow = 0
    private var selCol = 0

    // Cell views for refresh
    private val cellViews = mutableMapOf<String, EditText>()

    private val rows = 30
    private val cols = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excel_editor)
        grid = findViewById(R.id.grid)
        statusText = findViewById(R.id.statusText)
        cellRef = findViewById(R.id.cellRef)
        formulaBar = findViewById(R.id.formulaBar)
        sheetTabs = findViewById(R.id.sheetTabs)

        setupToolbar()

        formulaBar.setOnEditorActionListener { _, action, _ ->
            if (action == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                applyFormulaBar()
                true
            } else false
        }

        intent.getStringExtra("path")?.let {
            file = File(it)
            loadWorkbook(file!!)
        } ?: run {
            workbook = XSSFWorkbook()
            sheet = workbook!!.createSheet("Sheet1")
            statusText.text = "New sheet (empty)"
            renderAll()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        findViewById<Button>(R.id.btnAddSheet).setOnClickListener { addSheet() }
        findViewById<Button>(R.id.btnRenameSheet).setOnClickListener { renameSheet() }
    }

    // ---------- Toolbar ----------

    private fun setupToolbar() {
        findViewById<Button>(R.id.btnBold).setOnClickListener { toggleFontStyle { it.bold = !it.bold } }
        findViewById<Button>(R.id.btnItalic).setOnClickListener { toggleFontStyle { it.italic = !it.italic } }
        findViewById<Button>(R.id.btnUnderline).setOnClickListener { toggleFontStyle {
            it.underline = (if (it.underline.toInt() == Font.U_SINGLE) Font.U_NONE else Font.U_SINGLE).toShort()
        } }
        findViewById<Button>(R.id.btnFontPlus).setOnClickListener { changeFontSize(2) }
        findViewById<Button>(R.id.btnFontMinus).setOnClickListener { changeFontSize(-2) }
        findViewById<Button>(R.id.btnAlignLeft).setOnClickListener { setAlignment(HorizontalAlignment.LEFT) }
        findViewById<Button>(R.id.btnAlignCenter).setOnClickListener { setAlignment(HorizontalAlignment.CENTER) }
        findViewById<Button>(R.id.btnAlignRight).setOnClickListener { setAlignment(HorizontalAlignment.RIGHT) }
        findViewById<Button>(R.id.btnFillColor).setOnClickListener { applyFill() }
        findViewById<Button>(R.id.btnTextColor).setOnClickListener { applyFontColor() }
        findViewById<Button>(R.id.btnInsertRow).setOnClickListener { insertRow() }
        findViewById<Button>(R.id.btnDeleteRow).setOnClickListener { deleteRow() }
        findViewById<Button>(R.id.btnInsertCol).setOnClickListener { insertCol() }
        findViewById<Button>(R.id.btnDeleteCol).setOnClickListener { deleteCol() }
    }

    /** POI style objects are workbook-level: clone font + style for the selected cell */
    private fun styleSelectedCell(modify: (Pair<Font, CellStyle>) -> Unit) {
        val wb = workbook ?: return
        val cell = getCell(selRow, selCol)
        val oldStyle = cell.cellStyle
        val oldFont = wb.getFontAt(oldStyle.fontIndexAsInt)
        val newFont = wb.createFont()
        newFont.bold = oldFont.bold
        newFont.italic = oldFont.italic
        newFont.underline = oldFont.underline.toShort()
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

    private fun toggleFontStyle(mod: (Font) -> Unit) =
        styleSelectedCell { (font, _) -> mod(font) }

    private fun changeFontSize(delta: Int) = styleSelectedCell { (font, _) ->
        font.fontHeightInPoints = ((font.fontHeightInPoints + delta).coerceIn(6, 72))
    }

    private fun applyFontColor() {
        val colors = listOf(Color.BLACK, Color.RED, 0xFF0000FF.toInt(), 0xFF008000.toInt())
        val names = listOf("Black", "Red", "Blue", "Green")
        AlertDialog.Builder(this).setTitle("Font Color")
            .setItems(names.toTypedArray()) { _, which ->
                val c = colors[which]
                styleSelectedCell { (font, _) ->
                    font.setColor(org.apache.poi.ss.usermodel.IndexedColors
                        .valueOf(if (which == 0) "BLACK" else if (which == 1) "RED"
                                 else if (which == 2) "BLUE" else "GREEN").index)
                }
            }.show()
    }

    private fun applyFill() {
        val colors = listOf(-1, 0xFFFFFF00.toInt(), 0xFFFFA500.toInt(), 0xFF90EE90.toInt(), 0xFFADD8E6.toInt())
        val names = listOf("None", "Yellow", "Orange", "Green", "Blue")
        AlertDialog.Builder(this).setTitle("Fill Color")
            .setItems(names.toTypedArray()) { _, which ->
                styleSelectedCell { (_, style) ->
                    if (which == 0) {
                        style.fillPattern = FillPatternType.NO_FILL
                    } else {
                        style.fillPattern = FillPatternType.SOLID_FOREGROUND
                        style.fillForegroundColor = customColor(workbook!!, colors[which])
                    }
                }
                refreshCellView(selRow, selCol)
            }.show()
    }

    private fun customColor(wb: Workbook, argb: Int): Short = (argb and 0xFFFFFF).toShort()

    private fun setAlignment(align: HorizontalAlignment) =
        styleSelectedCell { (_, style) -> style.alignment = align }

    // ---------- Row/Column ops ----------

    private fun insertRow() { sheet?.shiftRows(selRow, sheet!!.lastRowNum, 1); renderAll() }
    private fun deleteRow() {
        val sh = sheet ?: return
        if (selRow <= sh.lastRowNum) { sh.removeRow(sh.getRow(selRow)); sh.shiftRows(selRow + 1, sh.lastRowNum, -1) }
        renderAll()
    }
    private fun insertCol() { sheet?.shiftColumns(selCol, sheet!!.getRow(0)?.lastCellNum?.toInt() ?: cols, 1); renderAll() }
    private fun deleteCol() {
        val sh = sheet ?: return
        val last = (sh.getRow(0)?.lastCellNum?.toInt() ?: 0) - 1
        if (selCol <= last) sh.shiftColumns(selCol + 1, last, -1)
        renderAll()
    }

    // ---------- Formula bar ----------

    private fun applyFormulaBar() {
        val text = formulaBar.text.toString()
        setCell(selRow, selCol, text)
        refreshCellView(selRow, selCol)
    }

    private fun updateFormulaBar(r: Int, c: Int) {
        cellRef.text = cellName(c) + (r + 1)
        formulaBar.setText(cellText(getCell(r, c)))
    }

    // ---------- Sheets ----------

    private fun addSheet() {
        val wb = workbook ?: return
        val name = "Sheet${wb.numberOfSheets + 1}"
        wb.createSheet(name)
        currentSheetIndex = wb.numberOfSheets - 1
        sheet = wb.getSheetAt(currentSheetIndex)
        renderAll()
    }

    private fun renameSheet() {
        val et = EditText(this)
        et.setText(sheet?.sheetName ?: "")
        AlertDialog.Builder(this).setTitle("Rename Sheet").setView(et)
            .setPositiveButton("OK") { _, _ ->
                val wb = workbook ?: return@setPositiveButton
                wb.setSheetName(currentSheetIndex, et.text.toString().ifBlank { "Sheet1" })
                renderAll()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun renderSheetTabs() {
        sheetTabs.removeAllViews()
        val wb = workbook ?: return
        for (i in 0 until wb.numberOfSheets) {
            val b = Button(this)
            b.text = wb.getSheetName(i)
            b.textSize = 12f
            b.isAllCaps = false
            b.setPadding(16, 4, 16, 4)
            if (i == currentSheetIndex) {
                b.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1976D2")); cornerRadius = 12f
                }
                b.setTextColor(Color.WHITE)
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
            f.inputStream().use { workbook = WorkbookFactory.create(it) }
            sheet = workbook?.getSheetAt(0)
            withContext(Dispatchers.Main) {
                statusText.text = "Loaded: ${f.name} — ${sheet?.physicalNumberOfRows ?: 0} rows"
                renderAll()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                statusText.text = "Error: ${e.message}"
                Toast.makeText(this@ExcelEditorActivity, "Load failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun save() = CoroutineScope(Dispatchers.IO).launch {
        val f = file ?: File(getExternalFilesDir(null), "new_sheet.xlsx").also { file = it }
        try {
            workbook?.creationHelper?.createFormulaEvaluator()?.evaluateAll()
            f.outputStream().use { workbook?.write(it) }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExcelEditorActivity, "Saved: ${f.name} ✅", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExcelEditorActivity, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ---------- Grid render ----------

    private fun renderAll() {
        renderSheetTabs()
        renderGrid()
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
                et.textSize = 13f
                et.setPadding(12, 10, 12, 10)
                et.minWidth = px(110)
                et.gravity = Gravity.CENTER_VERTICAL
                et.setBackgroundColor(Color.WHITE)
                et.setTextColor(Color.BLACK)
                et.setText(cellText(cell))
                applyCellStyleToView(et, cell)
                et.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        setCell(r, c, et.text.toString())
                    } else {
                        selRow = r; selCol = c
                        updateFormulaBar(r, c)
                    }
                }
                et.setOnClickListener {
                    selRow = r; selCol = c
                    updateFormulaBar(r, c)
                }
                cellViews["${r},${c}"] = et
                tr.addView(et)
            }
            grid.addView(tr)
        }
    }

    /** POI cell style ko EditText pe visually reflect karo (bold/italic/size/color/fill/align) */
    private fun applyCellStyleToView(et: EditText, cell: Cell?) {
        cell ?: return
        try {
            val style = cell.cellStyle
            val font = workbook?.getFontAt(style.fontIndexAsInt) ?: return
            et.setTypeface(null, fontTypeface(font))
            et.textSize = font.fontHeightInPoints.toFloat()
            if (style.alignment == HorizontalAlignment.CENTER) et.gravity = Gravity.CENTER
            else if (style.alignment == HorizontalAlignment.RIGHT) et.gravity = Gravity.END
            else et.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            if (style.fillPattern == FillPatternType.SOLID_FOREGROUND) {
                et.setBackgroundColor(poiColorToArgb(style.fillForegroundColor))
            }
        } catch (_: Exception) {}
    }

    private fun fontTypeface(f: Font): Int {
        var t = 0
        if (f.bold) t = t or Typeface.BOLD
        if (f.italic) t = t or Typeface.ITALIC
        return t
    }

    private fun poiColorToArgb(poi: Short): Int {
        val rgb = poi.toInt() and 0xFFFFFF
        return if (rgb == 0) Color.WHITE else (0xFF000000.toInt() or rgb)
    }

    private fun refreshCellView(r: Int, c: Int) {
        val et = cellViews["${r},${c}"] ?: return
        val cell = getCell(r, c)
        et.setText(cellText(cell))
        applyCellStyleToView(et, cell)
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
        tv.minWidth = px(40)
        tv.setTextColor(Color.WHITE)
        tv.background = GradientDrawable().apply {
            setColor(Color.parseColor("#1976D2")); cornerRadius = 4f
        }
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
