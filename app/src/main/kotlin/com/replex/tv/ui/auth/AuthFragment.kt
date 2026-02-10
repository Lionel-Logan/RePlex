package com.replex.tv.ui.auth

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.replex.tv.R
import com.replex.tv.databinding.FragmentAuthBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Authentication screen that displays PIN code and handles OAuth flow
 */
class AuthFragment : Fragment() {
    
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()
    private var taglineRotationJob: Job? = null
    
    private val taglines = listOf(
        "Host Plex, Stream RePlex",
        "Unmatched Lossless Quality",
        "Your Content, Your Control",
        "Enterprise Grade Streaming",
        "Direct Play Everywhere",
        "Zero Buffering, Pure Quality"
    )
    private var currentTaglineIndex = 0
    
    private val audioLogos = listOf(
        R.drawable.dolby_atmos,
        R.drawable.dolby_truehd,
        R.drawable.dts_x,
        R.drawable.dts_hd_ma,
        R.drawable.dolby_digital_plus,
        R.drawable.dts_hd,
        R.drawable.dolby_digital,
        R.drawable.dts,
        R.drawable.dolby_pro_logic_ii,
        R.drawable.aac
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("RePlex", "REPLEX: AuthFragment onViewCreated called")
        
        // Make plex.tv/link bold in instructions
        val instructions = binding.authInstructions.text.toString()
        val spannable = SpannableString(instructions)
        val linkStart = instructions.indexOf("plex.tv/link")
        if (linkStart != -1) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                linkStart,
                linkStart + "plex.tv/link".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.authInstructions.text = spannable
        }
        
        // Make plex.tv/link bold in helper text too
        val helperText = binding.helperText.text.toString()
        val helperSpannable = SpannableString(helperText)
        val helperLinkStart = helperText.indexOf("plex.tv/link")
        if (helperLinkStart != -1) {
            helperSpannable.setSpan(
                StyleSpan(Typeface.BOLD),
                helperLinkStart,
                helperLinkStart + "plex.tv/link".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.helperText.text = helperSpannable
        }
        
        setupObservers()
        setupListeners()
        startTaglineRotation()
        
        // Start auth flow
        Log.i("RePlex", "REPLEX: Starting auth flow")
        viewModel.startAuthFlow()
    }
    
    private fun setupObservers() {
        viewModel.pinCode.observe(viewLifecycleOwner) { code ->
            binding.pinCode.text = code
        }
        
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    showLoading()
                }
                is AuthViewModel.AuthState.WaitingForUser -> {
                    showWaiting()
                }
                is AuthViewModel.AuthState.Authenticating -> {
                    showAuthenticating()
                }
                is AuthViewModel.AuthState.Success -> {
                    onAuthSuccess()
                }
                is AuthViewModel.AuthState.Error -> {
                    showError(state.message)
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }
    
    private fun showLoading() {
        binding.pinCode.text = "----"
        binding.errorContainer.visibility = View.GONE
    }
    
    private fun showWaiting() {
        binding.errorContainer.visibility = View.GONE
    }
    
    private fun showAuthenticating() {
        binding.errorContainer.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        binding.errorContainer.visibility = View.VISIBLE
        binding.errorMessage.text = message
        binding.retryButton.requestFocus()
    }
    
    private fun onAuthSuccess() {
        Log.i("RePlex", "Auth success, navigating to home")
        // Navigate to home screen
        requireActivity().finish()
        requireActivity().startActivity(requireActivity().intent)
    }
    
    private fun startTaglineRotation() {
        taglineRotationJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                // Wait 4 seconds before changing
                delay(4000)
                
                if (!isActive || _binding == null) break
                
                // Fade out
                val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
                fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        if (_binding == null) return
                        
                        // Update text
                        currentTaglineIndex = (currentTaglineIndex + 1) % taglines.size
                        binding.rotatingTagline.text = taglines[currentTaglineIndex]
                        
                        // Fade in
                        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                        binding.rotatingTagline.startAnimation(fadeIn)
                    }
                })
                binding.rotatingTagline.startAnimation(fadeOut)
            }
        }
    }
    
    
    override fun onDestroyView() {
        super.onDestroyView()
        taglineRotationJob?.cancel()
        _binding = null
    }
    
    companion object {
        fun newInstance() = AuthFragment()
    }
}
