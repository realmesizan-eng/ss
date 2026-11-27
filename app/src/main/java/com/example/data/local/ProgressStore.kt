package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pathshala_prefs")

data class UserProgressState(
    val userName: String = "",
    val learningGoal: String = "দুটোই", // বাংলা, English, দুটোই
    val onboardingDone: Boolean = false,
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val todayActivities: Int = 0,
    val dailyGoalTarget: Int = 5,
    val totalStars: Int = 0,
    val unlockedReadingLevel: Int = 1,
    val completedLessonIds: Set<String> = emptySet(),
    val earnedBadges: Set<String> = emptySet(),
    val readerDarkMode: Boolean = false,
    val readerFontSizeSp: Int = 22,
    val dailyQuizCount: Int = 0,
    val dailyQuizDate: String = "",
    val dailyAiDefineCount: Int = 0,
    val isPremium: Boolean = false,
    val activeLicenseKey: String = "",
    val adminPin: String = "1234",
    val bkashNumber: String = "01747570144",
    val whatsappNumber: String = "01747570144",
    val licenseFeeTaka: Int = 100
)

class ProgressStore(private val context: Context) {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val LEARNING_GOAL = stringPreferencesKey("learning_goal")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val TODAY_ACTIVITIES = intPreferencesKey("today_activities")
        val TOTAL_STARS = intPreferencesKey("total_stars")
        val UNLOCKED_READING_LEVEL = intPreferencesKey("unlocked_reading_level")
        val COMPLETED_LESSONS = stringSetPreferencesKey("completed_lessons")
        val EARNED_BADGES = stringSetPreferencesKey("earned_badges")
        val READER_DARK_MODE = booleanPreferencesKey("reader_dark_mode")
        val READER_FONT_SIZE = intPreferencesKey("reader_font_size")
        val DAILY_QUIZ_COUNT = intPreferencesKey("daily_quiz_count")
        val DAILY_QUIZ_DATE = stringPreferencesKey("daily_quiz_date")
        val DAILY_AI_DEFINE_COUNT = intPreferencesKey("daily_ai_define_count")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val ACTIVE_LICENSE_KEY = stringPreferencesKey("active_license_key")
        val ADMIN_PIN = stringPreferencesKey("admin_pin")
        val BKASH_NUMBER = stringPreferencesKey("bkash_number")
        val WHATSAPP_NUMBER = stringPreferencesKey("whatsapp_number")
        val LICENSE_FEE = intPreferencesKey("license_fee")
    }

    private fun todayStr(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val state: Flow<UserProgressState> = context.dataStore.data.map { prefs ->
        val today = todayStr()
        val lastDate = prefs[Keys.LAST_ACTIVE_DATE] ?: ""
        val quizDate = prefs[Keys.DAILY_QUIZ_DATE] ?: ""
        UserProgressState(
            userName = prefs[Keys.USER_NAME] ?: "",
            learningGoal = prefs[Keys.LEARNING_GOAL] ?: "দুটোই",
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
            streakDays = prefs[Keys.STREAK_DAYS] ?: 1,
            lastActiveDate = lastDate,
            todayActivities = if (lastDate == today) (prefs[Keys.TODAY_ACTIVITIES] ?: 0) else 0,
            totalStars = prefs[Keys.TOTAL_STARS] ?: 0,
            unlockedReadingLevel = prefs[Keys.UNLOCKED_READING_LEVEL] ?: 1,
            completedLessonIds = prefs[Keys.COMPLETED_LESSONS] ?: emptySet(),
            earnedBadges = prefs[Keys.EARNED_BADGES] ?: emptySet(),
            readerDarkMode = prefs[Keys.READER_DARK_MODE] ?: false,
            readerFontSizeSp = prefs[Keys.READER_FONT_SIZE] ?: 22,
            dailyQuizCount = if (quizDate == today) (prefs[Keys.DAILY_QUIZ_COUNT] ?: 0) else 0,
            dailyQuizDate = quizDate,
            dailyAiDefineCount = if (quizDate == today) (prefs[Keys.DAILY_AI_DEFINE_COUNT] ?: 0) else 0,
            isPremium = prefs[Keys.IS_PREMIUM] ?: false,
            activeLicenseKey = prefs[Keys.ACTIVE_LICENSE_KEY] ?: "",
            adminPin = prefs[Keys.ADMIN_PIN] ?: "1234",
            bkashNumber = prefs[Keys.BKASH_NUMBER] ?: "01747570144",
            whatsappNumber = prefs[Keys.WHATSAPP_NUMBER] ?: "01747570144",
            licenseFeeTaka = prefs[Keys.LICENSE_FEE] ?: 100
        )
    }

    suspend fun saveOnboarding(name: String, goal: String) {
        val today = todayStr()
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name.trim().ifBlank { "শিক্ষার্থী" }
            prefs[Keys.LEARNING_GOAL] = goal
            prefs[Keys.ONBOARDING_DONE] = true
            prefs[Keys.LAST_ACTIVE_DATE] = today
            if ((prefs[Keys.STREAK_DAYS] ?: 0) == 0) prefs[Keys.STREAK_DAYS] = 1
        }
    }

    suspend fun recordActivity(starsEarned: Int = 1, lessonId: String? = null, badge: String? = null) {
        val today = todayStr()
        context.dataStore.edit { prefs ->
            val prevDate = prefs[Keys.LAST_ACTIVE_DATE] ?: ""
            val currentStreak = prefs[Keys.STREAK_DAYS] ?: 1
            if (prevDate.isNotEmpty() && prevDate != today) {
                prefs[Keys.STREAK_DAYS] = currentStreak + 1
                prefs[Keys.TODAY_ACTIVITIES] = 1
            } else {
                if (prevDate.isEmpty()) prefs[Keys.STREAK_DAYS] = 1
                prefs[Keys.TODAY_ACTIVITIES] = (prefs[Keys.TODAY_ACTIVITIES] ?: 0) + 1
            }
            prefs[Keys.LAST_ACTIVE_DATE] = today
            prefs[Keys.TOTAL_STARS] = (prefs[Keys.TOTAL_STARS] ?: 0) + starsEarned

            if (!lessonId.isNullOrBlank()) {
                val updated = (prefs[Keys.COMPLETED_LESSONS] ?: emptySet()) + lessonId
                prefs[Keys.COMPLETED_LESSONS] = updated
            }
            val badges = (prefs[Keys.EARNED_BADGES] ?: emptySet()).toMutableSet()
            if (!badge.isNullOrBlank()) badges.add(badge)
            if ((prefs[Keys.STREAK_DAYS] ?: 0) >= 7) badges.add("৭ দিনের স্ট্রিক")
            if ((prefs[Keys.TOTAL_STARS] ?: 0) >= 20) badges.add("তারকা সংগ্রাহক")
            prefs[Keys.EARNED_BADGES] = badges
        }
    }

    suspend fun unlockReadingLevel(level: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.UNLOCKED_READING_LEVEL] ?: 1
            if (level > current) {
                prefs[Keys.UNLOCKED_READING_LEVEL] = level.coerceIn(1, 5)
            }
        }
    }

    suspend fun incrementQuizCount() {
        val today = todayStr()
        context.dataStore.edit { prefs ->
            val prevDate = prefs[Keys.DAILY_QUIZ_DATE] ?: ""
            val count = if (prevDate == today) (prefs[Keys.DAILY_QUIZ_COUNT] ?: 0) else 0
            prefs[Keys.DAILY_QUIZ_DATE] = today
            prefs[Keys.DAILY_QUIZ_COUNT] = count + 1
        }
    }

    suspend fun incrementAiDefineCount() {
        val today = todayStr()
        context.dataStore.edit { prefs ->
            val prevDate = prefs[Keys.DAILY_QUIZ_DATE] ?: ""
            val count = if (prevDate == today) (prefs[Keys.DAILY_AI_DEFINE_COUNT] ?: 0) else 0
            prefs[Keys.DAILY_QUIZ_DATE] = today
            prefs[Keys.DAILY_AI_DEFINE_COUNT] = count + 1
        }
    }

    suspend fun setReaderPreferences(darkMode: Boolean, fontSizeSp: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.READER_DARK_MODE] = darkMode
            prefs[Keys.READER_FONT_SIZE] = fontSizeSp.coerceIn(16, 36)
        }
    }

    suspend fun activatePremium(licenseKey: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = true
            prefs[Keys.ACTIVE_LICENSE_KEY] = licenseKey.trim().uppercase()
        }
    }

    suspend fun setPremiumStatus(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = enabled
            if (!enabled) prefs[Keys.ACTIVE_LICENSE_KEY] = ""
        }
    }

    suspend fun updateAdminConfig(
        newPin: String,
        bkashNumber: String,
        whatsappNumber: String,
        feeTaka: Int
    ) {
        context.dataStore.edit { prefs ->
            if (newPin.length >= 4) prefs[Keys.ADMIN_PIN] = newPin
            if (bkashNumber.isNotBlank()) prefs[Keys.BKASH_NUMBER] = bkashNumber.trim()
            if (whatsappNumber.isNotBlank()) prefs[Keys.WHATSAPP_NUMBER] = whatsappNumber.trim()
            if (feeTaka > 0) prefs[Keys.LICENSE_FEE] = feeTaka
        }
    }
}
