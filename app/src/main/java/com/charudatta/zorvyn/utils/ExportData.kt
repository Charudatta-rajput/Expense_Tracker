package com.charudatta.zorvyn.utils

import com.charudatta.zorvyn.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

data class ExportSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netSavings: Double,
    val topCategory: String,
    val topCategoryAmount: Double,
    val periodStart: String,
    val periodEnd: String,
    val transactionCount: Int
)

object ExportDataGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun generateCSV(transactions: List<Transaction>, summary: ExportSummary): String {
        val stringBuilder = StringBuilder()


        stringBuilder.append("Date,Type,Category,Amount,Note,Transaction ID\n")


        transactions.sortedByDescending { it.date }.forEach { transaction ->
            stringBuilder.append("${dateFormat.format(Date(transaction.date))},")
            stringBuilder.append("${transaction.type},")
            stringBuilder.append("${escapeCSV(transaction.category)},")
            stringBuilder.append("₹${"%.2f".format(transaction.amount)},")
            stringBuilder.append("${escapeCSV(transaction.note)},")
            stringBuilder.append("${transaction.id}\n")
        }


        stringBuilder.append("\n\n--- SUMMARY ---\n")
        stringBuilder.append("Period,${summary.periodStart} to ${summary.periodEnd}\n")
        stringBuilder.append("Total Income,₹${"%.2f".format(summary.totalIncome)}\n")
        stringBuilder.append("Total Expense,₹${"%.2f".format(summary.totalExpense)}\n")
        stringBuilder.append("Net Savings,₹${"%.2f".format(summary.netSavings)}\n")
        stringBuilder.append("Top Category,${summary.topCategory} (₹${"%.2f".format(summary.topCategoryAmount)})\n")
        stringBuilder.append("Total Transactions,${summary.transactionCount}\n")
        stringBuilder.append("Generated on,${displayDateFormat.format(Date())}\n")

        return stringBuilder.toString()
    }

    private fun escapeCSV(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}