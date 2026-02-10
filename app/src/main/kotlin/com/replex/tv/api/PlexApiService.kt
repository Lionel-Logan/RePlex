package com.replex.tv.api

import com.replex.tv.models.PlexMediaContainer
import com.replex.tv.models.PlexResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Plex API service interface for Retrofit
 */
interface PlexApiService {
    
    /**
     * Get server information and capabilities
     */
    @GET("/")
    suspend fun getServerInfo(): Response<PlexResponse>
    
    /**
     * Get all library sections (Movies, TV Shows, etc.)
     */
    @GET("/library/sections")
    suspend fun getLibrarySections(): Response<PlexResponse>
    
    /**
     * Get all items in a specific library section
     * @param sectionId The library section ID
     * @param type Optional type filter (1=movie, 2=show, 4=episode)
     */
    @GET("/library/sections/{id}/all")
    suspend fun getSectionContent(
        @Path("id") sectionId: String,
        @Query("type") type: Int? = null
    ): Response<PlexResponse>
    
    /**
     * Get content filtered by genre
     * @param sectionId The library section ID
     * @param genre Genre tag ID or name
     */
    @GET("/library/sections/{id}/all")
    suspend fun getSectionContentByGenre(
        @Path("id") sectionId: String,
        @Query("genre") genre: String
    ): Response<PlexResponse>
    
    /**
     * Get detailed metadata for a specific item
     * @param ratingKey The unique rating key for the item
     */
    @GET("/library/metadata/{ratingKey}")
    suspend fun getMetadata(
        @Path("ratingKey") ratingKey: String
    ): Response<PlexResponse>
    
    /**
     * Get all hubs for the home screen
     * Note: X-Plex-Token can be passed as query param or header
     */
    @GET("/hubs/home")
    suspend fun getHomeHubs(
        @Query("X-Plex-Token") token: String? = null
    ): Response<PlexResponse>
    
    /**
     * Get continue watching hub (on deck)
     */
    @GET("/library/onDeck")
    suspend fun getContinueWatching(): Response<PlexResponse>
    
    /**
     * Get recently added content for a specific section
     * @param sectionId The library section ID
     */
    @GET("/library/sections/{id}/recentlyAdded")
    suspend fun getSectionRecentlyAdded(
        @Path("id") sectionId: String
    ): Response<PlexResponse>
    
    /**
     * Get hubs for a specific library section
     * @param sectionId The library section ID
     */
    @GET("/library/sections/{id}/hubs")
    suspend fun getSectionHubs(
        @Path("id") sectionId: String
    ): Response<PlexResponse>
    
    /**
     * Get seasons for a TV show
     * @param ratingKey The TV show's rating key
     */
    @GET("/library/metadata/{ratingKey}/children")
    suspend fun getSeasons(
        @Path("ratingKey") ratingKey: String
    ): Response<PlexResponse>
    
    /**
     * Get episodes for a TV show or season
     * @param ratingKey The show or season's rating key
     */
    @GET("/library/metadata/{ratingKey}/allLeaves")
    suspend fun getEpisodes(
        @Path("ratingKey") ratingKey: String
    ): Response<PlexResponse>
    
    /**
     * Universal search
     * @param query Search query string
     */
    @GET("/hubs/search")
    suspend fun search(
        @Query("query") query: String
    ): Response<PlexResponse>
    
    /**
     * Update playback timeline
     * @param ratingKey The item's rating key
     * @param state Playback state: "playing", "paused", "stopped"
     * @param time Current playback position in milliseconds
     * @param duration Total duration in milliseconds
     */
    @POST("/:/timeline")
    suspend fun updateTimeline(
        @Query("ratingKey") ratingKey: String,
        @Query("state") state: String,
        @Query("time") time: Long,
        @Query("duration") duration: Long
    ): Response<Unit>
}
