package com.replex.tv.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.replex.tv.R
import com.replex.tv.databinding.FragmentAuthBinding
import timber.log.Timber

/**
 * Authentication screen that displays PIN code and handles OAuth flow
 */
class AuthFragment : Fragment() {
    
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()
    
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
        
        Timber.d("REPLEX: AuthFragment onViewCreated called")
        setupObservers()
        setupListeners()
        
        // Start auth flow
        Timber.d("REPLEX: Starting auth flow")
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
        Timber.d("Auth success, navigating to home")
        // Navigate to home screen
        requireActivity().finish()
        requireActivity().startActivity(requireActivity().intent)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = AuthFragment()
    }
}
