package com.replex.tv.auth

import com.google.gson.annotations.SerializedName

/**
 * Request body for PIN generation
 * Note: strong=false generates 4-digit PINs for manual entry at plex.tv/link
 *       strong=true generates 25-char codes for QR/automatic flows
 */
data class PinRequest(
    @SerializedName("strong")
    val strong: Boolean = false
)
