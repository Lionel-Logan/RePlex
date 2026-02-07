# RePlex - Phased Implementation Plan

## API Reference Summary

Based on the OpenAPI spec and developer.plex.tv documentation, here are the key endpoints we'll use:

### Authentication
- **POST** `/api/v2/pins` - Generate PIN for OAuth flow
- **GET** `/api/v2/pins/{id}` - Check PIN status and get token

### Library Discovery
- **GET** `/` - Server info and capabilities
- **GET** `/library/sections` - Get all library sections (Movies, TV Shows)
- **GET** `/library/sections/{id}/all` - Get all items in a section
- **GET** `/library/metadata/{ratingKey}` - Get detailed metadata for item

### Content Discovery (Hubs)
- **GET** `/hubs/home` - Home screen hubs (Continue Watching, Recently Added, etc.)
- **GET** `/hubs/home/continueWatching` - Continue watching hub
- **GET** `/hubs/home/recentlyAdded` - Recently added content
- **GET** `/library/sections/{id}/hubs` - Section-specific hubs

### Playback
- **GET** `/library/parts/{id}/file` - Get direct play URL
- **POST** `/:/timeline` - Update playback progress
- **GET** `/library/metadata/{id}/children` - Get seasons for TV show
- **GET** `/library/metadata/{id}/grandchildren` - Get episodes for TV show

### Search
- **GET** `/hubs/search` - Universal search endpoint
- **GET** `/library/sections/{id}/all?title={query}` - Section-specific search

---

## Phase 1: Foundation & Authentication
**Goal:** Get the app running with OAuth authentication and basic Plex server connectivity

### Phase 1.1: Project Setup (Day 1-2)
**Deliverable:** Working Android TV app scaffold with dependencies

**Tasks:**
1. **Initialize Android TV Project**
   - Create new Android TV project in Android Studio
   - Setup Gradle with Kotlin DSL
   - Configure `build.gradle.kts`:
     ```kotlin
     dependencies {
         // Networking
         implementation("com.squareup.okhttp3:okhttp:4.12.0")
         implementation("com.squareup.retrofit2:retrofit:2.9.0")
         implementation("com.squareup.retrofit2:converter-gson:2.9.0")
         
         // ExoPlayer
         implementation("androidx.media3:media3-exoplayer:1.2.0")
         implementation("androidx.media3:media3-ui:1.2.0")
         
         // Room Database
         implementation("androidx.room:room-runtime:2.6.1")
         kapt("androidx.room:room-compiler:2.6.1")
         implementation("androidx.room:room-ktx:2.6.1")
         
         // Coroutines
         implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
         
         // Image Loading
         implementation("io.coil-kt:coil:2.5.0")
         
         // Lifecycle
         implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
     }
     ```

2. **Setup Fire TV Manifest**
   - Configure `AndroidManifest.xml` for TV:
     ```xml
     <uses-feature android:name="android.hardware.touchscreen" android:required="false" />
     <uses-feature android:name="android.software.leanback" android:required="true" />
     <application android:banner="@drawable/app_banner">
         <activity android:name=".MainActivity" android:theme="@style/Theme.Leanback">
             <intent-filter>
                 <action android:name="android.intent.action.MAIN" />
                 <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
             </intent-filter>
         </activity>
     </application>
     ```

3. **Create Package Structure**
   ```
   com.replex.tv/
   ├── auth/          # OAuth handling
   ├── api/           # Plex API client
   ├── models/        # Data models
   ├── ui/
   │   ├── browse/    # Browse screens
   │   ├── detail/    # Detail screens
   │   ├── player/    # Playback UI
   │   └── common/    # Shared UI components
   ├── database/      # Room database
   └── utils/         # Utilities
   ```

### Phase 1.2: Plex API Client (Day 2-3)
**Deliverable:** Working API client that can communicate with Plex server

