package com.charudatta.zorvyn.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charudatta.zorvyn.data.local.GoalPreferences  // Add this import
import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import android.content.Context
import android.widget.Toast
import com.charudatta.zorvyn.utils.ExportDataGenerator
import com.charudatta.zorvyn.utils.ExportHelper
import com.charudatta.zorvyn.utils.ExportSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


data class MonthlyData(
    val monthName: String,
    val totalSpent: Double,
    val percentageChange: Double? = null,
    val isIncrease: Boolean = false
)

data class InsightsState(
    val topCategory: String = "-",
    val topCategoryAmount: Double = 0.0,
    val thisWeekExpense: Double = 0.0,
    val lastWeekExpense: Double = 0.0,
    val categoryBreakdown: Map<String, Double> = emptyMap(),
    val monthlyTrends: List<MonthlyData> = emptyList(),
    val bestMonth: MonthlyData? = null,
    val worstMonth: MonthlyData? = null,
    val monthlyTotalExpense: Double = 0.0,
    val monthlyBudget: Double = 0.0  // Add this line
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val goalPreferences: GoalPreferences  // Add this injection
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state

    init {
        // Load monthly budget from preferences
        viewModelScope.launch {
            goalPreferences.monthlyGoal.collect { budget ->
                _state.update { it.copy(monthlyBudget = budget) }
            }
        }

        // Load transactions
        viewModelScope.launch {
            getTransactions().collect { transactions ->
                val expenses = transactions.filter { it.type == "expense" }

                // Category breakdown
                val breakdown = expenses
                    .groupBy { it.category }
                    .mapValues { e -> e.value.sumOf { it.amount } }

                val topEntry = breakdown.maxByOrNull { it.value }

                // This week vs last week
                val now = Calendar.getInstance()
                val startOfWeek = now.clone() as Calendar
                startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
                startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
                startOfWeek.set(Calendar.MINUTE, 0)
                startOfWeek.set(Calendar.SECOND, 0)
                startOfWeek.set(Calendar.MILLISECOND, 0)

                val startOfLastWeek = (startOfWeek.clone() as Calendar).apply {
                    add(Calendar.WEEK_OF_YEAR, -1)
                }

                val thisWeek = expenses
                    .filter { it.date >= startOfWeek.timeInMillis }
                    .sumOf { it.amount }

                val lastWeek = expenses
                    .filter { it.date >= startOfLastWeek.timeInMillis && it.date < startOfWeek.timeInMillis }
                    .sumOf { it.amount }

                // Calculate monthly trends
                val monthlyTrends = calculateMonthlyTrends(expenses)
                val bestMonth = monthlyTrends.minByOrNull { it.totalSpent }
                val worstMonth = monthlyTrends.maxByOrNull { it.totalSpent }

                // Calculate current month total expense
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                val monthlyTotalExpense = expenses.filter { transaction ->
                    val date = Calendar.getInstance().apply { timeInMillis = transaction.date }
                    date.get(Calendar.MONTH) == currentMonth &&
                            date.get(Calendar.YEAR) == currentYear
                }.sumOf { it.amount }

                _state.update { currentState ->
                    currentState.copy(
                        topCategory = topEntry?.key ?: "-",
                        topCategoryAmount = topEntry?.value ?: 0.0,
                        thisWeekExpense = thisWeek,
                        lastWeekExpense = lastWeek,
                        categoryBreakdown = breakdown,
                        monthlyTrends = monthlyTrends,
                        bestMonth = bestMonth,
                        worstMonth = worstMonth,
                        monthlyTotalExpense = monthlyTotalExpense
                    )
                }
            }
        }
    }


    // Add this function inside InsightsViewModel class
    fun exportTransactions(context: Context, format: String) {
        viewModelScope.launch {
            try {
                // Get all transactions
                val transactions = getTransactions().first()
                val expenses = transactions.filter { it.type == "expense" }
                val incomes = transactions.filter { it.type == "income" }

                if (transactions.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val totalExpense = expenses.sumOf { it.amount }
                val totalIncome = incomes.sumOf { it.amount }

                // Get top category
                val topCategory = expenses.groupBy { it.category }
                    .maxByOrNull { it.value.sumOf { t -> t.amount } }

                // Get date range
                val oldestDate = transactions.minByOrNull { it.date }?.date ?: System.currentTimeMillis()
                val newestDate = transactions.maxByOrNull { it.date }?.date ?: System.currentTimeMillis()
                val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())

                val summary = ExportSummary(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    netSavings = totalIncome - totalExpense,
                    topCategory = topCategory?.key ?: "-",
                    topCategoryAmount = topCategory?.value?.sumOf { it.amount } ?: 0.0,
                    periodStart = dateFormat.format(java.util.Date(oldestDate)),
                    periodEnd = dateFormat.format(java.util.Date(newestDate)),
                    transactionCount = transactions.size
                )

                val exportHelper = ExportHelper(context)
                val fileName = "Zorvyn_Export_${System.currentTimeMillis()}"

                val result = if (format == "csv") {
                    val csvContent = ExportDataGenerator.generateCSV(transactions, summary)
                    exportHelper.exportToCSV(csvContent, fileName)
                } else {
                    exportHelper.exportToPDF(transactions, summary, fileName)
                }

                withContext(Dispatchers.Main) {
                    if (result != null) {
                        Toast.makeText(
                            context,
                            "Exported to Downloads/Zorvyn/",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }
}

private fun calculateMonthlyTrends(expenses: List<Transaction>): List<MonthlyData> {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val months = mutableListOf<MonthlyData>()
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    // Get last 3 months
    for (i in 2 downTo 0) {
        var month = currentMonth - i
        var year = currentYear

        if (month < 0) {
            month += 12
            year -= 1
        }

        val monthlyTotal = expenses.filter { transaction ->
            val date = Calendar.getInstance().apply { timeInMillis = transaction.date }
            date.get(Calendar.MONTH) == month && date.get(Calendar.YEAR) == year
        }.sumOf { it.amount }

        months.add(
            MonthlyData(
                monthName = monthNames[month],
                totalSpent = monthlyTotal
            )
        )
    }

    // Add percentage changes
    return months.mapIndexed { index, data ->
        val previousMonth = months.getOrNull(index - 1)
        val percentageChange = if (previousMonth != null && previousMonth.totalSpent > 0) {
            ((data.totalSpent - previousMonth.totalSpent) / previousMonth.totalSpent) * 100
        } else null

        data.copy(
            percentageChange = percentageChange,
            isIncrease = percentageChange != null && percentageChange > 0
        )
    }
}