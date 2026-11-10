package com.vedanand.vinsoffice

/**
 * Unicode Hindi ↔ Krutidev/LMG legacy encoding converter.
 * LMG Arun jaise legacy fonts me Hindi tabhi sahi dikhti hai jab text
 * Krutidev encoding me ho. Full mapping table (~700 entries) online
 * available hai — yahan core subset diya gaya hai.
 */
object KrutiDevConverter {

    // Unicode → Krutidev core mappings (sample)
    private val unicodeToKruti = mapOf(
        "क" to "d", "ख" to "[k", "ग" to "x", "घ" to "?k", "ङ" to "a",
        "च" to "p", "छ" to "N", "ज" to "t", "झ" to ">", "ञ" to "a",
        "ट" to "V", "ठ" to "B", "ड" to "M", "ढ" to "<", "ण" to "Å",
        "त" to "r", "थ" to "'k", "द" to "n", "ध" to "Ùk", "न" to "i",
        "प" to "Q", "फ" to "¶", "ब" to "c", "भ" to "H", "म" to "e",
        "य" to ";", "र" to "j", "ल" to "y", "व" to "o", "श" to "'k",
        "ष" to ""k", "स" to "l", "ह" to "g", "क्ष" to "d", "त्र" to "ra",
        "ज्ञ" to "aa", "़" to "", "ा" to "k", "ि" to "f", "ी" to "F",
        "ु" to "q", "ू" to "w", "ृ" to "ª", "े" to "`", "ै" to "ks",
        "ो" to "ksa", "ौ" to "kSa", "ं" to "a", "ँ" to "aa", "्" to "",
        "०" to "0", "१" to "1", "२" to "2", "३" to "3", "४" to "4",
        "५" to "5", "६" to "6", "७" to "7", "८" to "8", "९" to "9",
        "।" to "A"
    )

    // Krutidev → Unicode (reverse)
    private val krutiToUnicode: Map<String, String> by lazy {
        unicodeToKruti.entries
            .sortedByDescending { it.value.length }
            .associate { it.value to it.key }
    }

    fun unicodeToKrutidev(text: String): String =
        text.map { unicodeToKruti[it.toString()] ?: it.toString() }.joinToString("")

    fun krutidevToUnicode(text: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            var matched = false
            for (len in 3 downTo 1) {
                if (i + len <= text.length) {
                    val sub = text.substring(i, i + len)
                    krutiToUnicode[sub]?.let {
                        sb.append(it); i += len; matched = true
                        return@let
                    }
                }
                if (matched) break
            }
            if (!matched) { sb.append(text[i]); i++ }
        }
        return sb.toString()
    }
}
