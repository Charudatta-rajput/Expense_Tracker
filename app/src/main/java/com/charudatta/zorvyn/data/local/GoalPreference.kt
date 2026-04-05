package com.charudatta.zorvyn.data.local

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "goals")

@Singleton
class GoalPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {


    private val MONTHLY_GOAL_KEY       = doublePreferencesKey("monthly_goal")
    private val CHALLENGE_START_KEY    = longPreferencesKey("challenge_start_date")
    private val LONGEST_STREAK_KEY     = intPreferencesKey("longest_streak")


    val monthlyGoal: Flow<Double> = context.dataStore.data
        .map { it[MONTHLY_GOAL_KEY] ?: 0.0 }

    val challengeStartDate: Flow<Long> = context.dataStore.data
        .map { it[CHALLENGE_START_KEY] ?: 0L }

    val longestStreak: Flow<Int> = context.dataStore.data
        .map { it[LONGEST_STREAK_KEY] ?: 0 }


    suspend fun setMonthlyGoal(amount: Double) {
        context.dataStore.edit { it[MONTHLY_GOAL_KEY] = amount }
    }

    suspend fun setChallengeStartDate(epochMillis: Long) {
        context.dataStore.edit { it[CHALLENGE_START_KEY] = epochMillis }
    }

    suspend fun setLongestStreak(value: Int) {
        context.dataStore.edit { it[LONGEST_STREAK_KEY] = value }
    }
}