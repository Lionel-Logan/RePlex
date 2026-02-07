package com.replex.tv

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.replex.tv.ui.auth.AuthFragment
import com.replex.tv.utils.TokenManager
import timber.log.Timber

/**
 * Main entry point for RePlex
 * 
 * This activity will handle:
 * - Checking authentication status
 * - Routing to auth screen or home screen
 * - Initializing PlexClient
 */
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d("RePlex", "========== MainActivity onCreate ==========")
        Timber.d("MainActivity created")
        
        // Check if user is authenticated
        if (!TokenManager.hasToken(this)) {
            Log.d("RePlex", "No token found - showing auth")
            Timber.d("No auth token found, showing auth screen")
            showAuthScreen()
        } else {
            Log.d("RePlex", "Token exists - showing home")
            Timber.d("Auth token found, showing home screen")
            showHomeScreen()
        }
    }
    
    private fun showAuthScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AuthFragment.newInstance())
            .commit()
    }
    
    private fun showHomeScreen() {
        // Placeholder until we implement Phase 2
        val textView = TextView(this).apply {
            text = "🎉 Authentication Successful!\n\nHome screen coming in Phase 2..."
            textSize = 24f
            setTextColor(getColor(android.R.color.white))
            gravity = android.view.Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }
        
        findViewById<android.view.ViewGroup>(R.id.fragment_container).apply {
            removeAllViews()
            addView(textView)
        }
    }
}
