package com.replex.tv.auth

import com.google.gson.annotations.SerializedName

/**
 * Plex.tv user account information
 */
data class PlexUser(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("uuid")
    val uuid: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("thumb")
    val thumb: String? = null,
    
    @SerializedName("authToken")
    val authToken: String
)
