package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a file part (usually one per media item)
 */
data class PlexPart(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("key")
    val key: String,
    
    @SerializedName("file")
    val file: String,
    
    @SerializedName("size")
    val size: Long? = null,
    
    @SerializedName("container")
    val container: String? = null,
    
    @SerializedName("duration")
    val duration: Long? = null,
    
    @SerializedName("Stream")
    val Stream: List<PlexStream>? = null
)
