package com.replex.tv.auth

import com.google.gson.annotations.SerializedName

/**
 * Response from PIN generation and checking
 */
data class PinResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("code")
    val code: String,
    
    @SerializedName("qr")
    val qr: String? = null,
    
    @SerializedName("product")
    val product: String? = null,
    
    @SerializedName("trusted")
    val trusted: Boolean? = null,
    
    @SerializedName("clientIdentifier")
    val clientIdentifier: String? = null,
    
    @SerializedName("location")
    val location: Location? = null,
    
    @SerializedName("expiresIn")
    val expiresIn: Int? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("expiresAt")
    val expiresAt: String? = null,
    
    @SerializedName("authToken")
    val authToken: String? = null,
    
    @SerializedName("newRegistration")
    val newRegistration: Boolean? = null
) {
    data class Location(
        @SerializedName("code")
        val code: String? = null,
        
        @SerializedName("country")
        val country: String? = null,
        
        @SerializedName("city")
        val city: String? = null,
        
        @SerializedName("subdivisions")
        val subdivisions: String? = null,
        
        @SerializedName("coordinates")
        val coordinates: String? = null
    )
}
