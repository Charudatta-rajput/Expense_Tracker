package com.charudatta.zorvyn.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.presentation.navigation.Screen
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowDown
import compose.icons.feathericons.ArrowUp
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronUp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddTransaction.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {


            // Header with date
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = getGreeting(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Text(
                            text = getFormattedDate(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { /* Handle settings/profile */ },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                BalanceCard(
                    balance = state.balance,
                    income = state.totalIncome,
                    expense = state.totalExpense,
                    monthlyGoal = state.monthlyGoal,
                    spentThisMonth = state.spentThisMonth
                )
            }

            // Spending Chart (Visual Element)
            if (state.transactions.isNotEmpty()) {
                item {
                    SpendingChart(transactions = state.transactions)
                }
            }

            // Category Breakdown with Pie Chart style
            if (state.transactions.isNotEmpty()) {
                item {
                    CategoryBreakdown(transactions = state.transactions)
                }
            }

            // Recent Transactions Header with View All button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { navController.navigate(Screen.Transactions.route) }
                    ) {
                        Text(
                            text = "View All",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (state.transactions.isEmpty()) {
                item { EmptyState() }
            } else {
                items(state.transactions.take(5)) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}


@Composable
fun BalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    monthlyGoal: Double,
    spentThisMonth: Double
) {

    val remainingBudget = monthlyGoal - spentThisMonth
    val isOverBudget = remainingBudget < 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Balance",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₹ ${"%,.2f".format(balance)}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Monthly Budget Progress
                if (monthlyGoal > 0) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Monthly Budget Progress",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = if (isOverBudget) "EXCEEDED" else "${(spentThisMonth / monthlyGoal * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverBudget) Color(0xFFFF5252) else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (spentThisMonth / monthlyGoal).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                isOverBudget -> Color(0xFFFF5252)
                                spentThisMonth / monthlyGoal > 0.8f -> Color(0xFFFFAB00)
                                else -> Color(0xFF00C853)
                            },
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )

                        // Show remaining or over budget amount
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isOverBudget)
                                "⚠️ Over budget by ₹ ${"%,.2f".format(-remainingBudget)}"
                            else
                                "₹ ${"%,.2f".format(remainingBudget)} remaining",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BalanceStat(
                        label = "Income",
                        amount = income,
                        icon = FeatherIcons.ArrowDown,
                        tint = Color(0xFF00C853),
                        backgroundColor = Color.White.copy(alpha = 0.2f)
                    )
                    BalanceStat(
                        label = "Expenses",
                        amount = expense,
                        icon = FeatherIcons.ArrowUp,
                        tint = Color(0xFFFF5252),
                        backgroundColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceStat(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    backgroundColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "₹ ${"%,.2f".format(amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun SpendingChart(transactions: List<Transaction>) {
    val weeklyData = getWeeklySpending(transactions)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Spending",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "₹${weeklyData.sumOf { it.second }.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (weeklyData.isNotEmpty()) {
                val maxSpend = weeklyData.maxOfOrNull { it.second } ?: 1.0
                val avgSpend = weeklyData.map { it.second }.average()

                // Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { (day, amount) ->
                        val barHeight = ((amount / maxSpend).coerceIn(0.0, 1.0) * 100).dp
                        val isAboveAvg = amount > avgSpend

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Amount
                            Text(
                                text = "₹${amount.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isAboveAvg)
                                    Color(0xFFFF5252)
                                else
                                    Color(0xFF4CAF50)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (isAboveAvg)
                                            Color(0xFFFF5252).copy(alpha = 0.8f)
                                        else
                                            Color(0xFF4CAF50).copy(alpha = 0.8f)
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Day
                            Text(
                                text = day.take(3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Average line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )
                    Text(
                        text = " Avg ₹${avgSpend.toInt()}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊",
                        fontSize = 40.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No spending data",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdown(transactions: List<Transaction>) {
    val categoryTotals = transactions
        .filter { it.type == "expense" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .entries
        .sortedByDescending { it.value }
        .take(5)

    if (categoryTotals.isEmpty()) return

    val total = categoryTotals.sumOf { it.value }
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF2196F3),
        Color(0xFF9C27B0),
        Color(0xFFF44336)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Category Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Donut-style progress indicators
            categoryTotals.forEachIndexed { index, entry ->
                val percentage = (entry.value / total * 100).toInt()
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors[index % colors.size])
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = entry.key,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${percentage}% (₹ ${"%,.0f".format(entry.value)})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = colors[index % colors.size],
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}





@Composable
fun TransactionItem(transaction: Transaction) {
    var expanded by remember { mutableStateOf(false) }
    val isIncome = transaction.type == "income"
    val amountColor = if (isIncome) Color(0xFF00C853) else Color(0xFFFF5252)
    val amountPrefix = if (isIncome) "+ ₹" else "- ₹"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isIncome) Color(0xFF00C853).copy(alpha = 0.15f)
                            else Color(0xFFFF5252).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = transaction.note.ifBlank { formatDate(transaction.date) },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "$amountPrefix ${"%,.2f".format(transaction.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) FeatherIcons.ChevronUp  else FeatherIcons.ChevronDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = formatFullDate(transaction.date),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    TextButton(onClick = { /* Handle edit/delete */ }) {
                        Text("Details", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "💸", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No transactions yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tap the + button to add your first transaction",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

// Helper functions
fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤️"
        in 17..20 -> "Good Evening 🌙"
        else -> "Good Night 🌃"
    }
}

fun getFormattedDate(): String {
    return SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
}

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
}

fun formatFullDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

fun getWeeklySpending(transactions: List<Transaction>): List<Pair<String, Double>> {
    val calendar = Calendar.getInstance()
    val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
    val currentYear = calendar.get(Calendar.YEAR)

    val weeklyData = mutableMapOf<String, Double>()
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    dayNames.forEach { weeklyData[it] = 0.0 }

    transactions.filter { it.type == "expense" }.forEach { transaction ->
        val date = Date(transaction.date)
        val cal = Calendar.getInstance().apply { time = date }
        if (cal.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
            cal.get(Calendar.YEAR) == currentYear) {
            val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val dayName = dayNames[dayIndex]
            weeklyData[dayName] = weeklyData[dayName]!! + transaction.amount
        }
    }

    return weeklyData.map { Pair(it.key, it.value) }
}



