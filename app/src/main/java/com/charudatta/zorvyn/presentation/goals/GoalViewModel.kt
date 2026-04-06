package com.charudatta.zorvyn.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charudatta.zorvyn.data.local.GoalPreferences
import com.charudatta.zorvyn.domain.model.Transaction
import com.charudatta.zorvyn.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject


private const val WARNING_THRESHOLD = 0.8f
private const val DANGER_THRESHOLD  = 1.0f

enum class GoalMode { BUDGET_TRACKER, NO_SPEND_CHALLENGE }

enum class InsightType { DANGER, WARNING, SUCCESS, NEUTRAL }



data class GoalsState(

    val mode: GoalMode = GoalMode.BUDGET_TRACKER,


    val monthlyGoal: Double    = 0.0,
    val spentThisMonth: Double = 0.0,
    val goalInput: String      = "",


    val currentStreak: Int     = 0,
    val longestStreak: Int     = 0,
    val noSpendDaysThisMonth: Int = 0,
    val challengeStartMillis: Long = 0L,

    val smartInsight: String   = "",
    val insightType: InsightType = InsightType.NEUTRAL
)



@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val goalPreferences: GoalPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    private val _mode = MutableStateFlow(GoalMode.BUDGET_TRACKER)

    init {
        observeData()
    }


    private fun observeData() {
        viewModelScope.launch {
            combine(
                getTransactions(),
                goalPreferences.monthlyGoal,
                goalPreferences.challengeStartDate,
                goalPreferences.longestStreak,
                _mode
            ) { transactions, goal, challengeStart, savedLongest, mode ->

                val startOfMonth = startOfCurrentMonthMillis()
                val now          = System.currentTimeMillis()

                val monthExpenses = transactions.filter {
                    it.type == "expense" && it.date >= startOfMonth
                }
                val spentThisMonth = monthExpenses.sumOf { it.amount }

                val streak      = computeStreak(transactions, now, challengeStart)
                val noSpendDays = computeNoSpendDaysThisMonth(transactions, now)
                val newLongest  = maxOf(savedLongest, streak)


                if (newLongest > savedLongest) {
                    goalPreferences.setLongestStreak(newLongest)
                }

                val (insight, insightType) = buildSmartInsight(
                    spent  = spentThisMonth,
                    goal   = goal,
                    streak = streak,
                    now    = now
                )

                GoalsState(
                    mode                 = mode,
                    monthlyGoal          = goal,
                    spentThisMonth       = spentThisMonth,
                    goalInput            = _state.value.goalInput,
                    currentStreak        = streak,
                    longestStreak        = newLongest,
                    noSpendDaysThisMonth = noSpendDays,
                    challengeStartMillis = challengeStart,
                    smartInsight         = insight,
                    insightType          = insightType
                )
            }.collect { _state.value = it }
        }
    }

    private fun computeStreak(transactions: List<Transaction>, now: Long, challengeStartMillis: Long): Int {
        if (challengeStartMillis == 0L) return 0

        val daysWithExpense: Set<Long> = transactions
            .filter { it.type == "expense" }
            .map { startOfDayMillis(it.date) }
            .toSet()

        var streak       = 0
        var dayStart     = startOfDayMillis(now)
        val challengeDay = startOfDayMillis(challengeStartMillis)

        while (dayStart >= challengeDay) {
            if (dayStart !in daysWithExpense) {
                streak++
                dayStart -= TimeUnit.DAYS.toMillis(1)
            } else {
                return streak
            }
        }
        return streak
    }


    private fun computeNoSpendDaysThisMonth(
        transactions: List<Transaction>,
        now: Long
    ): Int {
        val startOfMonth = startOfCurrentMonthMillis()

        val hasAnyThisMonth = transactions.any { it.date >= startOfMonth }
        if (!hasAnyThisMonth) return 0

        val daysWithExpense: Set<Long> = transactions
            .filter { it.type == "expense" && it.date >= startOfMonth }
            .map { startOfDayMillis(it.date) }
            .toSet()

        val daysPassed = daysBetween(startOfMonth, startOfDayMillis(now)) + 1
        var noSpendCount = 0
        var cursor = startOfMonth

        repeat(daysPassed.toInt()) {
            if (cursor !in daysWithExpense) noSpendCount++
            cursor += TimeUnit.DAYS.toMillis(1)
        }
        return noSpendCount
    }


    private fun buildSmartInsight(
        spent: Double,
        goal: Double,
        streak: Int,
        now: Long
    ): Pair<String, InsightType> {
        if (goal <= 0) return "Set a budget to start tracking" to InsightType.NEUTRAL

        val progress    = (spent / goal).toFloat()
        val remaining   = goal - spent
        val cal         = Calendar.getInstance().apply { timeInMillis = now }
        val daysLeft    = cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH) + 1
        val dailyBudget = if (daysLeft > 0) remaining / daysLeft else 0.0
        val dayOfMonth  = cal.get(Calendar.DAY_OF_MONTH)

        return when {
            progress >= DANGER_THRESHOLD ->
                "Budget exceeded by ₹${"%.0f".format(-remaining)}. Review your expenses." to
                        InsightType.DANGER

            progress >= WARNING_THRESHOLD ->
                "₹${"%.0f".format(remaining)} remaining for $daysLeft days — ₹${"%.0f".format(dailyBudget)}/day" to
                        InsightType.WARNING

            daysLeft == 0 && progress < 0.9f ->
                "Month complete! You saved ₹${"%.0f".format(remaining)}. Great discipline!" to
                        InsightType.SUCCESS

            streak >= 3 ->
                "$streak day no-spend streak! ₹${"%.0f".format(remaining)} remaining" to
                        InsightType.SUCCESS

            dayOfMonth > 20 && progress < 0.7f ->
                "On track to save ₹${"%.0f".format(remaining)} this month!" to
                        InsightType.SUCCESS

            else ->
                "₹${"%.0f".format(remaining)} remaining for $daysLeft days — ₹${"%.0f".format(dailyBudget)}/day" to
                        InsightType.NEUTRAL
        }
    }


    private fun startOfCurrentMonthMillis(): Long =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun startOfDayMillis(epochMillis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun daysBetween(startMillis: Long, endMillis: Long): Long =
        TimeUnit.MILLISECONDS.toDays(endMillis - startMillis)


    fun setMode(mode: GoalMode) {
        _mode.value = mode
    }

    fun setGoalInput(value: String) {
        _state.update { it.copy(goalInput = value) }
    }

    fun saveGoal() {
        val amount = _state.value.goalInput.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            goalPreferences.setMonthlyGoal(amount)
        }
    }

    fun startChallenge() {
        viewModelScope.launch {
            goalPreferences.setChallengeStartDate(System.currentTimeMillis())
        }
    }

    fun resetChallenge() {
        viewModelScope.launch {
            goalPreferences.setChallengeStartDate(0L)
            goalPreferences.setLongestStreak(0)
        }
    }
}