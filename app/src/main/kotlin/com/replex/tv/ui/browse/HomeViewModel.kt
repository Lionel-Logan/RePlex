package com.replex.tv.ui.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.replex.tv.models.PlexHub
import com.replex.tv.models.PlexMetadata
import com.replex.tv.repository.PlexRepository
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for home screen data
 */
class HomeViewModel : ViewModel() {
    
    private val repository = PlexRepository()
    
    private val _heroItems = MutableLiveData<List<PlexMetadata>>()
    val heroItems: LiveData<List<PlexMetadata>> = _heroItems
    
    private val _contentRows = MutableLiveData<List<ContentRow>>()
    val contentRows: LiveData<List<ContentRow>> = _contentRows
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    data class ContentRow(
        val title: String,
        val items: List<PlexMetadata>,
        val hubKey: String? = null,
        var loadMoreCallback: (() -> Unit)? = null,
        var hasMore: Boolean = false,
        var currentPage: Int = 0
    )
    
    init {
        loadHomeContent()
    }
    
    fun loadHomeContent() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                // Get continue watching (on deck) and recently added
                val continueWatchingItems = repository.getContinueWatching()
                val recentlyAddedItems = repository.getRecentlyAdded()
                
                android.util.Log.i("RePlex", "Fetched ${continueWatchingItems.size} continue watching items")
                android.util.Log.i("RePlex", "Fetched ${recentlyAddedItems.size} recently added items")
                
                // Use recently added for hero items
                val heroItems = recentlyAddedItems.take(10)
                android.util.Log.i("RePlex", "Using ${heroItems.size} hero items from recently added")
                
                // If both endpoints failed, try loading from library sections
                if (heroItems.isEmpty() && continueWatchingItems.isEmpty()) {
                    android.util.Log.w("RePlex", "API endpoints returned empty, trying library sections")
                    loadFromLibrarySections()
                    return@launch
                }
                
                _heroItems.value = heroItems
                
                // Build content rows
                val rows = mutableListOf<ContentRow>()
                
                // Continue Watching row (from /library/onDeck) - randomized
                if (continueWatchingItems.isNotEmpty()) {
                    rows.add(ContentRow("Continue Watching", continueWatchingItems.shuffled().take(20)))
                }
                
                // Recently Added row - randomized
                if (recentlyAddedItems.isNotEmpty()) {
                    rows.add(ContentRow("Recently Added", recentlyAddedItems.shuffled().take(20)))
                }
                
                // Add genre-based content rows instead of library sections
                val genreRows = repository.getContentByGenres(listOf(
                    "Action",
                    "Drama", 
                    "Comedy",
                    "Sci-Fi",
                    "Thriller",
                    "Horror",
                    "Romance",
                    "Documentary"
                ))
                
                genreRows.forEach { (genre, items) ->
                    if (items.isNotEmpty()) {
                        // Randomize items for each genre row
                        val randomizedItems = items.shuffled()
                        val row = ContentRow(
                            title = genre,
                            items = randomizedItems.take(20),
                            hasMore = randomizedItems.size > 20,
                            currentPage = 0
                        )
                        
                        // Setup load more callback for infinite scrolling
                        row.loadMoreCallback = {
                            loadMoreForGenre(genre, row)
                        }
                        
                        rows.add(row)
                    }
                }
                
                _contentRows.value = rows
                android.util.Log.i("RePlex", "Received ${heroItems.size} hero items")
                android.util.Log.i("RePlex", "Received ${rows.size} content rows")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to load home content")
                android.util.Log.e("RePlex", "HomeViewModel error: ${e.message}", e)
                _error.value = e.message ?: "Failed to load content"
                
