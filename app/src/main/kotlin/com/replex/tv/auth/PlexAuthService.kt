package com.replex.tv.auth

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Service for Plex.tv OAuth authentication
 */
class PlexAuthService(private val clientId: String) {
    
    companion object {
        private const val PLEX_TV_URL = "https://plex.tv/"
        private const val PRODUCT_NAME = "RePlex"
    }
    
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.tag("PlexAuth").d(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(PLEX_TV_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    private val api = retrofit.create(PlexAuthApiService::class.java)
    
    /**
     * Generate a new PIN for OAuth flow
     * @return PinResponse containing the PIN code to display to user
     */
    suspend fun generatePin(): Result<PinResponse> {
        return try {
            val response: Response<PinResponse> = api.generatePin(
                product = PRODUCT_NAME,
                clientId = clientId
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to generate PIN: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error generating PIN")
            Result.failure(e)
        }
    }
    
    /**
     * Check the status of a PIN to see if user has authenticated
     * @param pinId The PIN ID from generatePin()
     * @param code The PIN code to verify
     * @return PinResponse with authToken if authenticated, null authToken otherwise
     */
    suspend fun checkPin(pinId: Int, code: String): Result<PinResponse> {
        return try {
            val response: Response<PinResponse> = api.checkPin(
                pinId = pinId,
                product = PRODUCT_NAME,
                clientId = clientId,
                code = code
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to check PIN: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking PIN")
            Result.failure(e)
        }
    }
    
    /**
     * Poll for PIN authentication with automatic retry
     * @param pinId The PIN ID to check
     * @param code The PIN code
     * @param maxAttempts Maximum number of polling attempts (default: 150 = 5 minutes at 2s intervals)
     * @param delayMs Delay between polling attempts in milliseconds (default: 2000ms)
     * @param onPollAttempt Callback invoked on each poll attempt with attempt number
     * @return AuthToken if successful, null if timed out or error
     */
    suspend fun pollForAuth(
        pinId: Int,
        code: String,
        maxAttempts: Int = 150,
        delayMs: Long = 2000,
        onPollAttempt: ((Int) -> Unit)? = null
    ): String? {
        repeat(maxAttempts) { attempt ->
            onPollAttempt?.invoke(attempt + 1)
            
            val result = checkPin(pinId, code)
            if (result.isSuccess) {
                val authToken = result.getOrNull()?.authToken
                if (!authToken.isNullOrBlank()) {
                    Timber.d("Authentication successful after ${attempt + 1} attempts")
                    return authToken
                }
            }
            
            // Wait before next poll
            kotlinx.coroutines.delay(delayMs)
        }
        
        Timber.w("Authentication timed out after $maxAttempts attempts")
        return null
    }
    
    /**
     * Get user's Plex resources (servers and players)
     * @param authToken User's auth token
     * @return List of PlexResource objects
     */
    suspend fun getResources(authToken: String): Result<List<PlexResource>> {
        return try {
            val response = api.getResources(
                token = authToken,
                product = PRODUCT_NAME,
                clientId = clientId
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get resources: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting resources")
            Result.failure(e)
        }
    }
}
