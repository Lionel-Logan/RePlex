package com.replex.tv.ui.browse

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.replex.tv.databinding.FragmentSearchBinding

/**
 * Search fragment with custom on-screen keyboard
 * Implements real-time search as user types
 */
class SearchFragment : Fragment() {
    
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("RePlex", "SearchFragment created with on-screen keyboard")
        
        // Setup on-screen keyboard with real-time search
        binding.onScreenKeyboard.setOnTextChangeListener { query ->
            // Debounce search - wait 300ms after user stops typing
            searchRunnable?.let { searchHandler.removeCallbacks(it) }
            
            if (query.isEmpty()) {
                // Clear results
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Start typing to search..."
                Log.i("RePlex", "Search cleared")
                // TODO: Clear search results RecyclerView
            } else {
                binding.emptyStateText.visibility = View.GONE
                searchRunnable = Runnable {
                    performSearch(query)
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
        }
        
        // Focus keyboard on load
        binding.onScreenKeyboard.post {
            binding.onScreenKeyboard.requestFocus()
        }
    }
    
    private fun performSearch(query: String) {
        Log.i("RePlex", "Performing search: $query")
        // TODO: Call PlexRepository.search(query)
        // TODO: Update searchResultsRecycler with results
        binding.emptyStateText.text = "Searching for '$query'..."
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = SearchFragment()
    }
}
