package com.charudatta.zorvyn.presentation.goals

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.charudatta.zorvyn.presentation.navigation.Screen
import com.charudatta.zorvyn.ui.theme.ZorvynTheme
import compose.icons.FeatherIcons
import compose.icons.feathericons.Award
import compose.icons.feathericons.RefreshCw
import compose.icons.feathericons.Zap
import kotlin.math.min


private const val WARNING_THRESHOLD = 0.8f
private const val DANGER_THRESHOLD  = 1.0f



@Composable
fun GoalsScreen(
    navController: NavController,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddTransaction.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add transaction")
            }
        }
    ) { scaffoldPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            GoalsHeader()

            GoalModeTabs(
                selectedMode = state.mode,
                onModeSelected = viewModel::setMode
            )

            StreakCard(
                currentStreak = state.currentStreak,
                longestStreak = state.longestStreak,
                noSpendDays   = state.noSpendDaysThisMonth
            )

            AnimatedContent(
                targetState = state.mode,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInHorizontally(
                        tween(300),
                        initialOffsetX = { if (targetState == GoalMode.BUDGET_TRACKER) -it else it }
                    ) togetherWith fadeOut(tween(200))
                },
                label = "mode_transition"
            ) { mode ->
                when (mode) {
                    GoalMode.BUDGET_TRACKER -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            BudgetProgressCard(
                                spent   = state.spentThisMonth,
                                goal    = state.monthlyGoal,
                                insight = state.smartInsight,
                                insightType = state.insightType
                            )
                            SetBudgetCard(
                                input       = state.goalInput,
                                onInputChange = viewModel::setGoalInput,
                                onSave      = viewModel::saveGoal
                            )
                        }
                    }

                    GoalMode.NO_SPEND_CHALLENGE -> {
                        NoSpendChallengeCard(
                            challengeStartMillis = state.challengeStartMillis,
                            currentStreak        = state.currentStreak,
                            longestStreak        = state.longestStreak,
                            noSpendDays          = state.noSpendDaysThisMonth,
                            onStart              = viewModel::startChallenge,
                            onReset              = viewModel::resetChallenge
                        )
                    }
                }
            }

            Spacer(Modifier.height(72.dp))
        }
    }
}


