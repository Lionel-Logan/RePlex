package com.replex.tv.ui.browse.components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.replex.tv.R

/**
 * Custom on-screen keyboard for TV
 * Similar to Hotstar/Netflix keyboard layout
 */
class OnScreenKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private val inputDisplay: TextView
    private var currentInput = StringBuilder()
    private var onTextChangeListener: ((String) -> Unit)? = null
    
    // Vertical keyboard layout: 6 columns (better for TV)
    private val keyboardLayout = arrayOf(
        arrayOf("A", "B", "C", "D", "E", "F"),
        arrayOf("G", "H", "I", "J", "K", "L"),
        arrayOf("M", "N", "O", "P", "Q", "R"),
        arrayOf("S", "T", "U", "V", "W", "X"),
        arrayOf("Y", "Z", "⎵", "⌫", "✕")  // Space, Backspace, Clear icons
    )
    
    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(8.dp, 8.dp, 8.dp, 8.dp)
        setBackgroundColor(ContextCompat.getColor(context, R.color.black))
        
        // Input display
        inputDisplay = TextView(context).apply {
            text = ""
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            gravity = Gravity.CENTER
            setPadding(8.dp, 8.dp, 8.dp, 12.dp)
            minWidth = 220.dp
            background = ContextCompat.getDrawable(context, R.drawable.search_input_background)
        }
        addView(inputDisplay)
        
        // Keyboard grid
        createKeyboard()
    }
    
    private fun createKeyboard() {
        keyboardLayout.forEach { row ->
            val rowLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(0, 3.dp, 0, 3.dp)
            }
            
            row.forEach { key ->
                val button = createKeyButton(key)
                rowLayout.addView(button)
            }
            
            addView(rowLayout)
        }
    }
    
    private fun createKeyButton(key: String): Button {
        return Button(context).apply {
            text = key
            textSize = when (key) {
                "⎵", "⌫", "✕" -> 22f  // Larger for icons
                else -> 13f
            }
            setTextColor(ContextCompat.getColor(context, R.color.white))
            
            val width = when (key) {
                "⎵" -> 52.dp  // Space
                "⌫", "✕" -> 62.dp  // Delete/Clear
                else -> 48.dp
            }
            
            layoutParams = LayoutParams(width, 44.dp).apply {
                setMargins(3.dp, 3.dp, 3.dp, 3.dp)
            }
            
            setBackgroundResource(R.drawable.search_input_background)
            isFocusable = true
            isFocusableInTouchMode = true
            
            setOnClickListener { handleKeyPress(key) }
            
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scaleX = 1.1f
                    scaleY = 1.1f
                } else {
                    scaleX = 1.0f
                    scaleY = 1.0f
                }
            }
        }
    }
    
    private fun handleKeyPress(key: String) {
        when (key) {
            "⎵", "SPACE" -> {  // Space icon or text
                currentInput.append(" ")
                updateDisplay()
            }
            "⌫", "DELETE" -> {  // Backspace icon or text
                if (currentInput.isNotEmpty()) {
                    currentInput.deleteCharAt(currentInput.length - 1)
                    updateDisplay()
                }
            }
            "✕", "CLEAR" -> {  // Clear icon or text
                currentInput.clear()
                updateDisplay()
            }
            "DONE" -> {
                // Notify listener and hide keyboard
                onTextChangeListener?.invoke(currentInput.toString())
            }
            else -> {
                currentInput.append(key)
                updateDisplay()
            }
        }
    }
    
    private fun updateDisplay() {
        inputDisplay.text = if (currentInput.isEmpty()) "Search..." else currentInput.toString()
        // Notify listener of text change in real-time
        onTextChangeListener?.invoke(currentInput.toString())
    }
    
    fun setOnTextChangeListener(listener: (String) -> Unit) {
        onTextChangeListener = listener
    }
    
    fun getText(): String = currentInput.toString()
    
    fun clear() {
        currentInput.clear()
        updateDisplay()
    }
    
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Handle D-pad navigation
        return super.dispatchKeyEvent(event)
    }
    
    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()
}
