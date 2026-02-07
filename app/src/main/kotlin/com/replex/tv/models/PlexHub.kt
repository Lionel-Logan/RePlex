package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a content hub (e.g., Continue Watching, Recently Added)
 */
data class PlexHub(
    @SerializedName("hubKey")
    val hubKey: String? = null,
    
    @SerializedName("key")
    val key: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("hubIdentifier")
    val hubIdentifier: String? = null,
    
    @SerializedName("size")
    val size: Int = 0,
    
    @SerializedName("more")
    val more: Boolean = false,
    
    @SerializedName("style")
    val style: String? = null,
    
    @SerializedName("Metadata")
    val Metadata: List<PlexMetadata>? = null
)
