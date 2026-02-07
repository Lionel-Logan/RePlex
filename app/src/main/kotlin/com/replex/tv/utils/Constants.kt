package com.replex.tv.utils

/**
 * Constants used throughout the app
 */
object Constants {
    
    // Plex API
    const val PLEX_TV_URL = "https://plex.tv/"
    const val PLEX_TV_LINK_URL = "https://plex.tv/link"
    
    // Media Types
    const val TYPE_MOVIE = "movie"
    const val TYPE_SHOW = "show"
    const val TYPE_SEASON = "season"
    const val TYPE_EPISODE = "episode"
    
    // Plex Type IDs
    const val TYPE_ID_MOVIE = 1
    const val TYPE_ID_SHOW = 2
    const val TYPE_ID_SEASON = 3
    const val TYPE_ID_EPISODE = 4
    
    // Playback States
    const val STATE_PLAYING = "playing"
    const val STATE_PAUSED = "paused"
    const val STATE_STOPPED = "stopped"
    
    // Cache
    const val MAX_CACHE_SIZE_BYTES = 500L * 1024 * 1024 // 500MB
    const val IMAGE_CACHE_DIR = "image_cache"
    
    // Resume threshold
    const val RESUME_THRESHOLD_PERCENT = 5 // Show resume if > 5% watched
    const val WATCHED_THRESHOLD_PERCENT = 95 // Mark as watched if > 95% watched
    
    // Image sizes
    const val POSTER_WIDTH = 300
    const val POSTER_HEIGHT = 450
    const val BACKDROP_WIDTH = 1920
    const val BACKDROP_HEIGHT = 1080
    const val THUMB_WIDTH = 600
    const val THUMB_HEIGHT = 340
    
    // Languages (ISO 639-2 codes)
    const val LANG_ENGLISH = "eng"
    const val LANG_HINDI = "hin"
    const val LANG_MALAYALAM = "mal"
    const val LANG_TAMIL = "tam"
    const val LANG_TELUGU = "tel"
    const val LANG_KANNADA = "kan"
    
    // Audio Codec Display Names
    val AUDIO_CODEC_NAMES = mapOf(
        "truehd" to "Dolby TrueHD",
        "dca" to "DTS-HD MA",
        "dca-ma" to "DTS-HD MA",
        "eac3" to "Dolby Digital Plus",
        "ac3" to "Dolby Digital",
        "aac" to "AAC",
        "mp3" to "MP3",
        "flac" to "FLAC"
    )
    
    // Video Resolution Display Names
    val VIDEO_RESOLUTION_NAMES = mapOf(
        "4k" to "4K",
        "1080" to "1080p",
        "720" to "720p",
        "sd" to "SD"
    )
}
