package com.vedanand.vinsoffice

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
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
    private var workbook: Workbook? = null
    private var sheet: Sheet? = null
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excel_editor)
        grid = findViewById(R.id.grid)

        intent.getStringExtra("path")?.let {
            file = File(it)
            loadWorkbook(file!!)
        } ?: run {
            workbook = XSSFWorkbook()
            sheet = workbook!!.createSheet("Sheet1")
            renderGrid()
        }
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
    }

    private fun loadWorkbook(f: File) = CoroutineScope(Dispatchers.IO).launch {
        runCatching {
            f.inputStream().use { workbook = WorkbookFactory.create(it) }
            sheet = workbook?.getSheetAt(0)
        }
        withContext(Dispatchers.Main) { renderGrid() }
    }

    private fun renderGrid() {
        grid.removeAllViews()
        val sh = sheet ?: return
        for (r in 0 until maxOf(sh.physicalNumberOfRows, 10)) {
            val row = sh.getRow(r) ?: sh.createRow(r)
            val tr = TableRow(this)
            for (c in 0 until 6) {
                val cell = row.getCell(c) ?: row.createCell(c)
                val et = EditText(this)
                et.setText(cell.toString())
                et.setPadding(8, 8, 8, 8)
                et.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) setCell(r, c, et.text.toString())
                }
                tr.addView(et, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f))
            }
            grid.addView(tr)
        }
    }

    private fun setCell(r: Int, c: Int, value: String) {
        val cell = sheet!!.getRow(r).getCell(c)
        if (value.startsWith("=")) {
            cell.cellFormula = value.removePrefix("=")
            cell.cellType = CellType.FORMULA
        } else {
            cell.cellType = CellType.STRING
            cell.setCellValue(value)
        }
    }

    private fun save() = CoroutineScope(Dispatchers.IO).launch {
        val f = file ?: File(getExternalFilesDir(null), "new_sheet.xlsx").also { file = it }
        runCatching {
            workbook?.creationHelper?.createFormulaEvaluator()?.evaluateAll()
            f.outputStream().use { workbook?.write(it) }
        }.onSuccess {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExcelEditorActivity, "Saved ✅", Toast.LENGTH_SHORT).show()
            }
        }.onFailure { e ->
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ExcelEditorActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