**Tasks:**
1. **Create Data Models**
   ```kotlin
   // models/PlexMediaContainer.kt
   data class PlexMediaContainer(
       val size: Int,
       val Metadata: List<PlexMetadata>? = null,
       val Directory: List<PlexDirectory>? = null
   )
   
   data class PlexMetadata(
       val ratingKey: String,
       val key: String,
       val type: String,
       val title: String,
       val summary: String?,
       val thumb: String?,
       val art: String?,
       val duration: Long?,
       val year: Int?,
       val originallyAvailableAt: String?,
       val Media: List<PlexMedia>? = null,
       // Language metadata
       val audioLanguage: String?,
       val subtitleLanguage: String?
   )
   
   data class PlexMedia(
       val videoResolution: String?,
       val videoCodec: String?,
       val audioCodec: String?,
       val Part: List<PlexPart>? = null
   )
   
   data class PlexPart(
       val file: String,
       val key: String,
       val Stream: List<PlexStream>? = null
   )
   
   data class PlexStream(
       val streamType: Int, // 1=video, 2=audio, 3=subtitle
       val codec: String?,
       val language: String?,
       val displayTitle: String?,
       val selected: Boolean?
   )
   ```

2. **Build Retrofit API Interface**
   ```kotlin
   // api/PlexApiService.kt
   interface PlexApiService {
       @GET("/")
       suspend fun getServerInfo(): Response<PlexMediaContainer>
       
       @GET("/library/sections")
       suspend fun getLibrarySections(): Response<PlexMediaContainer>
       
       @GET("/library/sections/{id}/all")
       suspend fun getSectionContent(
           @Path("id") sectionId: String,
           @Query("type") type: Int? = null
       ): Response<PlexMediaContainer>
       
       @GET("/library/metadata/{ratingKey}")
       suspend fun getMetadata(@Path("ratingKey") ratingKey: String): Response<PlexMediaContainer>
       
       @GET("/hubs/home")
       suspend fun getHomeHubs(): Response<PlexMediaContainer>
   }
   ```
   
   **Note:** Use `Log.d("RePlex", "API call: <endpoint>")` for debugging API calls

3. **Create API Client Singleton**
   ```kotlin
   // api/PlexClient.kt
   object PlexClient {
       private lateinit var baseUrl: String
       private lateinit var authToken: String
       
       private val client = OkHttpClient.Builder()
           .addInterceptor { chain ->
               val request = chain.request().newBuilder()
                   .addHeader("X-Plex-Token", authToken)
                   .addHeader("X-Plex-Client-Identifier", getClientId())
                   .addHeader("X-Plex-Product", "RePlex")
                   .addHeader("X-Plex-Version", "1.0")
                   .addHeader("X-Plex-Platform", "Android")
                   .addHeader("X-Plex-Platform-Version", Build.VERSION.RELEASE)
                   .addHeader("X-Plex-Device", "FireTV")
                   .addHeader("Accept", "application/json")
                   .build()
               Log.d("RePlex", "API Request: ${request.url}")
               val response = chain.proceed(request)
               Log.d("RePlex", "API Response: ${response.code}")
               response
           }
           .build()
       
       val api: PlexApiService by lazy {
           Retrofit.Builder()
               .baseUrl(baseUrl)
               .client(client)
               .addConverterFactory(GsonConverterFactory.create())
               .build()
               .create(PlexApiService::class.java)
       }
   }
   ```

### Phase 1.3: OAuth Authentication (Day 3-5)
**Deliverable:** Working plex.tv/link authentication flow

**Tasks:**
1. **Create PIN Request/Response Models**
   ```kotlin
   data class PinRequest(val strong: Boolean = true)
   data class PinResponse(
       val id: Int,
       val code: String,
       val authToken: String?
   )
   ```

2. **Build Auth Service**
   ```kotlin
   // auth/PlexAuthService.kt
   class PlexAuthService {
       suspend fun generatePin(): PinResponse {
           // POST to plex.tv/api/v2/pins
       }
       
       suspend fun checkPin(pinId: Int): PinResponse {
           // GET plex.tv/api/v2/pins/{id}
       }
   }
   ```

3. **Create Auth Screen**
   - Display PIN code prominently on TV
   - Show instructions: "Go to plex.tv/link and enter: XXXX"
   - Poll PIN every 2 seconds
   - On success, store token in SharedPreferences
   - Navigate to main app

4. **Token Storage**
   ```kotlin
   // utils/TokenManager.kt
   object TokenManager {
       fun saveToken(context: Context, token: String)
       fun getToken(context: Context): String?
       fun clearToken(context: Context)
   }
   ```

**Milestone:** User can authenticate and app receives valid Plex token

---

