package com.nfcwallet.app.security

import android.content.Context
import android.content.SharedPreferences

// Stores user preferences for app lock, sensitive data masking, and theme mode
class SecurityPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nfc_wallet_security_prefs", Context.MODE_PRIVATE)

    var isAuthRequired: Boolean
        get() = prefs.getBoolean("require_auth", false)
        set(value) = prefs.edit().putBoolean("require_auth", value).apply()

    var isHideSensitiveInfo: Boolean
        get() = prefs.getBoolean("hide_sensitive_info", false)
        set(value) = prefs.edit().putBoolean("hide_sensitive_info", value).apply()

    // Options: "system", "light", "dark"
    var themeMode: String
        get() = prefs.getString("theme_mode", "system") ?: "system"
        set(value) = prefs.edit().putString("theme_mode", value).apply()
}
