package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nova_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_MESSAGE_COUNT = "message_count"
        private const val KEY_LAST_DATE = "last_date"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_CUSTOM_DOMAIN = "custom_domain"
        
        const val FREE_LIMIT = 60
    }

    var dailyBonusActivationTime: Long
        get() = prefs.getLong("daily_bonus_activation_time", 0L)
        set(value) = prefs.edit().putLong("daily_bonus_activation_time", value).apply()

    fun isDailyBonusActiveNow(): Boolean {
        val activated = dailyBonusActivationTime
        if (activated == 0L) return false
        val elapsed = System.currentTimeMillis() - activated
        return elapsed < 30 * 60 * 1000 // 30 minutes in milliseconds
    }

    var isPremium: Boolean
        get() = prefs.getBoolean(KEY_IS_PREMIUM, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PREMIUM, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userPassword: String
        get() = prefs.getString(KEY_USER_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_PASSWORD, value).apply()

    var customDomain: String
        get() = prefs.getString(KEY_CUSTOM_DOMAIN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_DOMAIN, value).apply()

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getRemainingMessages(): Int {
        if (isPremium || isDailyBonusActiveNow()) return 99999
        
        val currentDate = getCurrentDateString()
        val lastSavedDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        
        if (currentDate != lastSavedDate) {
            // New day! Reset count
            prefs.edit()
                .putString(KEY_LAST_DATE, currentDate)
                .putInt(KEY_MESSAGE_COUNT, 0)
                .apply()
            return FREE_LIMIT
        }
        
        val currentCount = prefs.getInt(KEY_MESSAGE_COUNT, 0)
        return (FREE_LIMIT - currentCount).coerceAtLeast(0)
    }

    fun incrementMessageCount(): Boolean {
        if (isPremium || isDailyBonusActiveNow()) return true
        
        val remaining = getRemainingMessages()
        if (remaining <= 0) return false // No messages left
        
        val currentCount = prefs.getInt(KEY_MESSAGE_COUNT, 0)
        prefs.edit().putInt(KEY_MESSAGE_COUNT, currentCount + 1).apply()
        return true
    }

    var firstLaunchTime: Long
        get() {
            var time = prefs.getLong("first_launch_time", 0L)
            if (time == 0L) {
                time = System.currentTimeMillis()
                prefs.edit().putLong("first_launch_time", time).apply()
            }
            return time
        }
        set(value) = prefs.edit().putLong("first_launch_time", value).apply()

    fun isTwoMonthsCompleted(): Boolean {
        val launch = firstLaunchTime
        val elapsed = System.currentTimeMillis() - launch
        val sixtyDaysInMs = 60L * 24L * 60L * 60L * 1000L
        return elapsed >= sixtyDaysInMs
    }

    var isFestivalActive: Boolean
        get() = prefs.getBoolean("is_festival_active", false)
        set(value) = prefs.edit().putBoolean("is_festival_active", value).apply()
}
