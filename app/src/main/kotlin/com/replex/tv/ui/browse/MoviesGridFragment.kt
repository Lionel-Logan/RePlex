package com.replex.tv.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.replex.tv.databinding.FragmentMoviesGridBinding

/**
 * Movies grid fragment
 * Displays all movies in a grid layout
 */
class MoviesGridFragment : Fragment() {
    
    private var _binding: FragmentMoviesGridBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviesGridBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("RePlex", "MoviesGridFragment created")
        
        // TODO: Setup grid layout
        // TODO: Load movies from repository
        // TODO: Implement pagination
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = MoviesGridFragment()
    }
}
