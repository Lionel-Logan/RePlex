package com.replex.tv.auth

import retrofit2.Response
import retrofit2.http.*

/**
 * Plex.tv authentication API service
 */
interface PlexAuthApiService {
    
    /**
     * Generate a new PIN for OAuth authentication
     */
    @POST("api/v2/pins")
    @Headers("Accept: application/json")
    suspend fun generatePin(
        @Header("X-Plex-Product") product: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Body request: PinRequest = PinRequest()
    ): Response<PinResponse>
    
    /**
     * Check PIN status and retrieve auth token if authenticated
     */
    @GET("api/v2/pins/{id}")
    @Headers("Accept: application/json")
    suspend fun checkPin(
        @Path("id") pinId: Int,
        @Header("X-Plex-Product") product: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Query("code") code: String
    ): Response<PinResponse>
    
    /**
     * Get user's Plex resources (servers)
     */
    @GET("api/v2/resources")
    @Headers("Accept: application/json")
    suspend fun getResources(
        @Header("X-Plex-Token") token: String,
        @Header("X-Plex-Product") product: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Query("includeHttps") includeHttps: Int = 1,
        @Query("includeRelay") includeRelay: Int = 0
    ): Response<List<PlexResource>>
}
