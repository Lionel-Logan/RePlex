package com.replex.tv.api

import com.replex.tv.models.PlexMediaContainer
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
    suspend fun getServerInfo(): Response<PlexMediaContainer>
    
    /**
     * Get all library sections (Movies, TV Shows, etc.)
     */
    @GET("/library/sections")
    suspend fun getLibrarySections(): Response<PlexMediaContainer>
    
    /**
     * Get all items in a specific library section
     * @param sectionId The library section ID
     * @param type Optional type filter (1=movie, 2=show, 4=episode)
     */
    @GET("/library/sections/{id}/all")
    suspend fun getSectionContent(
        @Path("id") sectionId: String,
        @Query("type") type: Int? = null
    ): Response<PlexMediaContainer>
    
    /**
     * Get detailed metadata for a specific item
     * @param ratingKey The unique rating key for the item
     */
    @GET("/library/metadata/{ratingKey}")
    suspend fun getMetadata(
        @Path("ratingKey") ratingKey: String
    ): Response<PlexMediaContainer>
    
    /**
     * Get all hubs for the home screen
     */
    @GET("/hubs/home")
    suspend fun getHomeHubs(): Response<PlexMediaContainer>
    
    /**
     * Get continue watching hub
     */
    @GET("/hubs/home/continueWatching")
    suspend fun getContinueWatching(): Response<PlexMediaContainer>
    
    /**
     * Get recently added content
     */
    @GET("/hubs/home/recentlyAdded")
    suspend fun getRecentlyAdded(): Response<PlexMediaContainer>
    
    /**
     * Get hubs for a specific library section
     * @param sectionId The library section ID
     */
    @GET("/library/sections/{id}/hubs")
    suspend fun getSectionHubs(
        @Path("id") sectionId: String
    ): Response<PlexMediaContainer>
    
    /**
     * Get seasons for a TV show
     * @param ratingKey The TV show's rating key
     */
    @GET("/library/metadata/{ratingKey}/children")
    suspend fun getSeasons(
        @Path("ratingKey") ratingKey: String
    ): Response<PlexMediaContainer>
    
    /**
     * Get episodes for a TV show or season
     * @param ratingKey The show or season's rating key
     */
    @GET("/library/metadata/{ratingKey}/allLeaves")
    suspend fun getEpisodes(
        @Path("ratingKey") ratingKey: String
    ): Response<PlexMediaContainer>
    
    /**
     * Universal search
     * @param query Search query string
     */
    @GET("/hubs/search")
    suspend fun search(
        @Query("query") query: String
    ): Response<PlexMediaContainer>
    
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
