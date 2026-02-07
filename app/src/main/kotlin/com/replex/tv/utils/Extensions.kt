package com.replex.tv.utils

/**
 * Extension functions for common operations
 */

/**
 * Formats duration in milliseconds to HH:MM:SS or MM:SS
 */
fun Long.formatDuration(): String {
    val seconds = this / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}

/**
 * Converts a timestamp to a human-readable relative time
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - (this * 1000) // Plex timestamps are in seconds
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}

/**
 * Checks if a string is a valid URL
 */
fun String.isValidUrl(): Boolean {
    return try {
        java.net.URL(this)
        this.startsWith("http://") || this.startsWith("https://")
    } catch (e: Exception) {
        false
    }
}

/**
 * Safely gets a value from a nullable list at an index
 */
fun <T> List<T>?.safeGet(index: Int): T? {
    return if (this != null && index in indices) this[index] else null
}
