package com.replex.tv.auth

import com.google.gson.annotations.SerializedName

/**
 * Plex resource (server) information
 */
data class PlexResource(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("product")
    val product: String,
    
    @SerializedName("provides")
    val provides: String?,
    
    @SerializedName("clientIdentifier")
    val clientIdentifier: String,
    
    @SerializedName("connections")
    val connections: List<PlexConnection>
)

data class PlexConnection(
    @SerializedName("protocol")
    val protocol: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("port")
    val port: Int,
    
    @SerializedName("uri")
    val uri: String,
    
    @SerializedName("local")
    val local: Boolean
)