                // Fallback to library sections or demo data
                loadFromLibrarySections()
            } finally {
                _loading.value = false
            }
        }
    }
    
    private fun loadMoreForGenre(genre: String, row: ContentRow) {
        viewModelScope.launch {
            try {
                val nextPage = row.currentPage + 1
                val moreItems = repository.getContentByGenre(genre, page = nextPage, pageSize = 20)
                
                if (moreItems.isNotEmpty()) {
                    val updatedItems = row.items.toMutableList().apply {
                        addAll(moreItems)
                    }
                    
                    row.currentPage = nextPage
                    row.hasMore = moreItems.size >= 20
                    
                    // Update the row with new items
                    val updatedRows = _contentRows.value?.toMutableList()
                    val rowIndex = updatedRows?.indexOfFirst { it.title == genre }
                    
                    if (rowIndex != null && rowIndex >= 0) {
                        updatedRows[rowIndex] = row.copy(items = updatedItems)
                        _contentRows.value = updatedRows
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("RePlex", "Error loading more for $genre: ${e.message}")
            }
        }
    }
    
    private suspend fun loadFromLibrarySections() {
        try {
            android.util.Log.i("RePlex", "Loading content from library sections")
            val sections = repository.getLibrarySections()
            android.util.Log.i("RePlex", "Found ${sections.size} library sections")
            
            if (sections.isEmpty()) {
                android.util.Log.w("RePlex", "No library sections found, using demo data")
                loadDemoData()
                return
            }
            
            val allHeroItems = mutableListOf<PlexMetadata>()
            val rows = mutableListOf<ContentRow>()
            
            sections.forEach { section ->
                android.util.Log.i("RePlex", "Loading section: ${section.title} (type=${section.type})")
                val sectionContent = repository.getSectionContent(section.key, size = 20)
                android.util.Log.i("RePlex", "Section ${section.title} has ${sectionContent.size} items")
                
                if (sectionContent.isNotEmpty()) {
                    // Use first 10 items from first section for hero banner
                    if (allHeroItems.isEmpty()) {
                        allHeroItems.addAll(sectionContent.take(10))
                    }
                    
                    // Add as content row
                    rows.add(ContentRow(section.title ?: "Library", sectionContent))
                }
            }
            
            if (allHeroItems.isEmpty()) {
                android.util.Log.w("RePlex", "Library sections are empty, using demo data")
                loadDemoData()
                return
            }
            
            _heroItems.value = allHeroItems
            _contentRows.value = rows
            android.util.Log.i("RePlex", "Loaded ${allHeroItems.size} hero items from libraries")
            android.util.Log.i("RePlex", "Loaded ${rows.size} content rows from libraries")
            
        } catch (e: Exception) {
            android.util.Log.e("RePlex", "Failed to load from library sections: ${e.message}", e)
            loadDemoData()
        }
    }
    
    private fun loadDemoData() {
        android.util.Log.i("RePlex", "Loading demo hero banner data as fallback")
        // Create demo hero items
        val demoHero = listOf(
            PlexMetadata(
                ratingKey = "demo1",
                key = "/library/metadata/demo1",
                title = "The Matrix",
                summary = "Welcome to the Real World - A computer hacker learns from mysterious rebels about the true nature of his reality.",
                type = "movie"
            ),
            PlexMetadata(
                ratingKey = "demo2",
                key = "/library/metadata/demo2",
                title = "Inception",
                summary = "Your Mind is the Scene of the Crime - A thief who steals corporate secrets through dream-sharing technology.",
                type = "movie"
            ),
            PlexMetadata(
                ratingKey = "demo3",
                key = "/library/metadata/demo3",
                title = "Interstellar",
                summary = "Mankind was Born on Earth, It was Never Meant to Die Here - A team of explorers travel through a wormhole in space.",
                type = "movie"
            )
        )
        _heroItems.value = demoHero
        
        val demoRow = ContentRow(
            title = "Featured Content",
            items = demoHero
        )
        _contentRows.value = listOf(demoRow)
        
        android.util.Log.i("RePlex", "Demo data loaded: ${demoHero.size} hero items")
    }
    
    fun retry() {
        loadHomeContent()
    }
}
