package com.vedanand.vinsoffice

import android.graphics.Typeface
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File

class WordEditorActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private var docFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_editor)
        editText = findViewById(R.id.editText)
        val fontSpinner = findViewById<Spinner>(R.id.fontSpinner)

        val fonts = FontManager.listFonts(this)
        fontSpinner.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, fonts)
        fontSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>, v: android.view.View?, pos: Int, id: Long) {
                val name = fonts.getOrNull(pos) ?: return
                editText.typeface = FontManager.load(this@WordEditorActivity, name) ?: Typeface.DEFAULT
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>) {}
        }

        intent.getStringExtra("path")?.let {
            docFile = File(it)
            loadDocx(docFile!!)
        }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveDocx() }
    }

    private fun loadDocx(file: File) = CoroutineScope(Dispatchers.IO).launch {
        val sb = StringBuilder()
        runCatching {
            XWPFDocument(file.inputStream()).use { doc ->
                doc.paragraphs.forEach { sb.append(it.text).append("\n") }
                doc.tables.forEach { table ->
                    table.rows.forEach { row ->
                        sb.append(row.tableCells.joinToString("\t") { it.text }).append("\n")
                    }
                }
            }
        }.onFailure { sb.append("Error: ${it.message}") }
        withContext(Dispatchers.Main) { editText.setText(sb.toString()) }
    }

    private fun saveDocx() = CoroutineScope(Dispatchers.IO).launch {
        val file = docFile ?: File(getExternalFilesDir(null), "new_document.docx").also { docFile = it }
        runCatching {
            XWPFDocument().use { doc ->
                editText.text.toString().split("\n").forEach { line ->
                    val run = doc.createParagraph().createRun()
                    run.setText(line)
                    run.fontFamily = "LMG Arun"
                    run.fontSize = 14
                }
                file.outputStream().use { doc.write(it) }
            }
        }.onSuccess {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@WordEditorActivity, "Saved ✅", Toast.LENGTH_SHORT).show()
            }
        }.onFailure { e ->
            withContext(Dispatchers.Main) {
                Toast.makeText(this@WordEditorActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
