package com.replex.tv.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.replex.tv.databinding.FragmentTvshowsGridBinding

/**
 * TV Shows grid fragment
 * Displays all TV shows in a grid layout
 */
class TVShowsGridFragment : Fragment() {
    
    private var _binding: FragmentTvshowsGridBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTvshowsGridBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("RePlex", "TVShowsGridFragment created")
        
        // TODO: Setup grid layout
        // TODO: Load TV shows from repository
        // TODO: Implement pagination
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = TVShowsGridFragment()
    }
}