## Phase 2: Browse UI & Basic Navigation
**Goal:** Build Netflix-style home screen with horizontal rows and navigation

### Phase 2.1: Home Screen Layout (Day 6-8)
**Deliverable:** Scrollable home screen with hero banner and content rows

**Tasks:**
1. **Create Home Fragment**
   ```kotlin
   // ui/browse/HomeFragment.kt
   class HomeFragment : Fragment() {
       private lateinit var heroView: View
       private lateinit var recyclerView: RecyclerView
       
       override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
           setupHeroBanner()
           setupContentRows()
       }
   }
   ```

2. **Hero Banner Component**
   - Auto-rotating ViewPager2 with 5-second intervals
   - Shows most recently added item
   - Displays: backdrop, title, brief description
   - On focus: show overlay with "Open Details" hint
   - Implement zoom-in focus animation (1.0x → 1.05x scale)

3. **Content Row RecyclerView**
   - Vertical RecyclerView of horizontal RecyclerViews
   - Each row represents a hub (Continue Watching, Action, etc.)
   - Row header with title
   - Horizontal scrolling poster tiles
   - Implement desaturation effect on unfocused rows

4. **Poster Tile Component**
   ```kotlin
   // ui/common/PosterTileView.kt
   class PosterTileView : FrameLayout {
       fun bind(metadata: PlexMetadata) {
           // Load poster with Coil
           // Display title overlay
           // Handle focus animations
       }
   }
   ```

5. **Focus Management**
   - Override `onFocusChange` for each tile
   - Apply scale animation (1.0x → 1.1x)
   - Blur hero banner when scrolling down
   - Desaturate unfocused rows (ColorMatrix with saturation 0.5)

### Phase 2.2: Hub Data Integration (Day 8-10)
**Deliverable:** Home screen populated with real Plex data

**Tasks:**
1. **Create ViewModel**
   ```kotlin
   // ui/browse/HomeViewModel.kt
   class HomeViewModel : ViewModel() {
       private val _hubs = MutableLiveData<List<Hub>>()
       val hubs: LiveData<List<Hub>> = _hubs
       
       fun loadHomeHubs() {
           viewModelScope.launch {
               val response = PlexClient.api.getHomeHubs()
               // Parse hubs and emit to LiveData
           }
       }
   }
   ```

2. **Hub Parsing Logic**
   - Parse `/hubs/home` response
   - Create separate rows for:
     - Continue Watching
     - Recently Added
     - Genre-based recommendations
   - Filter out empty hubs

3. **Image Loading with Caching**
   ```kotlin
   // Use Coil with custom cache configuration
   Coil.imageLoader(context).newBuilder()
       .memoryCache {
           MemoryCache.Builder(context)
               .maxSizePercent(0.25) // 25% of available memory
               .build()
       }
       .diskCache {
           DiskCache.Builder()
               .directory(context.cacheDir.resolve("image_cache"))
               .maxSizeBytes(500 * 1024 * 1024) // 500MB
               .build()
       }
       .build()
   ```

4. **LRU Cache Manager**
   ```kotlin
   // utils/CacheManager.kt
   object CacheManager {
       private const val MAX_CACHE_SIZE = 500 * 1024 * 1024 // 500MB
       
       fun evictOldestEntries() {
           // Implement LRU eviction when cache exceeds limit
       }
   }
   ```

### Phase 2.3: Top Navigation (Day 10-11)
**Deliverable:** Working top-level navigation between Home, Browse, Languages

**Tasks:**
1. **Create Navigation Bar**
   - Horizontal menu at top: Home | Browse | Languages
   - On focus, scale and highlight selected tab
   - Remember last selected tab

2. **Browse Fragment (Movies/TV Shows)**
   ```kotlin
   // ui/browse/BrowseFragment.kt
   class BrowseFragment : Fragment() {
       // Sub-tabs: Movies, TV Shows, Search
       // Grid layout for all items in section
   }
   ```

3. **Languages Fragment**
   - Dedicated screens per language (Hindi, Malayalam, Tamil, etc.)
   - Detect language from Plex metadata
   - Create separate sections for Movies and TV Shows per language

4. **Search Implementation**
   - Search bar in Browse tab
   - Query `/hubs/search?query={text}`
   - Filter by titles, actors, directors
   - Display results in grid

