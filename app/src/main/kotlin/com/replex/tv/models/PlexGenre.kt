package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a genre tag
 */
data class PlexGenre(
    @SerializedName("tag")
    val tag: String
)
