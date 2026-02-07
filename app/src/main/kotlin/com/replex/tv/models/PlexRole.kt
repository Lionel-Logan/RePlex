package com.replex.tv.models

import com.google.gson.annotations.SerializedName

/**
 * Represents an actor/role
 */
data class PlexRole(
    @SerializedName("tag")
    val tag: String,
    
    @SerializedName("role")
    val role: String? = null,
    
    @SerializedName("thumb")
    val thumb: String? = null
)
