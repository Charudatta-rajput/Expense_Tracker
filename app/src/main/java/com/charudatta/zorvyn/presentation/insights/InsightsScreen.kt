package com.charudatta.zorvyn.presentation.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.charudatta.zorvyn.presentation.navigation.Screen
import compose.icons.FeatherIcons
import compose.icons.feathericons.Download
import compose.icons.feathericons.FileText
import java.util.Calendar
import kotlin.math.abs

@Composable
fun InsightsScreen(
    navController: NavController,
    viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    val weekDiff = state.thisWeekExpense - state.lastWeekExpense
    val weekTrend = when {
        state.lastWeekExpense == 0.0 -> "No data for last week"
        weekDiff > 0 -> "▲ ₹${"%.2f".format(weekDiff)} more than last week"
        weekDiff < 0 -> "▼ ₹${"%.2f".format(-weekDiff)} less than last week"
        else -> "Same as last week"
    }
    val trendColor = if (weekDiff > 0) Color(0xFFFF5252) else Color(0xFF00C853)


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddTransaction.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Insights",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header with icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = getCategoryEmoji(state.topCategory),
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Top Category",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }

                        // Calculate percentage here
                        val totalSpent = state.categoryBreakdown.values.sum()
                        val percentage = if (totalSpent > 0)
                            (state.topCategoryAmount / totalSpent * 100).toInt()
                        else 0

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$percentage%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category name
                    Text(
                        text = state.topCategory,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "%.2f".format(state.topCategoryAmount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = " spent",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    val totalSpentForBar = state.categoryBreakdown.values.sum()
                    val barPercentage = if (totalSpentForBar > 0)
                        (state.topCategoryAmount / totalSpentForBar).toFloat()
                    else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barPercentage)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        )
                    }
                }
            }


            DailyAverageInsight(
                monthlyTotal = state.monthlyTotalExpense,
                monthlyBudget = state.monthlyBudget
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header with icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📈",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weekly Comparison",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Trend badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (weekDiff > 0)
                                Color(0xFFFF5252).copy(alpha = 0.1f)
                            else if (weekDiff < 0)
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when {
                                        weekDiff > 0 -> "▲"
                                        weekDiff < 0 -> "▼"
                                        else -> "●"
                                    },
                                    fontSize = 10.sp,
                                    color = if (weekDiff > 0) Color(0xFFFF5252)
                                    else if (weekDiff < 0) Color(0xFF4CAF50)
                                    else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = when {
                                        state.lastWeekExpense == 0.0 -> "No data"
                                        weekDiff > 0 -> " Higher"
                                        weekDiff < 0 -> " Lower"
                                        else -> " Same"
                                    },
                                    fontSize = 11.sp,
                                    color = if (weekDiff > 0) Color(0xFFFF5252)
                                    else if (weekDiff < 0) Color(0xFF4CAF50)
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weekly stats with visual bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // This Week Card
                        EnhancedWeekStat(
                            label = "This Week",
                            amount = state.thisWeekExpense,
                            isHighlighted = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Last Week Card
                        EnhancedWeekStat(
                            label = "Last Week",
                            amount = state.lastWeekExpense,
                            isHighlighted = false,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Insight message with emoji
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (weekDiff > 0)
                            Color(0xFFFF5252).copy(alpha = 0.08f)
                        else if (weekDiff < 0)
                            Color(0xFF4CAF50).copy(alpha = 0.08f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    state.lastWeekExpense == 0.0 -> "📊"
                                    weekDiff > 0 -> "⚠️"
                                    weekDiff < 0 -> "🎉"
                                    else -> "✅"
                                },
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = weekTrend,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (state.lastWeekExpense == 0.0)
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else trendColor
                            )
                        }
                    }
                }
            }


            MonthlyTrendCard(
                monthlyTrends = state.monthlyTrends,
                bestMonth = state.bestMonth,
                worstMonth = state.worstMonth
            )


            // Category breakdown
            if (state.categoryBreakdown.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Spending by Category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val maxAmount = state.categoryBreakdown.values.maxOrNull() ?: 1.0
                        val barColors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.error
                        )

                        state.categoryBreakdown.entries
                            .sortedByDescending { it.value }
                            .forEachIndexed { index, entry ->
                                val fraction = (entry.value / maxAmount).toFloat()
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = entry.key,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "₹ ${"%.0f".format(entry.value)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(barColors[index % barColors.size])
                                        )
                                    }
                                }
                            }
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📁 Export Data",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Download your transaction history as CSV or PDF",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CSV Export Button
                        OutlinedButton(
                            onClick = {
                                if (!isExporting) {
                                    isExporting = true
                                    viewModel.exportTransactions(context, "csv")
                                    isExporting = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                FeatherIcons.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CSV", fontSize = 14.sp)
                        }

                        // PDF Export Button
                        Button(
                            onClick = {
                                if (!isExporting) {
                                    isExporting = true
                                    viewModel.exportTransactions(context, "pdf")
                                    isExporting = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                FeatherIcons.FileText,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PDF", fontSize = 14.sp)
                        }
                    }

                    if (isExporting) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))


        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "food", "groceries", "restaurant" -> "🍔"
        "transport", "fuel", "taxi", "car" -> "🚗"
        "shopping", "clothing" -> "🛍️"
        "entertainment", "movies", "music" -> "🎬"
        "bills", "utilities", "rent" -> "💡"
        "health", "medical" -> "💊"
        "education" -> "📚"
        "salary", "income" -> "💰"
        else -> "📊"
    }
}
@Composable
fun EnhancedWeekStat(
    label: String,
    amount: Double,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 0.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isHighlighted) "🔥 $label" else label,
                fontSize = 12.sp,
                color = if (isHighlighted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${"%.0f".format(amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun DailyAverageInsight(
    monthlyTotal: Double,
    monthlyBudget: Double
) {
    val daysInMonth = getDaysInCurrentMonth()
    val dailyAvg = if (daysInMonth > 0) monthlyTotal / daysInMonth else 0.0
    val dailyBudget = if (daysInMonth > 0 && monthlyBudget > 0) monthlyBudget / daysInMonth else 0.0
    val isOverBudget = dailyBudget > 0 && dailyAvg > dailyBudget
    val difference = if (dailyBudget > 0) kotlin.math.abs(dailyAvg - dailyBudget) else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular indicator
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (dailyBudget > 0) (dailyAvg / dailyBudget).toFloat().coerceIn(0f, 1f) else 0f

                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = when {
                        isOverBudget -> Color(0xFFFF5252)
                        progress > 0.8f -> Color(0xFFFFA726)
                        else -> Color(0xFF4CAF50)
                    },
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.surface,
                )
                Text(
                    text = "₹${"%.0f".format(dailyAvg)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "📅 Daily Average",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "You spend ₹${"%.0f".format(dailyAvg)} per day this month",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (monthlyBudget > 0) {
                    if (isOverBudget) {
                        Text(
                            text = "⚠️ ₹${"%.0f".format(difference)} above daily budget",
                            fontSize = 11.sp,
                            color = Color(0xFFFF5252)
                        )
                        Text(
                            text = "Try to save ₹${"%.0f".format(difference)} per day",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                            text = "✨ ₹${"%.0f".format(difference)} below daily budget",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    Text(
                        text = "💡 Set a monthly budget in Goals",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

fun getDaysInCurrentMonth(): Int {
    val calendar = Calendar.getInstance()
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}
@Composable
fun MonthlyTrendCard(
    monthlyTrends: List<MonthlyData>,
    bestMonth: MonthlyData?,
    worstMonth: MonthlyData?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with icon and subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📊",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Monthly Trend",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Mini insight badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    val trendText = when {
                        monthlyTrends.lastOrNull()?.percentageChange?.let { it < 0 } == true -> "↓ Saving more"
                        monthlyTrends.lastOrNull()?.percentageChange?.let { it > 0 } == true -> "↑ Spending more"
                        else -> "Last 3 months"
                    }
                    Text(
                        text = trendText,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Modern Bar Chart
            val maxSpent = monthlyTrends.maxOfOrNull { it.totalSpent } ?: 1.0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyTrends.forEachIndexed { index, month ->
                    val barHeight = ((month.totalSpent / maxSpent) * 100).dp.coerceAtLeast(30.dp)
                    val barColor = when {
                        month.isIncrease -> Color(0xFFFF5252)
                        month.percentageChange != null && !month.isIncrease -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Amount above bar
                        Text(
                            text = "₹${"%.0f".format(month.totalSpent)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Bar with rounded top
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(barColor)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Month name
                        Text(
                            text = month.monthName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Percentage change chip
                        if (month.percentageChange != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (month.isIncrease)
                                    Color(0xFFFF5252).copy(alpha = 0.1f)
                                else
                                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (month.isIncrease) "▲" else "▼",
                                        fontSize = 8.sp,
                                        color = if (month.isIncrease) Color(0xFFFF5252) else Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = "${"%.0f".format(abs(month.percentageChange))}%",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (month.isIncrease) Color(0xFFFF5252) else Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Best & Worst Months - More visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Best Month Card
                bestMonth?.let {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Best Month",
                                    fontSize = 10.sp,
                                    color = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = it.monthName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${"%.0f".format(it.totalSpent)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Worst Month Card
                worstMonth?.let {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF5252).copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Worst Month",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF5252)
                                )
                                Text(
                                    text = it.monthName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${"%.0f".format(it.totalSpent)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Optional: Motivational message
            if (monthlyTrends.lastOrNull()?.percentageChange?.let { it < -5 } == true) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🎉", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You're spending less than last month! Keep it up!",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

