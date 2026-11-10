package com.vedanand.vinsoffice

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private val docPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri -> openWith(uri, WordEditorActivity::class.java) }
    private val sheetPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri -> openWith(uri, ExcelEditorActivity::class.java) }
    private val pdfPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri -> openWith(uri, PdfViewerActivity::class.java) }
    private val fontPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri -> uri?.let { importFont(it) } }

    private lateinit var fontSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnWord).setOnClickListener {
            docPicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        }
        findViewById<Button>(R.id.btnExcel).setOnClickListener {
            sheetPicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        }
        findViewById<Button>(R.id.btnPdf).setOnClickListener {
            pdfPicker.launch(arrayOf("application/pdf"))
        }
        findViewById<Button>(R.id.btnImportFont).setOnClickListener {
            fontPicker.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "*/*"))
        }
        fontSpinner = findViewById(R.id.fontSpinner)
        refreshFonts()
    }

    private fun refreshFonts() {
        val fonts = FontManager.listFonts(this)
        fontSpinner.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, fonts)
    }

    private fun importFont(uri: android.net.Uri) {
        val name = queryName(uri).removeSuffix(".ttf")
        if (FontManager.importFont(this, uri, name)) {
            Toast.makeText(this, "Font imported: $name ✅", Toast.LENGTH_SHORT).show()
            refreshFonts()
        } else {
            Toast.makeText(this, "Font import failed ❌", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWith(uri: android.net.Uri?, target: Class<*>) {
        uri ?: return
        val name = queryName(uri)
        val copy = File(cacheDir, name)
        contentResolver.openInputStream(uri)?.use { input ->
            copy.outputStream().use { input.copyTo(it) }
        }
        startActivity(Intent(this, target).putExtra("path", copy.absolutePath))
    }

    private fun queryName(uri: android.net.Uri): String =
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            (if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null)
        } ?: "document"
}
