package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Wrapper for Plex API responses
 */
data class PlexResponse(
    @SerializedName("MediaContainer")
    val MediaContainer: PlexMediaContainer
)

/**
 * Root container for Plex API responses
 */
data class PlexMediaContainer(
    @SerializedName("size")
    val size: Int = 0,
    
    @SerializedName("Metadata")
    val Metadata: List<PlexMetadata>? = null,
    
    @SerializedName("Directory")
    val Directory: List<PlexDirectory>? = null,
    
    @SerializedName("Hub")
    val Hub: List<PlexHub>? = null,
    
    @SerializedName("title1")
    val title1: String? = null,
    
    @SerializedName("title2")
    val title2: String? = null
)
