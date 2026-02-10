package com.replex.tv.ui.browse

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.replex.tv.R
import com.replex.tv.databinding.FragmentHomeBinding

/**
 * Main home screen container
 * Manages navigation tabs and content fragments
 */
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    
    private var currentTab: NavigationTab = NavigationTab.HOME
    private var isExploreExpanded = false
    private var lastExpandedTab: NavigationTab = NavigationTab.MOVIES
    private var lastClickedIndex = -1
    
    enum class NavigationTab {
        HOME, EXPLORE, MOVIES, TV_SHOWS, LANGUAGES, SEARCH
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("RePlex", "HomeFragment created")
        
        setupNavigationBar()
        setupObservers()
        setupBackButtonHandler()
        
        // Show home content by default
        showHomeContent()
    }
    
    private fun setupBackButtonHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPress()
                }
            }
        )
    }
    
    private fun handleBackPress() {
        when (currentTab) {
            NavigationTab.HOME -> {
                // Show exit dialog
                showExitDialog()
            }
            else -> {
                // Navigate back to home - collapse explore if expanded
                if (isExploreExpanded) {
                    isExploreExpanded = false
                    binding.navigationBar.collapseExplore()
                }
                currentTab = NavigationTab.HOME
                showHomeContent()
                // Reset tab selection to Home (index 0)
                post { binding.navigationBar.requestFocusForTab(0) }
            }
        }
    }
    
    private fun post(action: () -> Unit) {
        binding.root.post(action)
    }
    
    private fun showExitDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Exit RePlex")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { _, _ ->
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupNavigationBar() {
        binding.navigationBar.setTabs(
            listOf(
                getString(R.string.nav_home),
                getString(R.string.nav_explore),
                getString(R.string.nav_search)
            )
        )
        
        binding.navigationBar.setOnTabSelectedListener { position ->
            handleTabSelection(position)
        }
        
        binding.navigationBar.setOnTabExpandListener {
            expandExploreTab()
        }
    }
    
    private fun setupObservers() {
        // TODO: Observe ViewModel data
    }
    
    private fun handleTabSelection(position: Int) {
        when {
            !isExploreExpanded -> {
                // Normal state: Home | Explore | Search
                when (position) {
                    0 -> {
                        if (currentTab == NavigationTab.HOME) return // Already on home
                        currentTab = NavigationTab.HOME
                        showHomeContent()
                    }
                    1 -> {
                        // Explore selected - expand it
                        expandExploreTab()
                    }
                    2 -> {
                        if (currentTab == NavigationTab.SEARCH) return // Already on search
                        currentTab = NavigationTab.SEARCH
                        showSearchContent()
                    }
                }
            }
            else -> {
                // Expanded state: Home | Movies | TV Shows | Languages | Search
                when (position) {
                    0 -> {
                        if (currentTab == NavigationTab.HOME) return // Already on home
                        currentTab = NavigationTab.HOME
                        collapseExploreTab()
                        showHomeContent()
                    }
                    1 -> {
                        if (currentTab == NavigationTab.MOVIES) return // Already on movies
                        currentTab = NavigationTab.MOVIES
                        lastExpandedTab = NavigationTab.MOVIES
                        showMoviesContent()
                    }
                    2 -> {
                        if (currentTab == NavigationTab.TV_SHOWS) return // Already on TV shows
                        currentTab = NavigationTab.TV_SHOWS
                        lastExpandedTab = NavigationTab.TV_SHOWS
                        showTVShowsContent()
                    }
                    3 -> {
                        if (currentTab == NavigationTab.LANGUAGES) return // Already on languages
                        currentTab = NavigationTab.LANGUAGES
                        lastExpandedTab = NavigationTab.LANGUAGES
                        showLanguagesContent()
                    }
                    4 -> {
                        if (currentTab == NavigationTab.SEARCH) return // Already on search
                        currentTab = NavigationTab.SEARCH
                        collapseExploreTab()
                        showSearchContent()
                    }
                }
            }
        }
    }
    
    private fun expandExploreTab() {
        if (isExploreExpanded) return
        
        isExploreExpanded = true
        binding.navigationBar.expandExplore(lastExpandedTab.ordinal - 2) // Convert to expanded index
        
        // Show the last selected expanded tab content AND set currentTab
        when (lastExpandedTab) {
            NavigationTab.MOVIES -> {
                currentTab = NavigationTab.MOVIES
                showMoviesContent()
            }
            NavigationTab.TV_SHOWS -> {
                currentTab = NavigationTab.TV_SHOWS
                showTVShowsContent()
            }
            NavigationTab.LANGUAGES -> {
                currentTab = NavigationTab.LANGUAGES
                showLanguagesContent()
            }
            else -> {
                currentTab = NavigationTab.MOVIES // Default to movies
                showMoviesContent()
            }
        }
    }
    
    private fun collapseExploreTab() {
        if (!isExploreExpanded) return
        
        isExploreExpanded = false
        binding.navigationBar.collapseExplore()
    }
    
    private fun showHomeContent() {
        Log.i("RePlex", "Showing home content")
        childFragmentManager.beginTransaction()
            .replace(R.id.content_container, HomeContentFragment.newInstance())
            .commit()
    }
    
    private fun showMoviesContent() {
        Log.i("RePlex", "Showing movies content")
        childFragmentManager.beginTransaction()
            .replace(R.id.content_container, MoviesGridFragment.newInstance())
            .commit()
    }
    
    private fun showTVShowsContent() {
        Log.i("RePlex", "Showing TV shows content")
        childFragmentManager.beginTransaction()
            .replace(R.id.content_container, TVShowsGridFragment.newInstance())
            .commit()
    }
    
    private fun showLanguagesContent() {
        Log.i("RePlex", "Showing languages content")
        childFragmentManager.beginTransaction()
            .replace(R.id.content_container, LanguagesFragment.newInstance())
            .commit()
    }
    
    private fun showSearchContent() {
        Log.i("RePlex", "Showing search content")
        childFragmentManager.beginTransaction()
            .replace(R.id.content_container, SearchFragment.newInstance())
            .commit()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HomeFragment()
    }
}