**Milestone:** User can browse content, navigate tabs, and see their library organized by language

---

## Phase 3: Detail Screen & Transitions
**Goal:** Polished detail view with smooth transitions and metadata display

### Phase 3.1: Detail Screen UI (Day 12-14)
**Deliverable:** Full-screen detail view with all metadata

**Tasks:**
1. **Create Detail Fragment**
   ```kotlin
   // ui/detail/DetailFragment.kt
   class DetailFragment : Fragment() {
       private lateinit var backdropView: ImageView
       private lateinit var posterView: ImageView
       private lateinit var titleView: TextView
       private lateinit var metadataView: LinearLayout
       private lateinit var playButton: Button
   }
   ```

2. **Layout Design**
   - Full-screen backdrop with gradient overlay
   - Title (large, white)
   - Metadata row: Year | Duration | Quality | Audio Codec
   - Description (2-3 lines, fade at bottom)
   - Actors (names only, horizontal scroll)
   - Play button (prominent, focused by default)

3. **Metadata Display Logic**
   ```kotlin
   fun displayMetadata(metadata: PlexMetadata) {
       // Quality: "4K", "1080p", etc.
       // Audio: "Dolby TrueHD Atmos", "DTS-HD MA", etc.
       // Extract from Media -> Part -> Stream
   }
   ```

4. **TV Show Handling**
   - Season selector dropdown (current season by default)
   - Vertical episode list below description
   - Episode tiles: thumbnail, title, duration, brief summary
   - Select episode to update play button target

### Phase 3.2: Zoom + Blur Transition (Day 14-16)
**Deliverable:** Smooth transition from browse to detail

**Tasks:**
1. **Shared Element Transition**
   ```kotlin
   // When opening detail from poster tile:
   val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
       requireActivity(),
       posterView,
       "poster_transition"
   )
   ```

2. **Transition Animation**
   - Phase 1: Poster zooms forward (scale 1.0 → 2.0) over 300ms
   - Phase 2: Poster blurs (radius 0 → 25) over 200ms
   - Phase 3: Backdrop fades in behind over 200ms
   - Phase 4: Metadata slides in from bottom over 300ms
   - All animations use `DecelerateInterpolator`

3. **Gradient Overlay Generation**
   ```kotlin
   // Extract dominant color from backdrop
   Palette.from(backdropBitmap).generate { palette ->
       val dominantColor = palette?.getDominantColor(Color.BLACK)
       // Create gradient: transparent → dominantColor → black
       val gradient = GradientDrawable(
           GradientDrawable.Orientation.TOP_BOTTOM,
           intArrayOf(Color.TRANSPARENT, dominantColor, Color.BLACK)
       )
       overlayView.background = gradient
   }
   ```

### Phase 3.3: Horizontal Navigation in Detail (Day 16-17)
**Deliverable:** Swipe between items in same row from detail view

**Tasks:**
1. **Pass Row Data to Detail**
   - When opening detail, pass entire row's items
   - Store current index

2. **Left/Right Navigation**
   - On D-pad left/right, transition to prev/next item
   - Use horizontal slide transition (not zoom+blur)
   - Parallax effect: backdrop slides faster than content
   - Update metadata instantly

3. **Parallax Implementation**
   ```kotlin
   // Backdrop moves 1.5x faster than foreground content
   ObjectAnimator.ofFloat(backdropView, "translationX", 0f, -width * 1.5f)
   ObjectAnimator.ofFloat(contentView, "translationX", 0f, -width)
   ```

**Milestone:** User can explore content details with smooth transitions and navigate between items

---

## Phase 4: ExoPlayer Integration & Playback
**Goal:** Lossless Direct Play with custom playback UI

### Phase 4.1: ExoPlayer Setup (Day 18-20)
**Deliverable:** Basic video playback with Direct Play enforced

**Tasks:**
1. **Create Player Activity**
   ```kotlin
   // ui/player/PlayerActivity.kt
   class PlayerActivity : AppCompatActivity() {
       private lateinit var player: ExoPlayer
       private lateinit var playerView: PlayerView
       
       override fun onCreate(savedInstanceState: Bundle?) {
           initializePlayer()
           prepareMedia()
       }
   }
   ```

