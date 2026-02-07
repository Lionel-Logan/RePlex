package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a director
 */
data class PlexDirector(
    @SerializedName("tag")
    val tag: String
)
