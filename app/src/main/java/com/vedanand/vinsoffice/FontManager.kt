package com.vedanand.vinsoffice

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import java.io.File

/**
 * Custom font manager — bundled (assets/fonts) + user-imported fonts.
 * Shruti, LMG Arun, Mangal etc. support karta hai.
 */
object FontManager {
    private val cache = mutableMapOf<String, Typeface>()

    /** App ke assets/fonts folder se font load karo */
    fun fromAssets(context: Context, fontName: String): Typeface? {
        cache[fontName]?.let { return it }
        return try {
            val tf = Typeface.createFromAsset(context.assets, "fonts/$fontName.ttf")
            cache[fontName] = tf
            tf
        } catch (e: Exception) { null }
    }

    /** User-imported font: app-internal folder se */
    fun fromImported(context: Context, fontName: String): Typeface? {
        cache[fontName]?.let { return it }
        val file = File(context.filesDir, "fonts/$fontName.ttf")
        if (!file.exists()) return null
        return try {
            val tf = Typeface.createFromFile(file)
            cache[fontName] = tf
            tf
        } catch (e: Exception) { null }
    }

    /** Kisi bhi naam se best-match font load karo */
    fun load(context: Context, fontName: String): Typeface? =
        fromAssets(context, fontName) ?: fromImported(context, fontName)

    /** Saare available fonts ki list (bundled + imported) */
    fun listFonts(context: Context): List<String> {
        val list = mutableListOf<String>()
        runCatching { context.assets.list("fonts") }
            .getOrNull()?.forEach { list += it.removeSuffix(".ttf") }
        File(context.filesDir, "fonts").listFiles()
            ?.forEach { list += it.nameWithoutExtension }
        return list.distinct()
    }

    /** Storage se .ttf import karke app folder me copy karo */
    fun importFont(context: Context, uri: Uri, name: String): Boolean {
        return try {
            val dir = File(context.filesDir, "fonts").apply { mkdirs() }
            val out = File(dir, "$name.ttf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { output -> input.copyTo(output) }
            } ?: return false
            true
        } catch (e: Exception) { false }
    }
}
