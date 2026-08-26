package com.thyroidtracker.app.report

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

fun createDoctorReportPdf(context: Context, text: String): File {
    val document = PdfDocument()
    val bodyPaint = Paint().apply {
        textSize = 11f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
    val titlePaint = Paint(bodyPaint).apply {
        textSize = 18f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    val pageWidth = 612
    val pageHeight = 792
    val margin = 48f
    val bottomMargin = 48f
    val bodyLineHeight = 16f
    val contentWidth = pageWidth - (margin * 2)

    var pageNumber = 0
    var page: PdfDocument.Page? = null
    var y = margin

    fun startPage() {
        pageNumber += 1
        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        y = margin
    }

    fun finishPage() {
        page?.let { document.finishPage(it) }
        page = null
    }

    fun ensureSpace(height: Float) {
        if (page == null) startPage()
        if (y + height > pageHeight - bottomMargin) {
            finishPage()
            startPage()
        }
    }

    fun drawWrappedLine(line: String, paint: Paint) {
        if (line.isBlank()) {
            ensureSpace(bodyLineHeight)
            y += bodyLineHeight
            return
        }
        val words = line.split(Regex("\\s+"))
        var current = ""
        words.forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (paint.measureText(candidate) <= contentWidth) {
                current = candidate
            } else {
                ensureSpace(bodyLineHeight)
                page!!.canvas.drawText(current, margin, y, paint)
                y += bodyLineHeight
                current = word
            }
        }
        if (current.isNotBlank()) {
            ensureSpace(bodyLineHeight)
            page!!.canvas.drawText(current, margin, y, paint)
            y += bodyLineHeight
        }
    }

    startPage()
    drawWrappedLine("Thyroid Tracker — Patient Summary", titlePaint)
    y += 8f
    text.lineSequence().drop(1).forEach { drawWrappedLine(it, bodyPaint) }
    finishPage()

    val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
    val file = File(reportsDir, "Thyroid-Doctor-Report-${LocalDate.now()}.pdf")
    FileOutputStream(file).use { document.writeTo(it) }
    document.close()
    return file
}
