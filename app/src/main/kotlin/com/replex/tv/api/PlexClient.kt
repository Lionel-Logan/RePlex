package com.replex.tv.api

import android.content.Context
import android.os.Build
import com.google.gson.GsonBuilder
import com.replex.tv.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Singleton Plex API client
 */
object PlexClient {
    
    private var baseUrl: String = ""
    private var context: Context? = null
    private var clientId: String = ""
    
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.tag("PlexAPI").d(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = context?.let { TokenManager.getToken(it) } ?: ""
            
            val request = chain.request().newBuilder()
                .addHeader("X-Plex-Token", token)
                .addHeader("X-Plex-Client-Identifier", clientId)
                .addHeader("X-Plex-Product", "RePlex")
                .addHeader("X-Plex-Version", "1.0.0")
                .addHeader("X-Plex-Platform", "Android")
                .addHeader("X-Plex-Platform-Version", Build.VERSION.RELEASE)
                .addHeader("X-Plex-Device", "Fire TV")
                .addHeader("X-Plex-Device-Name", Build.MODEL)
                .addHeader("Accept", "application/json")
                .build()
            
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private var retrofitInstance: Retrofit? = null
    
    /**
     * Initialize the Plex client
     * @param context Application context
     * @param serverUrl Base URL of the Plex server (e.g., "http://192.168.1.100:32400")
     * @param clientIdentifier Unique client identifier
     */
    fun initialize(context: Context, serverUrl: String, clientIdentifier: String) {
        this.context = context.applicationContext
        this.baseUrl = serverUrl
        this.clientId = clientIdentifier
        
        retrofitInstance = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        Timber.d("PlexClient initialized with server: $serverUrl")
    }
    
    /**
     * Get the Plex API service
     */
    val api: PlexApiService
        get() {
            val retrofit = retrofitInstance 
                ?: throw IllegalStateException("PlexClient not initialized. Call initialize() first.")
            return retrofit.create(PlexApiService::class.java)
        }
    
    /**
     * Check if client is initialized
     */
    fun isInitialized(): Boolean = retrofitInstance != null
    
    /**
     * Get the current base URL
     */
    fun getBaseUrl(): String = baseUrl
    
    /**
     * Build a full image URL from a Plex path
     * @param path The image path from Plex (e.g., "/library/metadata/123/thumb/456")
     * @param width Optional target width for transcoding
     * @param height Optional target height for transcoding
     */
    fun buildImageUrl(path: String?, width: Int? = null, height: Int? = null): String? {
        if (path.isNullOrBlank()) return null
        
        val token = context?.let { TokenManager.getToken(it) } ?: ""
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        
        return buildString {
            append(baseUrl)
            append(cleanPath)
            append("?X-Plex-Token=$token")
            
            if (width != null || height != null) {
                if (width != null) append("&width=$width")
                if (height != null) append("&height=$height")
            }
        }
    }
    
    /**
     * Build a direct play URL for a media part
     * @param key The part key from Plex metadata
     */
    fun buildDirectPlayUrl(key: String): String {
        val token = context?.let { TokenManager.getToken(it) } ?: ""
        val cleanKey = if (key.startsWith("/")) key else "/$key"
        return "$baseUrl$cleanKey?X-Plex-Token=$token"
    }
}
