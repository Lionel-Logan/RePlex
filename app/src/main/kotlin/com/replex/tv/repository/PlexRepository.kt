package com.replex.tv.repository

import com.replex.tv.api.PlexClient
import com.replex.tv.models.PlexDirectory
import com.replex.tv.models.PlexHub
import com.replex.tv.models.PlexMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository for Plex data operations
 * Abstracts API calls and handles caching
 */
class PlexRepository {
    
    /**
     * Get continue watching content (on deck)
     */
    suspend fun getContinueWatching(): List<PlexMetadata> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.i("RePlex", "Fetching on deck from ${PlexClient.getBaseUrl()}/library/onDeck")
            val response = PlexClient.api.getContinueWatching()
            if (response.isSuccessful) {
                val container = response.body()?.MediaContainer
                val items = container?.Metadata ?: emptyList()
                // Filter out items with missing essential metadata
                val validItems = items.filter { it.title.isNotEmpty() && it.thumb != null }
                android.util.Log.i("RePlex", "Successfully fetched ${validItems.size} on deck items (${items.size - validItems.size} filtered)")
                validItems
            } else {
                android.util.Log.e("RePlex", "Failed to fetch on deck: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("RePlex", "Exception fetching on deck: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Get recently added content from all movie and TV sections
     */
    suspend fun getRecentlyAdded(): List<PlexMetadata> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.i("RePlex", "Fetching recently added from all sections")
            val sections = getLibrarySections()
            val movieAndTvSections = sections.filter { it.type == "movie" || it.type == "show" }
            
            val allRecentlyAdded = mutableListOf<PlexMetadata>()
            movieAndTvSections.forEach { section ->
                try {
                    val response = PlexClient.api.getSectionRecentlyAdded(section.key)
                    if (response.isSuccessful) {
                        val container = response.body()?.MediaContainer
                        val items = container?.Metadata ?: emptyList()
                        // Filter out items with missing essential metadata
                        val validItems = items.filter { it.title.isNotEmpty() && it.thumb != null }
                        android.util.Log.i("RePlex", "Section ${section.title}: ${validItems.size} recently added (${items.size - validItems.size} filtered)")
                        allRecentlyAdded.addAll(validItems)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RePlex", "Error fetching recently added from ${section.title}: ${e.message}")
                }
            }
            
            android.util.Log.i("RePlex", "Total recently added items: ${allRecentlyAdded.size}")
            allRecentlyAdded
        } catch (e: Exception) {
            android.util.Log.e("RePlex", "Exception fetching recently added: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Get library sections (Movies, TV Shows, etc.)
     */
    suspend fun getLibrarySections(): List<PlexDirectory> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.i("RePlex", "Fetching library sections from ${PlexClient.getBaseUrl()}")
            val response = PlexClient.api.getLibrarySections()
            android.util.Log.i("RePlex", "Library sections response: code=${response.code()}, successful=${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val container = response.body()?.MediaContainer
                val sections = container?.Directory ?: emptyList()
                android.util.Log.i("RePlex", "Successfully fetched ${sections.size} library sections")
                
                // Log raw response body for debugging
                if (sections.isEmpty()) {
                    android.util.Log.w("RePlex", "Response body was: ${container}")
                    android.util.Log.w("RePlex", "Directory field: ${container?.Directory}")
                }
                
                sections.forEach { section ->
                    android.util.Log.d("RePlex", "Library section: ${section.title} (type=${section.type}, key=${section.key})")
                }
                sections
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("RePlex", "Failed to fetch library sections: ${response.code()} - $errorBody")
                Timber.e("Failed to fetch sections: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("RePlex", "Exception fetching library sections: ${e.message}", e)
            Timber.e(e, "Error fetching sections")
            throw e
        }
    }
    
    /**
     * Get content from a specific library section
     */
    suspend fun getSectionContent(
        sectionId: String,
        type: Int? = null,
        start: Int = 0,
        size: Int = 50
    ): List<PlexMetadata> = withContext(Dispatchers.IO) {
        try {
            val response = PlexClient.api.getSectionContent(sectionId, type)
            if (response.isSuccessful) {
                val container = response.body()?.MediaContainer
                val items = container?.Metadata ?: emptyList()
                Timber.d("Fetched ${items.size} items from section $sectionId")
                
                // Filter out items with missing essential metadata
                val validItems = items.filter { it.title.isNotEmpty() && it.thumb != null }
                
                // Return paginated subset
                validItems.drop(start).take(size)
            } else {
                Timber.e("Failed to fetch section content: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching section content")
            throw e
        }
    }
    
    /**
     * Get metadata for a specific item
     */
    suspend fun getMetadata(ratingKey: String): PlexMetadata? = withContext(Dispatchers.IO) {
        try {
            val response = PlexClient.api.getMetadata(ratingKey)
            if (response.isSuccessful) {
                val container = response.body()?.MediaContainer
                val metadata = container?.Metadata?.firstOrNull()
                Timber.d("Fetched metadata for $ratingKey")
                metadata
            } else {
                Timber.e("Failed to fetch metadata: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching metadata")
            throw e
        }
    }
    
    /**
     * Search for content
     */
    suspend fun search(query: String): List<PlexMetadata> = withContext(Dispatchers.IO) {
        try {
            val response = PlexClient.api.search(query)
            if (response.isSuccessful) {
                val container = response.body()?.MediaContainer
                val results = container?.Metadata ?: emptyList()
                Timber.d("Search for '$query' returned ${results.size} results")
                results
            } else {
                Timber.e("Search failed: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching")
            throw e
        }
    }
    
    /**
     * Get movies section ID
     */
    suspend fun getMoviesSectionId(): String? = withContext(Dispatchers.IO) {
        val sections = getLibrarySections()
        sections.find { it.type == "movie" }?.key
    }
    
    /**
     * Get TV shows section ID
     */
    suspend fun getTVShowsSectionId(): String? = withContext(Dispatchers.IO) {
        val sections = getLibrarySections()
        sections.find { it.type == "show" }?.key
    }
    
    /**
     * Detect languages from content metadata
     * Returns map of language code to language name
     */
    suspend fun detectLanguages(): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val sections = getLibrarySections()
            val allLanguages = mutableSetOf<String>()
            
            sections.forEach { section ->
                val content = getSectionContent(section.key, size = 100)
                content.forEach { metadata ->
                    // Extract original language from metadata
                    // Plex doesn't always have originalLanguage, so we might need to parse from other fields
                    // For now, we'll use a placeholder
                    metadata.title?.let {
                        // TODO: Parse actual language from TMDB metadata
                        // This requires additional API calls or metadata parsing
                    }
                }
            }
            
            // Return language mappings
            // TODO: Implement proper language detection
            mapOf(
                "en" to "English",
                "hi" to "Hindi",
                "ml" to "Malayalam",
                "ta" to "Tamil",
                "te" to "Telugu"
            )
        } catch (e: Exception) {
            Timber.e(e, "Error detecting languages")
            emptyMap()
        }
    }
    
    /**
     * Get content organized by genres
     * Returns map of genre name to list of content
     */
    suspend fun getContentByGenres(genres: List<String>): Map<String, List<PlexMetadata>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.i("RePlex", "Fetching content by genres: ${genres.joinToString()}")
            
            // Get all movie and TV sections
            val sections = getLibrarySections()
            val movieAndTvSections = sections.filter { it.type == "movie" || it.type == "show" }
            
            val genreMap = mutableMapOf<String, MutableList<PlexMetadata>>()
            genres.forEach { genre -> genreMap[genre] = mutableListOf() }
            
            // Fetch content from each section and categorize by genre
            movieAndTvSections.forEach { section ->
                try {
                    val response = PlexClient.api.getSectionContent(section.key)
                    if (response.isSuccessful) {
                        val container = response.body()?.MediaContainer
                        val items = container?.Metadata ?: emptyList()
                        
                        // Categorize items by their genres
                        items.forEach { item ->
                            // Only include items with valid metadata
                            if (item.title.isNotEmpty() && item.thumb != null) {
                                item.Genre?.forEach { genreTag ->
                                    val genreName = genreTag.tag
                                    if (genreName != null && genreMap.containsKey(genreName)) {
                                        genreMap[genreName]?.add(item)
                                    }
                                }
                            }
                        }
                        
                        android.util.Log.i("RePlex", "Processed ${items.size} items from ${section.title}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RePlex", "Error fetching content from ${section.title}: ${e.message}")
                }
            }
            
            // Convert to immutable map
            genreMap.mapValues { it.value.toList() }
        } catch (e: Exception) {
            android.util.Log.e("RePlex", "Error getting content by genres: ${e.message}", e)
            emptyMap()
        }
    }
    
    /**
     * Get content for a specific genre with pagination
     */
    suspend fun getContentByGenre(
        genre: String,
        page: Int = 0,
        pageSize: Int = 20
    ): List<PlexMetadata> = withContext(Dispatchers.IO) {
        try {
            val sections = getLibrarySections()
            val movieAndTvSections = sections.filter { it.type == "movie" || it.type == "show" }
            
            val genreItems = mutableListOf<PlexMetadata>()
            
            movieAndTvSections.forEach { section ->
                try {
                    val response = PlexClient.api.getSectionContent(section.key)
                    if (response.isSuccessful) {
                        val container = response.body()?.MediaContainer
                        val items = container?.Metadata ?: emptyList()
                        
                        // Filter items by genre and valid metadata
                        val filteredItems = items.filter { item ->
                            item.title.isNotEmpty() && item.thumb != null && 
                            item.Genre?.any { it.tag == genre } == true
                        }
                        
                        genreItems.addAll(filteredItems)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RePlex", "Error fetching $genre from ${section.title}: ${e.message}")
                }
            }
            
            // Return paginated subset
            val startIndex = page * pageSize
            genreItems.drop(startIndex).take(pageSize)
        } catch (e: Exception) {
            android.util.Log.e("RePlex", "Error getting content for genre $genre: ${e.message}", e)
            emptyList()
        }
    }
}
