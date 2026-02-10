package com.replex.tv

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.replex.tv.api.PlexClient
import com.replex.tv.ui.auth.AuthFragment
import com.replex.tv.ui.browse.HomeFragment
import com.replex.tv.utils.ClientIdGenerator
import com.replex.tv.utils.TokenManager
import kotlinx.coroutines.launch

/**
 * Main entry point for RePlex
 * 
 * This activity will handle:
 * - Checking authentication status
 * - Routing to auth screen or home screen
 * - Initializing PlexClient with server discovery
 */
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        android.util.Log.i("RePlex", "========== MainActivity onCreate ==========")
        
        // Check if user is authenticated
        if (!TokenManager.hasToken(this)) {
            android.util.Log.i("RePlex", "No token found - showing auth")
            showAuthScreen()
        } else {
            android.util.Log.i("RePlex", "Token exists - showing home")
            discoverAndInitializeServer()
        }
    }
    
    private fun showAuthScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AuthFragment.newInstance())
            .commit()
    }
    
    private fun discoverAndInitializeServer() {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getToken(this@MainActivity)
                if (token != null) {
                    // Use PlexAuthService to discover servers
                    val clientId = ClientIdGenerator.getClientId(this@MainActivity)
                    val authService = com.replex.tv.auth.PlexAuthService(clientId)
                    val result = authService.getResources(token)
                    
                    if (result.isSuccess) {
                        val resources = result.getOrNull() ?: emptyList()
                        // Find the first server
                        val server = resources.firstOrNull { it.provides?.contains("server") == true }
                        
                        if (server != null) {
                            // Build local HTTP URL from address and port
                            val localConnection = server.connections.firstOrNull { it.local && it.address != null }
                            
                            val serverUrl = if (localConnection != null && localConnection.address != null) {
                                // Use plain HTTP with local IP address
                                val scheme = if (localConnection.protocol == "http") "http" else "http"  // Force HTTP
                                val address = localConnection.address
                                val port = localConnection.port ?: 32400
                                "$scheme://$address:$port"
                            } else {
                                // Fallback to any connection URI
                                server.connections.firstOrNull()?.uri ?: "http://192.168.1.42:32400"
                            }
                            
                            android.util.Log.i("RePlex", "========== SERVER DISCOVERY ==========")
                            android.util.Log.i("RePlex", "Server Name: ${server.name}")
                            android.util.Log.i("RePlex", "Server URL: $serverUrl")
                            android.util.Log.i("RePlex", "Original URI: ${localConnection?.uri}")
                            android.util.Log.i("RePlex", "Local: ${localConnection?.local}")
                            android.util.Log.i("RePlex", "Protocol: ${localConnection?.protocol}")
                            android.util.Log.i("RePlex", "Address: ${localConnection?.address}")
                            android.util.Log.i("RePlex", "Port: ${localConnection?.port}")
                            android.util.Log.i("RePlex", "======================================")
                            initializeAndShowHome(serverUrl, clientId)
                            return@launch
                        }
                    }
                }
                
                // Fallback to common IPs if discovery fails
                android.util.Log.w("RePlex", "Server discovery failed, using fallback")
                val clientId = ClientIdGenerator.getClientId(this@MainActivity)
                initializeAndShowHome("http://192.168.1.100:32400", clientId)
            } catch (e: Exception) {
                android.util.Log.e("RePlex", "Error during server discovery", e)
                // Fallback
                val clientId = ClientIdGenerator.getClientId(this@MainActivity)
                initializeAndShowHome("http://192.168.1.100:32400", clientId)
            }
        }
    }
    
    private fun initializeAndShowHome(serverUrl: String, clientId: String) {
        if (!PlexClient.isInitialized()) {
            try {
                PlexClient.initialize(this, serverUrl, clientId)
                android.util.Log.i("RePlex", "PlexClient initialized with URL: $serverUrl")
            } catch (e: Exception) {
                android.util.Log.e("RePlex", "Failed to initialize PlexClient", e)
            }
        }
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment.newInstance())
            .commit()
    }
}
