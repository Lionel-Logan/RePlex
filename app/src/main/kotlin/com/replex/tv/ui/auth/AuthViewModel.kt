package com.replex.tv.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.replex.tv.auth.PlexAuthService
import com.replex.tv.auth.PinResponse
import com.replex.tv.utils.ClientIdGenerator
import com.replex.tv.utils.TokenManager
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication flow
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authService = PlexAuthService(ClientIdGenerator.getClientId(application))
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _pinCode = MutableLiveData<String>()
    val pinCode: LiveData<String> = _pinCode
    
    private var currentPinId: Int? = null
    private var currentCode: String? = null
    private var retryCount = 0
    private val maxRetries = 3
    
    sealed class AuthState {
        object Loading : AuthState()
        object WaitingForUser : AuthState()
        object Authenticating : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    fun startAuthFlow() {
        Log.i("RePlex", "startAuthFlow called")
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                // Generate PIN
                Log.i("RePlex", "About to call authService.generatePin()")
                val result = authService.generatePin()
                
                if (result.isSuccess) {
                    val pinResponse = result.getOrNull()!!
                    currentPinId = pinResponse.id
                    currentCode = pinResponse.code
                    
                    // Log what we received
                    Log.i("RePlex", "=== PIN RESPONSE ===")
                    Log.i("RePlex", "  id: ${pinResponse.id}")
                    Log.i("RePlex", "  code length: ${pinResponse.code.length} chars")
                    Log.i("RePlex", "  code value: '${pinResponse.code}'")
                    Log.i("RePlex", "  qr: ${pinResponse.qr}")
                    Log.i("RePlex", "  authToken: ${pinResponse.authToken?.take(20)}...")
                    Log.i("RePlex", "  product: ${pinResponse.product}")
                    Log.i("RePlex", "==================")
                    
                    // Display the full PIN code (4 digits when strong=false)
                    val displayPin = pinResponse.code.uppercase()
                    _pinCode.value = displayPin
                    _authState.value = AuthState.WaitingForUser
                    
                    Log.i("RePlex", "Displaying PIN: $displayPin")
                    
                    // Start polling for authentication
                    pollForAuth()
                } else {
                    _authState.value = AuthState.Error("Failed to generate PIN. Please try again.")
                    Log.e("RePlex", "Failed to generate PIN: ${result.exceptionOrNull()}")
                    retryCount = 0
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error. Please check your connection.")
                Log.e("RePlex", "Error in auth flow", e)
                retryCount = 0
            }
        }
    }
    
    private fun pollForAuth() {
        val pinId = currentPinId ?: return
        val code = currentCode ?: return
        
        _authState.value = AuthState.Authenticating
        
        viewModelScope.launch {
            try {
                val authToken = authService.pollForAuth(
                    pinId = pinId,
                    code = code,
                    maxAttempts = 150, // 5 minutes
                    delayMs = 2000,
                    onPollAttempt = { attempt ->
                        Log.i("RePlex", "Polling attempt: $attempt")
                    }
                )
                
                if (authToken != null) {
                    // Save token
                    TokenManager.saveToken(getApplication(), authToken)
                    _authState.value = AuthState.Success
                    Log.i("RePlex", "Authentication successful!")
                } else {
                    // Timeout occurred - retry with new PIN
                    if (retryCount < maxRetries) {
                        retryCount++
                        Log.w("RePlex", "Authentication timed out. Retrying... (attempt $retryCount/$maxRetries)")
                        startAuthFlow()
                    } else {
                        _authState.value = AuthState.Error("Authentication failed after $maxRetries attempts. Please try again.")
                        Log.w("RePlex", "Authentication timed out after max retries")
                        retryCount = 0
                    }
                }
            } catch (e: Exception) {
                // On exception, also retry automatically
                if (retryCount < maxRetries) {
                    retryCount++
                    Log.e("RePlex", "Error polling for auth. Retrying... (attempt $retryCount/$maxRetries): ${e.message}")
                    startAuthFlow()
                } else {
                    _authState.value = AuthState.Error("Authentication failed after $maxRetries attempts. Please try again.")
                    Log.e("RePlex", "Error polling for auth after max retries", e)
                    retryCount = 0
                }
            }
        }
    }
    
    fun retry() {
        retryCount = 0
        startAuthFlow()
    }
}
