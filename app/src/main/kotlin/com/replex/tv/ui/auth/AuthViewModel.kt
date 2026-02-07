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
import timber.log.Timber

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
    
    sealed class AuthState {
        object Loading : AuthState()
        object WaitingForUser : AuthState()
        object Authenticating : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    fun startAuthFlow() {
        Log.d("RePlex", "startAuthFlow called")
        Timber.d("REPLEX: startAuthFlow called")
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                // Generate PIN
                Log.d("RePlex", "About to call authService.generatePin()")
                Timber.d("REPLEX: About to call authService.generatePin()")
                val result = authService.generatePin()
                
                if (result.isSuccess) {
                    val pinResponse = result.getOrNull()!!
                    currentPinId = pinResponse.id
                    currentCode = pinResponse.code
                    
                    // Log what we received
                    Log.d("RePlex", "=== PIN RESPONSE ===")
                    Log.d("RePlex", "  id: ${pinResponse.id}")
                    Log.d("RePlex", "  code length: ${pinResponse.code.length} chars")
                    Log.d("RePlex", "  code value: '${pinResponse.code}'")
                    Log.d("RePlex", "  qr: ${pinResponse.qr}")
                    Log.d("RePlex", "  authToken: ${pinResponse.authToken?.take(20)}...")
                    Log.d("RePlex", "  product: ${pinResponse.product}")
                    Log.d("RePlex", "==================")
                    
                    // Display the full PIN code (4 digits when strong=false)
                    val displayPin = pinResponse.code.uppercase()
                    _pinCode.value = displayPin
                    _authState.value = AuthState.WaitingForUser
                    
                    Log.d("RePlex", "Displaying PIN: $displayPin")
                    Timber.d("PIN generated: ${pinResponse.code}")
                    
                    // Start polling for authentication
                    pollForAuth()
                } else {
                    _authState.value = AuthState.Error("Failed to generate PIN. Please try again.")
                    Timber.e(result.exceptionOrNull(), "Failed to generate PIN")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error. Please check your connection.")
                Timber.e(e, "Error in auth flow")
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
                        Timber.d("Polling attempt: $attempt")
                    }
                )
                
                if (authToken != null) {
                    // Save token
                    TokenManager.saveToken(getApplication(), authToken)
                    _authState.value = AuthState.Success
                    Timber.d("Authentication successful!")
                } else {
                    _authState.value = AuthState.Error("Authentication timed out. Please try again.")
                    Timber.w("Authentication timed out")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Authentication failed. Please try again.")
                Timber.e(e, "Error polling for auth")
            }
        }
    }
    
    fun retry() {
        startAuthFlow()
    }
}
