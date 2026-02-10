package com.replex.tv.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.replex.tv.databinding.FragmentHomeContentBinding
import com.replex.tv.ui.browse.adapters.HeroBannerAdapter
import com.replex.tv.ui.browse.adapters.ContentRowAdapter

/**
 * Home content fragment containing hero banner and content rows
 */
class HomeContentFragment : Fragment() {
    
    private var _binding: FragmentHomeContentBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels({ requireParentFragment() })
    private lateinit var heroBannerAdapter: HeroBannerAdapter
    private lateinit var contentRowAdapter: ContentRowAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeContentBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("RePlex", "HomeContentFragment created")
        
        setupHeroBanner()
        setupContentRows()
        setupObservers()
    }
    
    private fun setupHeroBanner() {
        Log.i("RePlex", "Setting up hero banner")
        heroBannerAdapter = HeroBannerAdapter()
    }
    
    private fun setupContentRows() {
        Log.i("RePlex", "Setting up content rows")
        
        contentRowAdapter = ContentRowAdapter()
        binding.contentRowsRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = contentRowAdapter
            isFocusable = false
        }
        
        // Enable hero banner as first item
        contentRowAdapter.setShowHeroBanner(true)
        
        // Setup hero banner after view is created
        view?.post {
            contentRowAdapter.getHeroBannerView()?.let { heroView ->
                heroBannerAdapter.setMetadataUpdateCallback { title, subtitle ->
                    heroView.updateMetadata(title, subtitle)
                }
                heroView.setHeroAdapter(heroBannerAdapter)
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.heroItems.observe(viewLifecycleOwner) { items ->
            Log.i("RePlex", "Received ${items.size} hero items")
            if (items.isNotEmpty()) {
                // Convert PlexMetadata to HeroBannerAdapter items
                val heroBannerItems = items.map {
                    HeroBannerAdapter.HeroBannerItem(
                        title = it.title ?: "Unknown",
                        subtitle = it.summary ?: "",
                        backdropUrl = it.thumb,
                        itemId = it.ratingKey ?: ""
                    )
                }
                heroBannerAdapter.submitList(heroBannerItems)
                
                // Update hero banner view after it's created
                view?.post {
                    contentRowAdapter.getHeroBannerView()?.let { heroView ->
                        heroBannerAdapter.setMetadataUpdateCallback { title, subtitle ->
                            heroView.updateMetadata(title, subtitle)
                        }
                        heroView.setHeroAdapter(heroBannerAdapter)
                    }
                }
            }
        }
        
        viewModel.contentRows.observe(viewLifecycleOwner) { rows ->
            Log.i("RePlex", "Received ${rows.size} content rows")
            if (rows.isNotEmpty()) {
                contentRowAdapter.submitList(rows)
                Log.i("RePlex", "Content rows adapter updated with ${rows.size} rows")
            }
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { _ ->
            // TODO: Show/hide loading state
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("RePlex", "Error loading content: $it")
                // TODO: Show error state
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HomeContentFragment()
    }
}
