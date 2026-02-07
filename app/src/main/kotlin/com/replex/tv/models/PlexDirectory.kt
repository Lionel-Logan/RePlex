package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a Plex directory/section (e.g., Movies, TV Shows)
 */
data class PlexDirectory(
    @SerializedName("key")
    val key: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("type")
    val type: String? = null, // "movie", "show", etc.
    
    @SerializedName("agent")
    val agent: String? = null,
    
    @SerializedName("scanner")
    val scanner: String? = null,
    
    @SerializedName("language")
    val language: String? = null,
    
    @SerializedName("uuid")
    val uuid: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: Long? = null,
    
    @SerializedName("createdAt")
    val createdAt: Long? = null
)
