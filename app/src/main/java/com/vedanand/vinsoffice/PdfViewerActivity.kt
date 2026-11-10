package com.vedanand.vinsoffice

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var pdfFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)
        setContentView(R.layout.activity_pdf_viewer)
        imageView = findViewById(R.id.pdfPage)

        intent.getStringExtra("path")?.let {
            pdfFile = File(it)
            renderPage(0)
        }
        findViewById<Button>(R.id.btnAddText).setOnClickListener {
            pdfFile?.let { addTextToPdf(it, "नमस्ते / Hello", 50f, 700f) }
        }
    }

    private fun renderPage(index: Int) {
        val file = pdfFile ?: return
        try {
            PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
                renderer.openPage(index).use { page ->
                    val bmp = Bitmap.createBitmap(
                        page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    imageView.setImageBitmap(bmp)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "PDF render error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** pdfbox se text add karo — custom TTF (LMG Arun/Shruti) embed ke saath */
    private fun addTextToPdf(file: File, text: String, x: Float, y: Float) {
        try {
            val doc = PDDocument.load(file)
            val page = doc.getPage(0)
            val content = PDPageContentStream(
                doc, page, PDPageContentStream.AppendMode.APPEND, true, true)

            val fontFile = File(filesDir, "fonts/LMG Arun.ttf")
            val font = if (fontFile.exists()) PDType0Font.load(doc, fontFile)
                       else PDType0Font.load(doc, assets.open("fonts/LMG Arun.ttf"))
            content.beginText()
            content.setFont(font, 14f)
            content.newLineAtOffset(x, y)
            content.showText(text)
            content.endText()
            content.close()
            doc.save(file.absolutePath)
            doc.close()
            renderPage(0)
            Toast.makeText(this, "PDF updated ✅", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
