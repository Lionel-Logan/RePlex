package com.replex.tv.ui.browse.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.replex.tv.R
import com.replex.tv.ui.browse.HomeViewModel
import com.replex.tv.ui.browse.components.HeroBannerView

/**
 * Adapter for content rows with hero banner header
 * View type 0: Hero Banner
 * View type 1: Content Rows
 */
class ContentRowAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val VIEW_TYPE_HERO = 0
        private const val VIEW_TYPE_ROW = 1
    }
    
    private val rows = mutableListOf<HomeViewModel.ContentRow>()
    private var showHeroBanner = false
    private var heroBannerView: HeroBannerView? = null
    
    class HeroViewHolder(val heroBannerView: HeroBannerView) : RecyclerView.ViewHolder(heroBannerView)
    
    class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.row_title)
        val contentRecycler: RecyclerView = itemView.findViewById(R.id.row_content_recycler)
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && showHeroBanner) VIEW_TYPE_HERO else VIEW_TYPE_ROW
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HERO -> {
                val density = parent.context.resources.displayMetrics.density
                val heroView = HeroBannerView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (400 * density).toInt()
                    )
                }
                heroBannerView = heroView
                HeroViewHolder(heroView)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_content_row, parent, false)
                RowViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {            is HeroViewHolder -> {
                // Hero banner is managed externally via getHeroBannerView()
            }
            is RowViewHolder -> {
                // Offset position by 1 if hero banner exists
                val rowPosition = if (showHeroBanner) position - 1 else position
                val row = rows[rowPosition]
                
                holder.titleText.text = row.title
                
                // Setup horizontal RecyclerView for content items
                holder.contentRecycler.layoutManager = LinearLayoutManager(
                    holder.itemView.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                
                val contentAdapter = ContentItemAdapter()
                contentAdapter.submitList(row.items)
                holder.contentRecycler.adapter = contentAdapter
                
                // Make the row NOT focusable - only items inside are focusable
                holder.itemView.isFocusable = false
                holder.itemView.focusable = View.NOT_FOCUSABLE
                
                // Implement infinite scrolling for content rows
                holder.contentRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                        val adapter = recyclerView.adapter as? ContentItemAdapter
                        
                        layoutManager?.let { lm ->
                            adapter?.let { adp ->
                                val lastVisiblePosition = lm.findLastVisibleItemPosition()
                                val totalItems = adp.itemCount
                                
                                // Load more when near the end (3 items before last)
                                if (lastVisiblePosition >= totalItems - 3) {
                                    row.loadMoreCallback?.invoke()
                                }
                            }
                        }
                    }
                })
            }
        }
    }
    
    override fun getItemCount(): Int {
        return (if (showHeroBanner) 1 else 0) + rows.size
    }
    
    fun submitList(newRows: List<HomeViewModel.ContentRow>) {
        rows.clear()
        rows.addAll(newRows)
        notifyDataSetChanged()
    }
    
    fun setShowHeroBanner(show: Boolean) {
        showHeroBanner = show
        notifyDataSetChanged()
    }
    
    fun getHeroBannerView(): HeroBannerView? = heroBannerView
}
