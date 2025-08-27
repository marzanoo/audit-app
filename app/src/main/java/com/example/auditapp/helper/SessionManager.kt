package com.example.auditapp.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)

    fun saveAuthToken(
        id: Int,
        name: String,
        email: String,
        token: String,
        expiresAt: String,
        role: Int,
        nik: String
    ) {
        with(sharedPreferences.edit()) {
            putInt("ID", id)
            putString("NAME", name)
            putString("EMAIL", email)
            putString("TOKEN", token)
            putString("EXPIRES_AT", expiresAt)
            putInt("ROLE", role)
            putString("NIK", nik)
            apply()
        }
        Log.d("SessionManager", "Saved NIK: $nik, ID: $id, Name: $name, Email: $email, Role: $role")
    }

    fun getNIK(): String? {
        val nik = sharedPreferences.getString("NIK", null)
        Log.d("SessionManager", "Retrieved NIK: $nik")
        return nik
    }

    fun getName(): String? {
        val name = sharedPreferences.getString("NAME", null)
        Log.d("SessionManager", "Retrieved NAME: $name")
        return name
    }

    fun getEmail(): String? {
        val email = sharedPreferences.getString("EMAIL", null)
        Log.d("SessionManager", "Retrieved EMAIL: $email")
        return email
    }

    fun getUserId(): Int {
        val id = sharedPreferences.getInt("ID", -1)
        Log.d("SessionManager", "Retrieved ID: $id")
        return id
    }

    fun getAuthToken(): String? {
        val token = sharedPreferences.getString("TOKEN", null)
        Log.d("SessionManager", "Retrieved TOKEN: $token")
        return token
    }

    fun isTokenExpired(): Boolean {
        val expiresAt = sharedPreferences.getString("EXPIRES_AT", null) ?: return true
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val expiresTime = format.parse(expiresAt)
        val isExpired = expiresTime?.before(Date()) ?: true
        Log.d("SessionManager", "Token expires at: $expiresAt, isExpired: $isExpired")
        return isExpired
    }

    fun clearAuthToken() {
        sharedPreferences.edit().clear().apply()
        Log.d("SessionManager", "Cleared session")
    }

    fun getUserRole(): Int {
        val role = sharedPreferences.getInt("ROLE", -1)
        Log.d("SessionManager", "Retrieved ROLE: $role")
        return role
    }
}