package com.replex.tv.ui.browse.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.replex.tv.R
import com.replex.tv.models.PlexMetadata

/**
 * Adapter for individual content items within a row
 */
class ContentItemAdapter : RecyclerView.Adapter<ContentItemAdapter.ContentItemViewHolder>() {
    
    private val items = mutableListOf<PlexMetadata>()
    private var onItemClickListener: ((PlexMetadata) -> Unit)? = null
    
    class ContentItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImage: ImageView = itemView.findViewById(R.id.item_poster)
        val titleText: TextView = itemView.findViewById(R.id.item_title)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content_card, parent, false)
        return ContentItemViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ContentItemViewHolder, position: Int) {
        val item = items[position]
        
        holder.titleText.text = item.title
        
        // TODO: Load poster image with Glide/Coil
        // For now, show placeholder gradient
        val gradientColors = when (position % 5) {
            0 -> intArrayOf(0xFF1a1a2e.toInt(), 0xFF16213e.toInt())
            1 -> intArrayOf(0xFF0f3460.toInt(), 0xFF16213e.toInt())
            2 -> intArrayOf(0xFF533483.toInt(), 0xFF2b2d42.toInt())
            3 -> intArrayOf(0xFFe94560.toInt(), 0xFF16213e.toInt())
            else -> intArrayOf(0xFF2d4059.toInt(), 0xFF11182a.toInt())
        }
        
        holder.posterImage.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            gradientColors
        )
        
        holder.itemView.isFocusable = true
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
        
        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.animate()
                    .scaleX(1.15f)
                    .scaleY(1.15f)
                    .translationZ(8f)
                    .setDuration(150)
                    .start()
            } else {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .translationZ(0f)
                    .setDuration(150)
                    .start()
            }
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    fun submitList(newItems: List<PlexMetadata>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    
    fun setOnItemClickListener(listener: (PlexMetadata) -> Unit) {
        onItemClickListener = listener
    }
}
