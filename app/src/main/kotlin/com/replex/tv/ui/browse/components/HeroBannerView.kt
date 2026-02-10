package com.replex.tv.ui.browse.components

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.replex.tv.R

/**
 * HeroBannerView - Auto-rotating hero banner with parallax backdrop
 * 
 * Features:
 * - ViewPager2 with auto-rotation (7 seconds)
 * - Left/Right D-pad navigation
 * - Parallax backdrop with dynamic gradient
 * - Continues rotating even when unfocused
 * - Smooth page transitions
 */
class HeroBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val viewPager: ViewPager2
    private val backdropImage: ImageView
    private val titleText: TextView
    private val subtitleText: TextView
    private val gradientOverlay: FrameLayout
    
    private val autoRotateHandler = Handler(Looper.getMainLooper())
    private var autoRotateRunnable: Runnable? = null
    private val AUTO_ROTATE_DELAY = 7000L // 7 seconds
    
    private var isUserInteracting = false
    
    init {
        // Make the whole banner view focusable
        isFocusable = true
        isFocusableInTouchMode = true
        
        // Add padding around the hero banner for rounded panel effect
        setPadding(48.dp, 24.dp, 48.dp, 24.dp)
        
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            650.dp // Hero banner height (increased from 550dp)
        )
        
        // Create rounded panel container
        val panelContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            // Rounded background with outline
            background = GradientDrawable().apply {
                setColor(0xFF1A1A1A.toInt()) // Dark gray background
                cornerRadius = 16.dp.toFloat()
                setStroke(3.dp, 0xFF333333.toInt()) // Increased border width to prevent cutoff
            }
            
            // Clip to rounded corners and prevent border cutoff
            clipToOutline = true
            clipToPadding = false
            clipChildren = false
            outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        }
        addView(panelContainer)
        
        // Parallax backdrop layer (inside rounded panel)
        backdropImage = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 0.4f // Dimmed backdrop
        }
        panelContainer.addView(backdropImage)
        
        // Gradient overlay for better text readability (inside rounded panel)
        gradientOverlay = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            background = createGradientDrawable()
        }
        panelContainer.addView(gradientOverlay)
        
        // ViewPager2 for hero items (inside rounded panel)
        viewPager = ViewPager2(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            offscreenPageLimit = 1
            isFocusable = false
            isFocusableInTouchMode = false
            // Allow children to receive focus
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            
            // Add page change callback to update text
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // Notify adapter to update metadata
                    (adapter as? HeroBannerAdapterInterface)?.onPageSelected(position)
                }
            })
        }
        panelContainer.addView(viewPager)
        
        // Title overlay
        titleText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(48.dp, 0, 48.dp, 16.dp)
            }
            textSize = 48f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            typeface = context.resources.getFont(R.font.rajdhani_bold)
        }
        
        // Subtitle overlay (hidden - descriptions removed)
        subtitleText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(48.dp, 0, 48.dp, 48.dp)
            }
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            typeface = context.resources.getFont(R.font.rajdhani_medium)
            alpha = 0.8f
            visibility = View.GONE // Hide subtitle
        }
        
        // Add text overlays
        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.START
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(titleText)
            addView(subtitleText)
        }
        addView(textContainer)
        
        setupAutoRotation()
        setupKeyboardNavigation()
    }
    
    private fun createGradientDrawable(): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                0x00000000, // Transparent at top
                0x99000000.toInt() // Semi-transparent black at bottom
            )
        )
    }
    
    private fun setupAutoRotation() {
        autoRotateRunnable = object : Runnable {
            override fun run() {
                if (!isUserInteracting) {
                    val currentItem = viewPager.currentItem
                    val adapter = viewPager.adapter
                    if (adapter != null && adapter.itemCount > 0) {
                        val nextItem = (currentItem + 1) % adapter.itemCount
                        viewPager.setCurrentItem(nextItem, true)
                    }
                }
                autoRotateHandler.postDelayed(this, AUTO_ROTATE_DELAY)
            }
        }
    }
    
    private fun setupKeyboardNavigation() {
        // Hero banner itself handles focus and key events
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Scale up slightly when focused
                animate()
                    .scaleX(1.02f)
                    .scaleY(1.02f)
                    .setDuration(200)
                    .start()
            } else {
                // Scale back to normal
                animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
        }
        
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        isUserInteracting = true
                        val currentItem = viewPager.currentItem
                        val adapter = viewPager.adapter
                        if (adapter != null && adapter.itemCount > 0) {
                            val prevItem = if (currentItem == 0) {
                                adapter.itemCount - 1
                            } else {
                                currentItem - 1
                            }
                            viewPager.setCurrentItem(prevItem, true)
                        }
                        resetAutoRotateTimer()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        isUserInteracting = true
                        val currentItem = viewPager.currentItem
                        val adapter = viewPager.adapter
                        if (adapter != null && adapter.itemCount > 0) {
                            val nextItem = (currentItem + 1) % adapter.itemCount
                            viewPager.setCurrentItem(nextItem, true)
                        }
                        resetAutoRotateTimer()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    }
    
    private fun resetAutoRotateTimer() {
        autoRotateRunnable?.let {
            autoRotateHandler.removeCallbacks(it)
            autoRotateHandler.postDelayed({
                isUserInteracting = false
            }, 2000) // Resume auto-rotation after 2 seconds of inactivity
            autoRotateHandler.postDelayed(it, AUTO_ROTATE_DELAY)
        }
    }
    
    fun startAutoRotation() {
        autoRotateRunnable?.let {
            autoRotateHandler.postDelayed(it, AUTO_ROTATE_DELAY)
        }
    }
    
    fun stopAutoRotation() {
        autoRotateRunnable?.let {
            autoRotateHandler.removeCallbacks(it)
        }
    }
    
    interface HeroBannerAdapterInterface {
        fun onPageSelected(position: Int)
    }
    
    fun setHeroAdapter(adapter: RecyclerView.Adapter<*>) {
        viewPager.adapter = adapter
        
        // Connect adapter to update metadata
        if (adapter is HeroBannerAdapterInterface) {
            adapter.onPageSelected(0)  // Initialize with first item
        }
    }
    
    fun updateMetadata(title: String, subtitle: String) {
        titleText.text = title
        // Subtitle is hidden, no need to update
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAutoRotation()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAutoRotation()
    }
    
    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()
}
