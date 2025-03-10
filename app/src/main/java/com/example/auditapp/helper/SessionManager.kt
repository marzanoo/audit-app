package com.example.auditapp.helper

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)

    fun saveAuthToken(id: Int, name: String, email: String, token: String, expiresAt: String, role: Int) {
        with(sharedPreferences.edit()) {
            putInt("ID", id)
            putString("NAME", name)
            putString("EMAIL", email)
            putString("TOKEN", token)
            putString("EXPIRES_AT", expiresAt)
            putInt("ROLE", role)
            apply()
        }
    }

    fun getName(): String? {
        return sharedPreferences.getString("NAME", "")
    }

    fun getEmail(): String? {
        return sharedPreferences.getString("EMAIL", "")
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("ID", -1)
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString("TOKEN", "")
    }

    fun isTokenExpired(): Boolean {
        val expiresAt = sharedPreferences.getString("EXPIRES_AT", null) ?: return true
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val expiresTime = format.parse(expiresAt)
        return expiresTime?.before(Date()) ?: true
    }

    fun clearAuthToken() {
        sharedPreferences.edit().clear().apply()
    }

    fun getUserRole(): Int {
        return sharedPreferences.getInt("ROLE", -1)
    }
}