package com.charudatta.zorvyn.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExportHelper(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun exportToCSV(content: String, fileName: String): File? {
        return try {
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(content, fileName, "csv")
            } else {
                saveToExternalStorage(content, fileName, "csv")
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToPDF(
        transactions: List<com.charudatta.zorvyn.domain.model.Transaction>,
        summary: ExportSummary,
        fileName: String
    ): File? {
        return try {
            // Create PDF file in cache
            val pdfFile = File(context.cacheDir, "$fileName.pdf")

            // Initialize PDF writer and document
            val pdfWriter = PdfWriter(pdfFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Add title
            val title = Paragraph("Zorvyn Transaction Report")
                .setFontSize(18f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(title)

            // Add generation date
            val datePara = Paragraph("Generated on: ${SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
            document.add(datePara)

            document.add(Paragraph("\n"))

            // Add Summary Section
            val summaryTitle = Paragraph("SUMMARY")
                .setFontSize(14f)
                .setBold()
            document.add(summaryTitle)

            document.add(Paragraph("\n"))

            // Create summary table (2 columns)
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
                .useAllAvailableWidth()

            summaryTable.addCell(Cell().add(Paragraph("Period:")))
            summaryTable.addCell(Cell().add(Paragraph("${summary.periodStart} to ${summary.periodEnd}")))

            summaryTable.addCell(Cell().add(Paragraph("Total Income:")))
            summaryTable.addCell(Cell().add(Paragraph("₹${"%.2f".format(summary.totalIncome)}")))

            summaryTable.addCell(Cell().add(Paragraph("Total Expense:")))
            summaryTable.addCell(Cell().add(Paragraph("₹${"%.2f".format(summary.totalExpense)}")))

            summaryTable.addCell(Cell().add(Paragraph("Net Savings:")))
            summaryTable.addCell(Cell().add(Paragraph("₹${"%.2f".format(summary.netSavings)}")))

            summaryTable.addCell(Cell().add(Paragraph("Top Category:")))
            summaryTable.addCell(Cell().add(Paragraph("${summary.topCategory} (₹${"%.2f".format(summary.topCategoryAmount)})")))

            summaryTable.addCell(Cell().add(Paragraph("Total Transactions:")))
            summaryTable.addCell(Cell().add(Paragraph("${summary.transactionCount}")))

            document.add(summaryTable)
            document.add(Paragraph("\n"))

            // Add Transactions Section
            val transactionsTitle = Paragraph("TRANSACTIONS")
                .setFontSize(14f)
                .setBold()
            document.add(transactionsTitle)

            document.add(Paragraph("\n"))

            // Create transactions table
            val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 10f, 15f, 15f, 30f, 15f)))
                .useAllAvailableWidth()

            // Add headers
            val headers = listOf("Date", "Type", "Category", "Amount", "Note", "ID")
            headers.forEach { header ->
                table.addCell(Cell().add(Paragraph(header).setBold()))
            }

            // Add data rows (limit to 50 for readability)
            val displayDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            transactions.sortedByDescending { it.date }.take(50).forEach { transaction ->
                table.addCell(displayDateFormat.format(Date(transaction.date)))
                table.addCell(transaction.type)
                table.addCell(transaction.category)
                table.addCell("₹${"%.2f".format(transaction.amount)}")
                table.addCell(if (transaction.note.length > 30) transaction.note.take(27) + "..." else transaction.note)
                table.addCell(transaction.id.toString().take(8))
            }

            document.add(table)

            // Close document
            document.close()
            pdfDocument.close()

            // Save to downloads folder
            val finalFile = savePdfToDownloads(pdfFile, fileName)
            finalFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun savePdfToDownloads(pdfFile: File, fileName: String): File? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Zorvyn")
                }

                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        pdfFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    pdfFile.delete()
                    return File("${Environment.DIRECTORY_DOWNLOADS}/Zorvyn/$fileName.pdf")
                }
                null
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, "Zorvyn")
                if (!appDir.exists()) appDir.mkdirs()

                val destFile = File(appDir, "$fileName.pdf")
                pdfFile.copyTo(destFile, overwrite = true)
                pdfFile.delete()
                destFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(content: String, fileName: String, extension: String): File? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.$extension")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/$extension")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Zorvyn")
        }

        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            return File(getDownloadPath(fileName, extension))
        }
        return null
    }

    private fun saveToExternalStorage(content: String, fileName: String, extension: String): File? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, "Zorvyn")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        val file = File(appDir, "$fileName.$extension")
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray())
        }
        return file
    }

    private fun getDownloadPath(fileName: String, extension: String): String {
        return "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/Zorvyn/$fileName.$extension"
    }
}