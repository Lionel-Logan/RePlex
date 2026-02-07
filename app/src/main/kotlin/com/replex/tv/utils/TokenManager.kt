package com.replex.tv.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber

/**
 * Manages secure storage of Plex authentication token
 */
object TokenManager {
    
    private const val PREFS_NAME = "replex_secure_prefs"
    private const val KEY_AUTH_TOKEN = "plex_auth_token"
    private const val KEY_SERVER_URL = "plex_server_url"
    private const val KEY_USER_NAME = "plex_user_name"
    
    private fun getSecurePrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create EncryptedSharedPreferences, falling back to regular SharedPreferences")
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Save the Plex authentication token
     */
    fun saveToken(context: Context, token: String) {
        getSecurePrefs(context).edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
        Timber.d("Auth token saved")
    }
    
    /**
     * Retrieve the saved Plex authentication token
     */
    fun getToken(context: Context): String? {
        return getSecurePrefs(context).getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Check if a token exists
     */
    fun hasToken(context: Context): Boolean {
        return !getToken(context).isNullOrBlank()
    }
    
    /**
     * Clear the authentication token (logout)
     */
    fun clearToken(context: Context) {
        getSecurePrefs(context).edit()
            .remove(KEY_AUTH_TOKEN)
            .apply()
        Timber.d("Auth token cleared")
    }
    
    /**
     * Save the Plex server URL
     */
    fun saveServerUrl(context: Context, url: String) {
        getSecurePrefs(context).edit()
            .putString(KEY_SERVER_URL, url)
            .apply()
        Timber.d("Server URL saved: $url")
    }
    
    /**
     * Retrieve the saved server URL
     */
    fun getServerUrl(context: Context): String? {
        return getSecurePrefs(context).getString(KEY_SERVER_URL, null)
    }
    
    /**
     * Save the Plex username
     */
    fun saveUserName(context: Context, userName: String) {
        getSecurePrefs(context).edit()
            .putString(KEY_USER_NAME, userName)
            .apply()
    }
    
    /**
     * Retrieve the saved username
     */
    fun getUserName(context: Context): String? {
        return getSecurePrefs(context).getString(KEY_USER_NAME, null)
    }
    
    /**
     * Clear all stored data
     */
    fun clearAll(context: Context) {
        getSecurePrefs(context).edit().clear().apply()
        Timber.d("All auth data cleared")
    }
}