2. **ExoPlayer Configuration**
   ```kotlin
   player = ExoPlayer.Builder(this)
       .setAudioAttributes(
           AudioAttributes.Builder()
               .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
               .setUsage(C.USAGE_MEDIA)
               .build(),
           true // Handle audio focus
       )
       .build()
       .apply {
           playWhenReady = true
       }
   ```

3. **Direct Play URL Construction**
   ```kotlin
   fun getDirectPlayUrl(metadata: PlexMetadata): String {
       val part = metadata.Media?.firstOrNull()?.Part?.firstOrNull()
       val key = part?.key ?: throw Exception("No playable part found")
       return "${PlexClient.baseUrl}$key?X-Plex-Token=${TokenManager.getToken(this)}"
   }
   ```

4. **HDMI Passthrough Configuration**
   ```kotlin
   // In AndroidManifest.xml, ensure:
   <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
   
   // In player setup:
   val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
   audioManager.setParameters("use_audio_passthrough=true")
   ```

5. **Enforce Direct Play**
   - Never request transcoding
   - Parse `Media` array for Direct Play compatible stream
   - If no compatible stream found, show error (don't fallback to transcode)

### Phase 4.2: Custom Playback UI (Day 20-22)
**Deliverable:** Custom overlay with controls and codec info

**Tasks:**
1. **Disable Default Controls**
   ```kotlin
   playerView.useController = false
   ```

2. **Create Custom Overlay Layout**
   ```xml
   <!-- overlay_player.xml -->
   <FrameLayout>
       <!-- Bottom controls -->
       <LinearLayout android:gravity="bottom">
           <Button android:id="@+id/playPauseButton" />
           <SeekBar android:id="@+id/timeline" />
           <TextView android:id="@+id/currentTime" />
           <TextView android:id="@+id/duration" />
       </LinearLayout>
       
       <!-- Top right debug info -->
       <TextView 
           android:id="@+id/debugInfo"
           android:layout_gravity="top|end"
           android:textSize="12sp"
           android:text="4K | Dolby TrueHD Atmos" />
   </FrameLayout>
   ```

3. **Overlay Controller Logic**
   ```kotlin
   class PlayerOverlayController(private val player: ExoPlayer) {
       private var isVisible = true
       private val hideHandler = Handler(Looper.getMainLooper())
       
       fun show() {
           overlayView.visibility = View.VISIBLE
           resetHideTimer()
       }
       
       fun hide() {
           overlayView.visibility = View.GONE
       }
       
       private fun resetHideTimer() {
           hideHandler.removeCallbacks(hideRunnable)
           hideHandler.postDelayed(hideRunnable, 5000) // Hide after 5s
       }
   }
   ```

4. **Timeline Scrubber**
   - Custom SeekBar with thumbnail preview (if available)
   - Display time indicator on scrub
   - Support numerical input (if remote has number buttons)

5. **Timestamp Input Dialog**
   ```kotlin
   // Show on long-press of timeline
   class TimestampInputDialog : DialogFragment() {
       // RecyclerView with hour/minute/second pickers
       // Or: Text input if keyboard available
   }
   ```

6. **Audio/Subtitle Selector**
   ```kotlin
   // Parse available streams
   val audioStreams = player.currentTracks.groups
       .filter { it.type == C.TRACK_TYPE_AUDIO }
   val subtitleStreams = player.currentTracks.groups
       .filter { it.type == C.TRACK_TYPE_TEXT }
   
   // Show in overlay menu
   // Display: "Hindi - Dolby TrueHD Atmos 7.1"
   // Sort by quality (TrueHD > DTS-HD > AC3)
   ```

### Phase 4.3: Subtitle Handling (Day 22-23)
**Deliverable:** PGS/ASS subtitle support with fallback

**Tasks:**
1. **Subtitle Priority Logic**
   ```kotlin
   fun selectBestSubtitle(streams: List<PlexStream>): PlexStream? {
       val englishSubs = streams.filter { it.language == "eng" }
       return englishSubs.firstOrNull { it.codec == "pgs" }
           ?: englishSubs.firstOrNull { it.codec == "ass" }
           ?: englishSubs.firstOrNull { it.codec == "srt" }
   }
   ```

2. **ASS Rendering Configuration**
   ```kotlin
   // ExoPlayer supports ASS via libass
   // Ensure styled rendering is enabled
   player.trackSelectionParameters = player.trackSelectionParameters
       .buildUpon()
       .setPreferredTextLanguage("eng")
       .build()
   ```

3. **User Subtitle Toggle**
   - Allow user to switch between available subtitle tracks
   - Remember preference per-content language (not global)

### Phase 4.4: Resume & Timeline Sync (Day 23-24)
**Deliverable:** Resume playback from last position

**Tasks:**
1. **Room Database Schema**
   ```kotlin
   @Entity(tableName = "playback_state")
   data class PlaybackState(
       @PrimaryKey val ratingKey: String,
       val position: Long,
       val duration: Long,
       val lastPlayed: Long
   )
   
   @Dao
   interface PlaybackDao {
       @Insert(onConflict = OnConflictStrategy.REPLACE)
       suspend fun savePosition(state: PlaybackState)
       
       @Query("SELECT * FROM playback_state WHERE ratingKey = :key")
       suspend fun getPosition(key: String): PlaybackState?
   }
   ```

2. **Save Progress on Pause/Exit**
   ```kotlin
   override fun onPause() {
       super.onPause()
       viewModelScope.launch {
           database.playbackDao().savePosition(
               PlaybackState(
                   ratingKey = currentMetadata.ratingKey,
                   position = player.currentPosition,
                   duration = player.duration,
                   lastPlayed = System.currentTimeMillis()
               )
           )
       }
   }
   ```

3. **Resume Dialog**
   - On play button press, check database
   - If position > 5% and < 95%, show dialog:
     - "Resume from 1:23:45" (focused)
     - "Play from Beginning"

4. **Sync to Plex Server**
   ```kotlin
   // POST to /:/timeline to update watch status
   suspend fun updateTimeline(ratingKey: String, position: Long, state: String) {
       PlexClient.api.updateTimeline(
           ratingKey = ratingKey,
           state = state, // "playing", "paused", "stopped"
           time = position,
           duration = player.duration
       )
   }
   ```

**Milestone:** User can play content with lossless audio, custom controls, and resume from last position

---

## Phase 5: Polish & Optimization
**Goal:** Performance optimization, edge case handling, and final UX polish

### Phase 5.1: Performance Optimization (Day 25-26)
**Deliverable:** Smooth 60fps scrolling and instant navigation

**Tasks:**
1. **RecyclerView Optimization**
   ```kotlin
   recyclerView.apply {
       setHasFixedSize(true)
       setItemViewCacheSize(20)
       recycledViewPool.setMaxRecycledViews(0, 20)
   }
   ```

2. **Image Loading Optimization**
   - Use thumbnail URLs for poster tiles (lower resolution)
   - Preload next 3 items in scroll direction
   - Cancel requests when view is recycled

3. **Layout Inflation Optimization**
   - Use `ViewStub` for initially hidden elements
   - Avoid nested layouts (use ConstraintLayout)

4. **Memory Profiling**
   - Use Android Profiler to identify leaks
   - Ensure bitmaps are recycled
   - Monitor cache size, trigger eviction if needed

### Phase 5.2: Edge Case Handling (Day 26-27)
**Deliverable:** Graceful error handling and loading states

**Tasks:**
1. **Network Error Handling**
   - Retry logic with exponential backoff
   - Show error message with "Retry" button
   - Cache last successful response as fallback

2. **Empty States**
   - "No Continue Watching" → show Recently Added instead
   - Empty library → show message to add content in Plex

3. **Loading States**
   - Skeleton screens for home/browse
   - Shimmer effect while loading posters
   - Progress indicator for detail screen

4. **TV Show Edge Cases**
   - Handle single-season shows (skip season selector)
   - Handle missing episode thumbnails (use show poster)

### Phase 5.3: Final UX Polish (Day 27-28)
**Deliverable:** Production-ready user experience

**Tasks:**
1. **Focus Indicators**
   - Subtle glow on focused items
   - Distinct color for focus vs selection

2. **Sound Effects** (Optional)
   - Navigation sounds (subtle ticks)
   - Focus change sounds

3. **Episode Auto-Play**
   - When episode ends, show 10-second countdown
   - "Next episode in 10... 9... 8..."
   - D-pad down to cancel
   - Auto-play next episode if not cancelled

4. **Continue Watching Cleanup**
   - Auto-remove items when 98% watched
   - For TV shows, show next unwatched episode (not last watched)

5. **Settings Screen**
   - Server URL display
   - Token management (logout)
   - Cache statistics + "Clear Cache" button
   - Debug overlay toggle

### Phase 5.4: Testing & Bug Fixes (Day 28-30)
**Deliverable:** Stable, tested application

**Tasks:**
1. **Device Testing**
   - Test on Fire TV Stick 4K
   - Test different content types (movies, multi-season shows)
   - Test different codecs (TrueHD, DTS-HD, AAC)

2. **AVR Testing**
   - Verify passthrough with actual AVR
   - Check codec display matches AVR display
   - Test subtitle rendering

3. **Performance Testing**
   - Large library (1000+ items)
   - Low memory scenarios
   - Rapid navigation stress test

4. **Bug Tracking**
   - Document issues in GitHub/Jira
   - Prioritize critical bugs
   - Fix P0/P1 issues before release

**Milestone:** RePlex is production-ready with polished UX and stable playback

---

## Development Guidelines

### Code Quality Standards
- **Kotlin Style:** Follow Android Kotlin style guide
- **Architecture:** MVVM pattern with Repository layer
- **Error Handling:** Use `Result<T>` sealed class
- **Logging:** Use `Log.d()` only (no Timber) for consistent debugging
- **Testing:** Unit tests for ViewModels, integration tests for API

### Git Workflow
- Feature branches: `feature/phase-X-Y-description`
- Commit messages: `[Phase X.Y] Description`
- Tag releases: `v0.1.0-alpha` after each phase

### Build & Deployment Guidelines
- **Build Process:** Always stop existing builds, clean, then build APK
  ```powershell
  # Stop any running Gradle daemons
  .\gradlew.bat --stop
  # Clean build artifacts
  .\gradlew.bat clean
  # Build debug APK
  .\gradlew.bat assembleDebug
  ```
- **Log Monitoring:** Never use `Select-String` or filtering - always view full error logs
  ```powershell
  # Good: Full logs
  adb logcat
  
  # Bad: Filtered logs (hides critical errors)
  adb logcat | Select-String "error"
  ```
- **Debugging:** Use `Log.d("RePlex", message)` for all logging (no Timber)

### Testing Strategy
- **Unit Tests:** ViewModels, API parsers, utilities
- **UI Tests:** Espresso for critical flows
- **Manual Testing:** Use ADB logcat for debugging with full unfiltered output

### Performance Targets
- **Home Screen Load:** < 1 second
- **Detail Transition:** < 300ms
- **Playback Start:** < 2 seconds (local network)
- **Scroll FPS:** Steady 60fps

---

## Success Criteria

### Phase 1 Complete When:
- [ ] User can authenticate via plex.tv/link
- [ ] App connects to local Plex server
- [ ] Basic API calls work (library sections, metadata)

### Phase 2 Complete When:
- [ ] Home screen displays with hero banner
- [ ] Content rows load with real data
- [ ] Navigation between Home/Browse/Languages works
- [ ] Search returns results

### Phase 3 Complete When:
- [ ] Detail screen shows full metadata
- [ ] Transition from browse to detail is smooth
- [ ] User can navigate between items in detail view
- [ ] TV show seasons/episodes display correctly

### Phase 4 Complete When:
- [ ] Video plays with Direct Play
- [ ] AVR receives lossless audio (verified on device)
- [ ] Custom playback controls work
- [ ] User can resume from last position
- [ ] Subtitles render correctly (PGS/ASS)

### Phase 5 Complete When:
- [ ] App performs smoothly on Fire TV Stick 4K
- [ ] All edge cases handled gracefully
- [ ] Episode auto-play works
- [ ] Settings screen functional
- [ ] No critical bugs remain

---

## Timeline Summary
- **Phase 1:** 5 days (Foundation & Auth)
- **Phase 2:** 6 days (Browse UI)
- **Phase 3:** 6 days (Detail Screen)
- **Phase 4:** 7 days (Playback)
- **Phase 5:** 6 days (Polish)
- **Total:** ~30 days (6 weeks at 5 days/week)

---

## Next Steps
1. Review this plan and confirm approach
2. Set up development environment
3. Begin Phase 1.1: Project Setup
4. Establish daily progress tracking mechanism

Let's build something terrifying and dramatic! 🔥