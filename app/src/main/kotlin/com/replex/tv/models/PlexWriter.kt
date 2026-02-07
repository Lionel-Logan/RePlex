package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a writer
 */
data class PlexWriter(
    @SerializedName("tag")
    val tag: String
)