@Composable
private fun GoalsHeader() {
    Text(
        text  = "Goals & Challenges",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun GoalModeTabs(
    selectedMode: GoalMode,
    onModeSelected: (GoalMode) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedMode.ordinal,
        containerColor   = MaterialTheme.colorScheme.surfaceVariant,
        contentColor     = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Tab(
            selected = selectedMode == GoalMode.BUDGET_TRACKER,
            onClick  = { onModeSelected(GoalMode.BUDGET_TRACKER) },
            text     = { Text("Budget") }
        )
        Tab(
            selected = selectedMode == GoalMode.NO_SPEND_CHALLENGE,
            onClick  = { onModeSelected(GoalMode.NO_SPEND_CHALLENGE) },
            text     = { Text("Challenge") }
        )
    }
}


@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int,
    noSpendDays: Int
) {
    val streakColor = when {
        currentStreak >= 7  -> Color(0xFFFF6D00)
        currentStreak >= 3  -> Color(0xFFFFAB00)
        else                -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedStreakFlame(streak = currentStreak, color = streakColor)
                Column {
                    Text(
                        text       = if (currentStreak > 0) "$currentStreak day streak" else "No streak yet",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = streakColor
                    )
                    Text(
                        text  = "Best Streak: $longestStreak days",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "$noSpendDays",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Text(
                    text  = "no-spend days",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AnimatedStreakFlame(streak: Int, color: Color) {
    val scale by rememberInfiniteTransition(label = "flame").animateFloat(
        initialValue  = 1f,
        targetValue   = if (streak > 0) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )
    Icon(
        imageVector        = FeatherIcons.Zap,
        contentDescription = "Streak flame",
        tint               = color,
        modifier           = Modifier.size((36 * scale).dp)
    )
}


@Composable
private fun BudgetProgressCard(
    spent: Double,
    goal: Double,
    insight: String,
    insightType: InsightType
) {
    val progress = if (goal > 0) min(1.0, spent / goal).toFloat() else 0f

    val progressColor = when {
        progress >= DANGER_THRESHOLD  -> MaterialTheme.colorScheme.error
        progress >= WARNING_THRESHOLD -> Color(0xFFFFAB00)
        else                          -> Color(0xFF00C853)
    }

    val insightColor = when (insightType) {
        InsightType.DANGER  -> MaterialTheme.colorScheme.error
        InsightType.WARNING -> Color(0xFFFFAB00)
        InsightType.SUCCESS -> Color(0xFF00C853)
        InsightType.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }


    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label         = "budget_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (goal > 0.0) {


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BudgetStatColumn(
                        label  = "Spent this month",
                        amount = spent
                    )
                    BudgetStatColumn(
                        label  = "Budget",
                        amount = goal,
                        align  = Alignment.End
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .semantics {
                            contentDescription =
                                "Budget used ${(animatedProgress * 100).toInt()}%"
                        },
                    color      = progressColor,
                    trackColor = MaterialTheme.colorScheme.surface,
                )


                Text(
                    text  = "${(progress * 100).toInt()}% of budget used",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )


                Text(
                    text       = insight,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color      = insightColor
                )

            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "No budget set yet add one below",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetStatColumn(
    label: String,
    amount: Double,
    align: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = align) {
        Text(
            text  = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text       = "₹${"%.2f".format(amount)}",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun SetBudgetCard(
    input: String,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val isValid  = input.toDoubleOrNull()?.let { it > 0 } ?: false
    val hasError = input.isNotEmpty() && !isValid

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = "Set Monthly Budget",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value         = input,
                onValueChange = onInputChange,
                label         = { Text("Budget amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                isError       = hasError,
                supportingText = {
                    if (hasError) {
                        Text(
                            text  = "Please enter a valid amount greater than 0",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Button(
                onClick   = onSave,
                enabled   = isValid,
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(10.dp)
            ) {
                Text("Save Budget")
            }
        }
    }
}



@Composable
private fun NoSpendChallengeCard(
    challengeStartMillis: Long,
    currentStreak: Int,
    longestStreak: Int,
    noSpendDays: Int,
    onStart: () -> Unit,
    onReset: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ResetChallengeDialog(
            onConfirm = { showResetDialog = false; onReset() },
            onDismiss = { showResetDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = FeatherIcons.Award,
                    contentDescription = null,
                    tint               = Color(0xFFFFAB00),
                    modifier           = Modifier.size(24.dp)
                )
                Text(
                    text       = "No-Spend Challenge",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text  = "Challenge yourself to go as many days as possible without any expense. Every day you don't spend anything keeps your streak alive.",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChallengeStatChip(
                    label = "Current\nStreak",
                    value = "$currentStreak days",
                    modifier = Modifier.weight(1f)
                )
                ChallengeStatChip(
                    label = "Best\nStreak",
                    value = "$longestStreak days",
                    modifier = Modifier.weight(1f)
                )
                ChallengeStatChip(
                    label = "No Spend Days\n   this month",
                    value = "$noSpendDays",
                    modifier = Modifier.weight(1f)
                )
            }


            val milestone = getMilestoneMessage(currentStreak)
            if (milestone.isNotEmpty()) {
                Text(
                    text       = milestone,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFF00C853)
                )
            }


            if (challengeStartMillis == 0L) {
                Button(
                    onClick  = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Text("Start Challenge Today")
                }
            } else {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = challengeStartMillis }
                val startedOn = "Started ${cal.get(java.util.Calendar.DAY_OF_MONTH)} " +
                        cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.getDefault())
                Text(
                    text  = startedOn,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                OutlinedButton(
                    onClick  = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        FeatherIcons.RefreshCw,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Reset Challenge")
                }
            }
        }
    }
}

@Composable
private fun ChallengeStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = value,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            Text(
                text  = label,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getMilestoneMessage(streak: Int): String = when (streak) {
    1    -> "🌱 First day down! Keep going."
    3    -> "💪 3-day streak! Building the habit."
    7    -> "🔥 One week! You're on fire!"
    14   -> "⚡ Two weeks! Incredible discipline."
    30   -> "🏆 30 days! You're a savings legend."
    else -> ""
}


@Composable
private fun ResetChallengeDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Reset Challenge?") },
        text    = { Text("This will clear your current streak and start fresh. Your longest streak record will also be reset.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Preview(showBackground = true, name = "Budget — under limit")
@Composable
private fun PreviewBudgetUnder() {
    ZorvynTheme {
        BudgetProgressCard(
            spent       = 3200.0,
            goal        = 6000.0,
            insight     = "₹2800 remaining — ₹140/day",
            insightType = InsightType.NEUTRAL
        )
    }
}

@Preview(showBackground = true, name = "Budget — warning zone")
@Composable
private fun PreviewBudgetWarning() {
    ZorvynTheme {
        BudgetProgressCard(
            spent       = 5100.0,
            goal        = 6000.0,
            insight     = "₹900 left for 5 days — ₹180/day",
            insightType = InsightType.WARNING
        )
    }
}

@Preview(showBackground = true, name = "Budget — exceeded")
@Composable
private fun PreviewBudgetExceeded() {
    ZorvynTheme {
        BudgetProgressCard(
            spent       = 6800.0,
            goal        = 6000.0,
            insight     = "Budget exceeded by ₹800. Review your expenses.",
            insightType = InsightType.DANGER
        )
    }
}

@Preview(showBackground = true, name = "Streak Card — active")
@Composable
private fun PreviewStreakCard() {
    ZorvynTheme {
        StreakCard(currentStreak = 7, longestStreak = 12, noSpendDays = 14)
    }
}

@Preview(showBackground = true, name = "No-Spend Challenge")
@Composable
private fun PreviewChallenge() {
    ZorvynTheme {
        NoSpendChallengeCard(
            challengeStartMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L),
            currentStreak        = 7,
            longestStreak        = 12,
            noSpendDays          = 14,
            onStart              = {},
            onReset              = {}
        )
    }
}