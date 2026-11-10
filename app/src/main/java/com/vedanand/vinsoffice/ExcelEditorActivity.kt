package com.vedanand.vinsoffice

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class ExcelEditorActivity : AppCompatActivity() {

    private lateinit var grid: TableLayout
    private lateinit var statusText: TextView
    private var workbook: Workbook? = null
    private var sheet: Sheet? = null
    private var file: File? = null

    private val rows = 20
    private val cols = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excel_editor)
        grid = findViewById(R.id.grid)
        statusText = findViewById(R.id.statusText)

        intent.getStringExtra("path")?.let {
            file = File(it)
            loadWorkbook(file!!)
        } ?: run {
            workbook = XSSFWorkbook()
            sheet = workbook!!.createSheet("Sheet1")
            statusText.text = "New sheet (empty)"
            renderGrid()
        }
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
    }

    private fun loadWorkbook(f: File) = CoroutineScope(Dispatchers.IO).launch {
        try {
            f.inputStream().use { workbook = WorkbookFactory.create(it) }
            sheet = workbook?.getSheetAt(0)
            withContext(Dispatchers.Main) {
                statusText.text = "Loaded: ${f.name} — ${sheet?.physicalNumberOfRows ?: 0} rows"
                renderGrid()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                statusText.text = "Error: ${e.message}"
                Toast.makeText(this@ExcelEditorActivity, "Load failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderGrid() {
        grid.removeAllViews()
        val sh = sheet

        // Header row: A, B, C...
        val header = TableRow(this)
        header.addView(headerCell(""))
        for (c in 0 until cols) header.addView(headerCell(cellName(c)))
        grid.addView(header)

        for (r in 0 until rows) {
            val row = sh?.getRow(r)
            val tr = TableRow(this)

            // Row number column
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
                et.hint = ""
                et.setText(cellText(cell))
                et.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) setCell(r, c, et.text.toString())
                }
                tr.addView(et)
            }
            grid.addView(tr)
        }
    }

    private fun cellText(cell: org.apache.poi.ss.usermodel.Cell?): String {
        cell ?: return ""
        return when (cell.cellType) {
            CellType.FORMULA -> try {
                "=" + cell.cellFormula
            } catch (e: Exception) { cell.toString() }
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
            setColor(Color.parseColor("#1976D2"))
            cornerRadius = 4f
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

    private fun px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun setCell(r: Int, c: Int, value: String) {
        val sh = sheet ?: return
        val row = sh.getRow(r) ?: sh.createRow(r)
        val cell = row.getCell(c) ?: row.createCell(c)
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
}
