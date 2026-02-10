package com.replex.tv.ui.browse.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.replex.tv.R
import com.replex.tv.ui.browse.components.HeroBannerView

/**
 * Adapter for HeroBannerView ViewPager2
 * Displays hero banner images with metadata
 */
class HeroBannerAdapter : RecyclerView.Adapter<HeroBannerAdapter.HeroBannerViewHolder>(), 
    HeroBannerView.HeroBannerAdapterInterface {
    
    private val heroItems = mutableListOf<HeroBannerItem>()
    private var onItemClickListener: ((HeroBannerItem) -> Unit)? = null
    private var metadataUpdateCallback: ((String, String) -> Unit)? = null
    
    data class HeroBannerItem(
        val title: String,
        val subtitle: String,
        val backdropUrl: String?,
        val itemId: String
    )
    
    class HeroBannerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            isFocusable = true
            isFocusableInTouchMode = true
            
            // Placeholder background
            background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    ) {
        val imageView: ImageView = itemView as ImageView
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroBannerViewHolder {
        return HeroBannerViewHolder(parent)
    }
    
    override fun onBindViewHolder(holder: HeroBannerViewHolder, position: Int) {
        val item = heroItems[position]
        
        // TODO: Load backdrop image with Glide/Coil
        // For now, show placeholder with gradient
        val gradientColors = when (position % 3) {
            0 -> intArrayOf(0xFF1a1a2e.toInt(), 0xFF16213e.toInt())
            1 -> intArrayOf(0xFF0f3460.toInt(), 0xFF16213e.toInt())
            else -> intArrayOf(0xFF533483.toInt(), 0xFF2b2d42.toInt())
        }
        
        holder.imageView.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            gradientColors
        )
        
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
        
        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(200)
                    .start()
            } else {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
        }
    }
    
    override fun getItemCount(): Int = heroItems.size
    
    fun submitList(items: List<HeroBannerItem>) {
        heroItems.clear()
        heroItems.addAll(items)
        notifyDataSetChanged()
    }
    
    fun setOnItemClickListener(listener: (HeroBannerItem) -> Unit) {
        onItemClickListener = listener
    }
    
    fun setMetadataUpdateCallback(callback: (String, String) -> Unit) {
        metadataUpdateCallback = callback
    }
    
    override fun onPageSelected(position: Int) {
        if (position in heroItems.indices) {
            val item = heroItems[position]
            metadataUpdateCallback?.invoke(item.title, item.subtitle)
        }
    }
}
