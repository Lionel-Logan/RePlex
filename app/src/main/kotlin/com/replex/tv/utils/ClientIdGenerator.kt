package com.replex.tv.utils

import android.content.Context
import java.util.UUID

/**
 * Generates and persists a unique client identifier for Plex API authentication
 */
object ClientIdGenerator {
    
    private const val PREFS_NAME = "replex_prefs"
    private const val KEY_CLIENT_ID = "client_id"
    
    /**
     * Get or generate a unique client identifier
     */
    fun getClientId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        var clientId = prefs.getString(KEY_CLIENT_ID, null)
        
        if (clientId.isNullOrBlank()) {
            clientId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_CLIENT_ID, clientId).apply()
        }
        
        return clientId
    }
}
