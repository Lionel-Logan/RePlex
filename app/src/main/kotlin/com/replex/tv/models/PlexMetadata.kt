package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a media item (Movie, TV Show, Episode, etc.)
 */
data class PlexMetadata(
    @SerializedName("ratingKey")
    val ratingKey: String,
    
    @SerializedName("key")
    val key: String,
    
    @SerializedName("type")
    val type: String, // "movie", "show", "season", "episode"
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("summary")
    val summary: String? = null,
    
    @SerializedName("thumb")
    val thumb: String? = null,
    
    @SerializedName("art")
    val art: String? = null,
    
    @SerializedName("duration")
    val duration: Long? = null,
    
    @SerializedName("year")
    val year: Int? = null,
    
    @SerializedName("originallyAvailableAt")
    val originallyAvailableAt: String? = null,
    
    @SerializedName("addedAt")
    val addedAt: Long? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: Long? = null,
    
    @SerializedName("viewCount")
    val viewCount: Int? = null,
    
    @SerializedName("viewOffset")
    val viewOffset: Long? = null,
    
    @SerializedName("Media")
    val Media: List<PlexMedia>? = null,
    
    @SerializedName("Genre")
    val Genre: List<PlexGenre>? = null,
    
    @SerializedName("Director")
    val Director: List<PlexDirector>? = null,
    
    @SerializedName("Writer")
    val Writer: List<PlexWriter>? = null,
    
    @SerializedName("Role")
    val Role: List<PlexRole>? = null,
    
    // Language metadata
    @SerializedName("audioLanguage")
    val audioLanguage: String? = null,
    
    @SerializedName("subtitleLanguage")
    val subtitleLanguage: String? = null,
    
    // TV Show specific
    @SerializedName("parentRatingKey")
    val parentRatingKey: String? = null,
    
    @SerializedName("grandparentRatingKey")
    val grandparentRatingKey: String? = null,
    
    @SerializedName("parentTitle")
    val parentTitle: String? = null,
    
    @SerializedName("grandparentTitle")
    val grandparentTitle: String? = null,
    
    @SerializedName("index")
    val index: Int? = null, // Episode or Season number
    
    @SerializedName("parentIndex")
    val parentIndex: Int? = null // Season number for episodes
)
