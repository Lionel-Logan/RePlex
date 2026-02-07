package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a media file with technical details
 */
data class PlexMedia(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("duration")
    val duration: Long? = null,
    
    @SerializedName("bitrate")
    val bitrate: Int? = null,
    
    @SerializedName("width")
    val width: Int? = null,
    
    @SerializedName("height")
    val height: Int? = null,
    
    @SerializedName("aspectRatio")
    val aspectRatio: Float? = null,
    
    @SerializedName("audioChannels")
    val audioChannels: Int? = null,
    
    @SerializedName("audioCodec")
    val audioCodec: String? = null,
    
    @SerializedName("videoCodec")
    val videoCodec: String? = null,
    
    @SerializedName("videoResolution")
    val videoResolution: String? = null, // "1080", "4k", etc.
    
    @SerializedName("container")
    val container: String? = null,
    
    @SerializedName("videoFrameRate")
    val videoFrameRate: String? = null,
    
    @SerializedName("Part")
    val Part: List<PlexPart>? = null
)
