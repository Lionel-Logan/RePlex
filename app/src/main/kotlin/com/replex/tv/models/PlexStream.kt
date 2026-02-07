package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents an audio, video, or subtitle stream
 */
data class PlexStream(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("streamType")
    val streamType: Int, // 1=video, 2=audio, 3=subtitle
    
    @SerializedName("codec")
    val codec: String? = null,
    
    @SerializedName("language")
    val language: String? = null,
    
    @SerializedName("languageCode")
    val languageCode: String? = null,
    
    @SerializedName("displayTitle")
    val displayTitle: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("selected")
    val selected: Boolean? = null,
    
    // Audio specific
    @SerializedName("channels")
    val channels: Int? = null,
    
    @SerializedName("audioChannelLayout")
    val audioChannelLayout: String? = null,
    
    @SerializedName("samplingRate")
    val samplingRate: Int? = null,
    
    @SerializedName("bitrate")
    val bitrate: Int? = null,
    
    // Video specific
    @SerializedName("width")
    val width: Int? = null,
    
    @SerializedName("height")
    val height: Int? = null,
    
    @SerializedName("frameRate")
    val frameRate: Float? = null,
    
    // Subtitle specific
    @SerializedName("format")
    val format: String? = null,
    
    @SerializedName("key")
    val key: String? = null
) {
    companion object {
        const val TYPE_VIDEO = 1
        const val TYPE_AUDIO = 2
        const val TYPE_SUBTITLE = 3
    }
}
