package com.vedanand.vinsoffice

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File

/**
 * Professional Word editor — page view + rich text (bold/italic/underline/
 * strike/color/highlight/alignment) jo .docx me save hoti hai.
 */
class WordEditorActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var page: android.view.View
    private lateinit var sizeLabel: TextView
    private lateinit var wordCount: TextView
    private lateinit var saveStatus: TextView
    private lateinit var fontSpinner: Spinner

    private var docFile: File? = null
    private var baseTextSize = 14f
    private var currentSize = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_editor)

        editText = findViewById(R.id.editText)
        page = findViewById(R.id.page)
        sizeLabel = findViewById(R.id.sizeLabel)
        wordCount = findViewById(R.id.wordCount)
        saveStatus = findViewById(R.id.saveStatus)
        fontSpinner = findViewById(R.id.fontSpinner)

        setupFonts()
        setupToolbar()

        // Word count live
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val words = s?.toString()?.trim()?.split(Regex("\\s+"))?.count { it.isNotBlank() } ?: 0
                wordCount.text = "${words} words"
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        intent.getStringExtra("path")?.let {
            docFile = File(it)
            loadDocx(docFile!!)
        }
    }

    private fun setupFonts() {
        val fonts = FontManager.listFonts(this)
        fontSpinner.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, fonts)
        fontSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>, v: android.view.View?, pos: Int, id: Long) {
                val name = fonts.getOrNull(pos) ?: return
                val tf = FontManager.load(this@WordEditorActivity, name) ?: Typeface.DEFAULT
                editText.typeface = tf
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>) {}
        }
    }

    // ---------- Toolbar ----------

    private fun setupToolbar() {
        fun t(id: String) = findViewById<TextView>(resources.getIdentifier(id, "id", packageName))
        t("btnDone").setOnClickListener { saveDocx() }
        t("btnSaveTop").setOnClickListener { saveDocx() }
        t("btnClose").setOnClickListener { finish() }
        t("btnZoom").setOnClickListener {
            baseTextSize = when (baseTextSize) { 12f -> 14f; 14f -> 18f; 18f -> 22f; else -> 12f }
            (page.layoutParams as? android.widget.LinearLayout.LayoutParams)?.let { _ -> }
            editText.textSize = baseTextSize
            t("btnZoom").text = "${(baseTextSize / 14f * 100).toInt()}%"
        }
        t("btnSizeUp").setOnClickListener { changeSize(2) }
        t("btnSizeDown").setOnClickListener { changeSize(-2) }

        t("btnBold").setOnClickListener { toggleSpan(::StyleSpan, selectionFlags(), create = { StyleSpan(Typeface.BOLD) }, has = { s -> hasStyle(s, Typeface.BOLD) }) }
        t("btnItalic").setOnClickListener { toggleSpan(::StyleSpan, selectionFlags(), create = { StyleSpan(Typeface.ITALIC) }, has = { s -> hasStyle(s, Typeface.ITALIC) }) }
        t("btnUnderline").setOnClickListener { toggleSpan(::UnderlineSpan, selectionFlags(), create = { UnderlineSpan() }, has = { s -> s.any { it is UnderlineSpan } }) }
        t("btnStrike").setOnClickListener { toggleSpan(::StrikethroughSpan, selectionFlags(), create = { StrikethroughSpan() }, has = { s -> s.any { it is StrikethroughSpan } }) }
        t("btnTextColor").setOnClickListener { pickColor("Text Color") { c ->
            applyToSelection { start, end -> editText.text.setSpan(ForegroundColorSpan(c), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        } }
        t("btnHighlight").setOnClickListener { pickColor("Highlight") { c ->
            applyToSelection { start, end -> editText.text.setSpan(BackgroundColorSpan(c), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        } }
        t("btnAlignLeft").setOnClickListener { setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL) }
        t("btnAlignCenter").setOnClickListener { setAlignment(android.text.Layout.Alignment.ALIGN_CENTER) }
        t("btnAlignRight").setOnClickListener { setAlignment(android.text.Layout.Alignment.ALIGN_OPPOSITE) }
        t("btnAlignJustify").setOnClickListener { setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL) }
    }

    private fun selectionFlags(): Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE

    private fun hasStyle(spans: Array<CharacterStyle>, flag: Int): Boolean =
        spans.any { it is StyleSpan && (it.style and flag) != 0 }

    private fun toggleSpan(
        ctor: () -> Unit,
        flags: Int,
        create: () -> CharacterStyle,
        has: (Array<CharacterStyle>) -> Boolean
    ) {
        val s = editText.selectionStart.coerceAtLeast(0)
        val e = editText.selectionEnd.coerceAtLeast(0)
        if (s == e) {
            Toast.makeText(this, "Pehle text select karo", Toast.LENGTH_SHORT).show()
            return
        }
        val spans = editText.text.getSpans(s, e, CharacterStyle::class.java)
        if (has(spans)) {
            // remove matching spans
            editText.text.getSpans(s, e, CharacterStyle::class.java).forEach { span ->
                val shouldRemove = when (span) {
                    is StyleSpan -> (span.style and (Typeface.BOLD or Typeface.ITALIC)) != 0 && has(arrayOf(span))
                    is UnderlineSpan -> has(arrayOf(span))
                    is StrikethroughSpan -> has(arrayOf(span))
                    else -> false
                }
                if (shouldRemove) editText.text.removeSpan(span)
            }
        } else {
            editText.text.setSpan(create(), s, e, flags)
        }
    }

    private fun applyToSelection(apply: (Int, Int) -> Unit) {
        val s = editText.selectionStart.coerceAtLeast(0)
        val e = editText.selectionEnd.coerceAtLeast(0)
        if (s == e) {
            Toast.makeText(this, "Pehle text select karo", Toast.LENGTH_SHORT).show()
            return
        }
        apply(minOf(s, e), maxOf(s, e))
    }

    private fun changeSize(delta: Int) {
        currentSize = (currentSize + delta).coerceIn(8, 72)
        sizeLabel.text = "$currentSize"
        applyToSelection { s, e ->
            editText.text.setSpan(android.text.style.RelativeSizeSpan(currentSize / 11f), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun pickColor(title: String, apply: (Int) -> Unit) {
        val names = arrayOf("Black", "Red", "Blue", "Green", "Yellow", "Highlight Yellow", "Highlight Green")
        val colors = intArrayOf(
            0xFF212121.toInt(), 0xFFE53935.toInt(), 0xFF1E88E5.toInt(), 0xFF43A047.toInt(),
            0xFFFDD835.toInt(), 0xFFFFFF00.toInt(), 0xFFCCFF90.toInt())
        AlertDialog.Builder(this).setTitle(title)
            .setItems(names) { _, which -> apply(colors[which]) }.show()
    }

    private fun setAlignment(alignment: android.text.Layout.Alignment) {
        val text = editText.text
        // Current paragraph ke start/end dhoondo
        val sel = editText.selectionStart.coerceAtLeast(0)
        var start = text.indexOf('
', sel)
        var paraStart = if (sel > 0) text.lastIndexOf('
', sel - 1).coerceAtLeast(-1) + 1 else 0
        var paraEnd = if (start == -1) text.length else start
        text.setSpan(AlignmentSpan.Standard(alignment), paraStart, paraEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    // ---------- Load ----------

    private fun loadDocx(file: File) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val sb = StringBuilder()
            val doc = XWPFDocument(file.inputStream())
            doc.paragraphs.forEach { p ->
                sb.append(p.text).append("\n")
            }
            doc.tables.forEach { table ->
                table.rows.forEach { row ->
                    sb.append(row.tableCells.joinToString("\t") { it.text }).append("\n")
                }
            }
            doc.close()
            withContext(Dispatchers.Main) {
                editText.setText(sb.toString())
                saveStatus.text = "Loaded"
            }
        } catch (t: Throwable) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@WordEditorActivity,
                    "Load failed: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ---------- Save (rich text -> docx runs) ----------

    private fun saveDocx() = CoroutineScope(Dispatchers.IO).launch {
        val file = docFile ?: File(getExternalFilesDir(null), "new_document.docx").also { docFile = it }
        try {
            XWPFDocument().use { doc ->
                val text = editText.text
                // Line by line paragraphs, format-grouped runs
                val lines = text.toString().split("\n")
                var offset = 0
                for (line in lines) {
                    val para = doc.createParagraph()
                    // Alignment span?
                    text.getSpans(offset, offset + line.length.coerceAtLeast(0), AlignmentSpan::class.java)
                        .firstOrNull()?.let { span ->
                            when (span.alignment) {
                                android.text.Layout.Alignment.ALIGN_CENTER -> para.alignment = ParagraphAlignment.CENTER
                                android.text.Layout.Alignment.ALIGN_OPPOSITE -> para.alignment = ParagraphAlignment.RIGHT
                                else -> para.alignment = ParagraphAlignment.LEFT
                            }
                        }
                    if (line.isEmpty()) { offset += 1; continue }

                    // Format-grouped runs within the line
                    var segStart = 0
                    while (segStart < line.length) {
                        var segEnd = segStart + 1
                        val fmt = formatAt(text, offset + segStart)
                        while (segEnd < line.length && formatAt(text, offset + segEnd) == fmt) segEnd++
                        val run = para.createRun()
                        run.setText(line.substring(segStart, segEnd))
                        val (bold, italic, underline, strike, color, size) = fmt
                        run.isBold = bold
                        run.isItalic = italic
                        if (underline) run.isUnderline = org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE
                        if (strike) run.isStrikeThrough = true
                        if (color != null) run.color = rgbHex(color)
                        run.fontSize = size
                        segStart = segEnd
                    }
                    offset += line.length + 1
                }
                file.outputStream().use { doc.write(it) }
            }
            withContext(Dispatchers.Main) { saveStatus.text = "Saved ✅" }
        } catch (t: Throwable) {
            withContext(Dispatchers.Main) {
                saveStatus.text = "Save failed: ${t.javaClass.simpleName}"
                Toast.makeText(this@WordEditorActivity, "Save failed: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Position pe active formatting nikalo */
    private data class Fmt(
        val bold: Boolean, val italic: Boolean, val underline: Boolean,
        val strike: Boolean, val color: Int?, val size: Int
    )

    private fun formatAt(text: Editable, pos: Int): Fmt {
        var bold = false; var italic = false; var underline = false; var strike = false
        var color: Int? = null
        for (span in text.getSpans(pos, pos + 1, CharacterStyle::class.java)) {
            if (span !is StyleSpan) {
                when (span) {
                    is UnderlineSpan -> underline = true
                    is StrikethroughSpan -> strike = true
                    is ForegroundColorSpan -> color = span.foregroundColor
                    else -> {}
                }
            } else {
                if (span.style and Typeface.BOLD != 0) bold = true
                if (span.style and Typeface.ITALIC != 0) italic = true
            }
        }
        // Size span?
        var size = currentSize
        for (span in text.getSpans(pos, pos + 1, android.text.style.AbsoluteSizeSpan::class.java)) {
            size = span.size
        }
        for (span in text.getSpans(pos, pos + 1, android.text.style.RelativeSizeSpan::class.java)) {
            size = (11 * span.sizeChange).toInt().coerceIn(8, 72)
        }
        return Fmt(bold, italic, underline, strike, color, size)
    }

    private fun rgbHex(color: Int): String =
        String.format("%02X%02X%02X", (color shr 16) and 0xFF, (color shr 8) and 0xFF, color and 0xFF)
}
