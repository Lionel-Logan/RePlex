package com.replex.tv.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.replex.tv.databinding.FragmentLanguagesBinding

/**
 * Languages fragment
 * Displays content organized by original language
 */
class LanguagesFragment : Fragment() {
    
    private var _binding: FragmentLanguagesBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguagesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("RePlex", "LanguagesFragment created")
        
        // TODO: Detect languages from Plex metadata
        // TODO: Create sections for each language
        // TODO: Handle bilingual content (appears in both sections)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = LanguagesFragment()
    }
}
