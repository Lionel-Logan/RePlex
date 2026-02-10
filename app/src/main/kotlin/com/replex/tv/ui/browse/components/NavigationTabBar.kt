package com.replex.tv.ui.browse.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.replex.tv.R
import timber.log.Timber

/**
 * Custom navigation tab bar with expand/collapse functionality
 * 
 * Normal state: Home | Explore | Search
 * Expanded state: Home | Movies | TV Shows | Languages | Search
 */
class NavigationTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private var tabs: List<String> = emptyList()
    private var expandedTabs: List<String> = emptyList()
    private var isExpanded = false
    private var selectedIndex = 0
    private var lastClickedIndex = -1
    private var selectionIndicator: View? = null
    
    private var onTabSelectedListener: ((Int) -> Unit)? = null
    private var onTabExpandListener: (() -> Unit)? = null
    
    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(40.dp, 16.dp, 40.dp, 16.dp) // Reduced from 32dp to 16dp
        isFocusable = false
        clipToPadding = false
        clipChildren = false
        
        setupDefaultTabs()
    }
    
    private fun setupDefaultTabs() {
        expandedTabs = listOf(
            context.getString(R.string.nav_home),
            context.getString(R.string.nav_movies),
            context.getString(R.string.nav_tv_shows),
            context.getString(R.string.nav_languages),
            context.getString(R.string.nav_search)
        )
    }
    
    fun setTabs(tabList: List<String>) {
        tabs = tabList
        rebuildTabs()
    }
    
    fun setOnTabSelectedListener(listener: (Int) -> Unit) {
        onTabSelectedListener = listener
    }
    
    fun setOnTabExpandListener(listener: () -> Unit) {
        onTabExpandListener = listener
    }
    
    private fun rebuildTabs() {
        removeAllViews()
        
        val currentTabs = if (isExpanded) expandedTabs else tabs
        
        currentTabs.forEachIndexed { index, title ->
            val tabView = createTabView(title, index)
            addView(tabView)
            
            // Add spacing between tabs
            if (index < currentTabs.size - 1) {
                addView(createSpacer())
            }
        }
        
        // Focus first tab or selected index
        if (childCount > 0) {
            val indexToFocus = if (selectedIndex * 2 < childCount) selectedIndex * 2 else 0
            getChildAt(indexToFocus)?.requestFocus()
        }
    }
    
    private fun createTabView(title: String, index: Int): TextView {
        return TextView(context).apply {
            text = title
            setTextAppearance(R.style.TextAppearance_TabText)
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            
            setPadding(20.dp, 10.dp, 20.dp, 10.dp)
            
            // Create rounded background drawable
            background = createTabBackground(false)
            
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    animateFocus(this, true)
                    selectedIndex = index
                    animateIndicatorToTab(view)
                } else {
                    animateFocus(this, false)
                }
            }
            
            setOnClickListener {
                handleTabClick(index)
            }
            
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    handleTabClick(index)
                    true
                } else {
                    false
                }
            }
        }
    }
    
    private fun createSpacer(): View {
        return View(context).apply {
            layoutParams = LayoutParams(32.dp, 1)
        }
    }
    
    private fun handleTabClick(index: Int) {
        // Prevent re-triggering if clicking the same tab
        if (lastClickedIndex == index && !(!isExpanded && index == 1)) {
            Timber.d("Tab $index already selected, ignoring")
            return
        }
        
        lastClickedIndex = index
        
        // Check if this is the Explore tab in normal state
        if (!isExpanded && index == 1 && tabs.size == 3) {
            // This is Explore tab - trigger expansion
            onTabExpandListener?.invoke()
        } else {
            onTabSelectedListener?.invoke(index)
        }
    }
    
    fun expandExplore(selectedExpandedIndex: Int = 0) {
        if (isExpanded) return
        
        isExpanded = true
        selectedIndex = selectedExpandedIndex + 1 // +1 because Home is at index 0
        lastClickedIndex = -1 // Reset to allow navigation
        
        animateExpansion {
            rebuildTabs()
        }
    }
    
    fun collapseExplore() {
        if (!isExpanded) return
        
        isExpanded = false
        selectedIndex = 0 // Reset to Home
        lastClickedIndex = -1 // Reset to allow navigation
        
        animateExpansion {
            rebuildTabs()
        }
    }
    
    /**
     * Request focus for a specific tab by index
     */
    fun requestFocusForTab(index: Int) {
        if (index >= 0 && index < childCount) {
            selectedIndex = index
            getChildAt(index)?.requestFocus()
        }
    }
    
    private fun animateExpansion(onComplete: () -> Unit) {
        // Fade out current tabs
        animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                onComplete()
                // Fade in new tabs
                alpha = 0f
                animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
    
    private fun animateFocus(view: TextView, focused: Boolean) {
        val scaleAnimator = ValueAnimator.ofFloat(
            view.scaleX,
            if (focused) 1.15f else 1.0f
        )
        scaleAnimator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            view.scaleX = scale
            view.scaleY = scale
        }
        scaleAnimator.duration = 280
        scaleAnimator.interpolator = DecelerateInterpolator()
        scaleAnimator.start()
        
        // Update background and text appearance
        view.background = createTabBackground(focused)
        
        if (focused) {
            view.setTextAppearance(R.style.TextAppearance_TabTextFocused)
        } else {
            view.setTextAppearance(R.style.TextAppearance_TabText)
        }
    }
    
    private fun createTabBackground(focused: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16.dp.toFloat()
            
            if (focused) {
                // Red outline with glow
                setStroke(2.dp, ContextCompat.getColor(context, R.color.accent_red))
                setColor(ContextCompat.getColor(context, R.color.accent_red_glow))
            } else {
                // Transparent
                setColor(ContextCompat.getColor(context, android.R.color.transparent))
            }
        }
    }
    
    private fun animateIndicatorToTab(targetTab: View) {
        // Smooth slide animation for tab indicator
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 250
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
    
    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()
}
