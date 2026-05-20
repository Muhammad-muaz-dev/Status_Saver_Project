package com.example.statussaver.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.statussaver.ui.adapters.MediaViewPagerAdapter
import com.example.statussaver.data.StatusRepo
import com.example.statussaver.databinding.FragmentStatusBinding
import com.example.statussaver.utils.Constants
import com.example.statussaver.utils.SharedPrefKeys
import com.example.statussaver.utils.SharedPrefUtils
import com.example.statussaver.viewmodels.StatusViewModel
import com.example.statussaver.viewmodels.factories.StatusViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class FragmentStatus : Fragment() {

    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var type: String
    private val viewPagerTitles = listOf("Images", "Videos")
    private lateinit var viewModel: StatusViewModel
    private var currentAppType: String = ""

    companion object {
        private const val TAG = "FragmentStatus_DEBUG"
    }

    // ============ MODERN ACTIVITY RESULT API ============
    private val directoryPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        Log.d(TAG, "→ directoryPickerLauncher callback received. Uri: $uri")

        if (uri != null) {
            handleDirectorySelection(uri)
        } else {
            Log.w(TAG, "→ URI is null - User likely cancelled or picker failed")
            Toast.makeText(requireContext(), "No directory selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(Constants.FRAGMENT_TYPE_KEY, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatusBinding.inflate(inflater, container, false)

        val repo = StatusRepo(requireActivity())
        viewModel = ViewModelProvider(
            requireActivity(),
            StatusViewModelFactory(repo)
        )[StatusViewModel::class.java]

        setupUI()
        return binding.root
    }

    private fun setupUI() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            when (type) {
                Constants.TYPE_WHATSAPP_MAIN -> refreshWhatsAppStatuses()
                Constants.TYPE_WHATSAPP_BUSINESS -> refreshWhatsAppBusinessStatuses()
                else -> Log.e(TAG, "Unknown fragment type: $type")
            }
        }

        when (type) {
            Constants.TYPE_WHATSAPP_MAIN -> setupWhatsAppUI()
            Constants.TYPE_WHATSAPP_BUSINESS -> setupWhatsAppBusinessUI()
            else -> Log.e(TAG, "Unknown fragment type: $type")
        }
    }

    private fun setupWhatsAppUI() {
        val isPermissionGranted = SharedPrefUtils.getPrefBoolean(
            SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED, false
        )

        if (isPermissionGranted) {
            getWhatsAppStatuses()
        } else {
            showPermissionPrompt(Constants.TYPE_WHATSAPP_MAIN)
        }
        setupViewPager()
    }

    private fun setupWhatsAppBusinessUI() {
        val isPermissionGranted = SharedPrefUtils.getPrefBoolean(
            SharedPrefKeys.PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED, false
        )

        if (isPermissionGranted) {
            getWhatsAppBusinessStatuses()
        } else {
            showPermissionPrompt(Constants.TYPE_WHATSAPP_BUSINESS)
        }
        setupViewPager(isBusiness = true)
    }

    private fun showPermissionPrompt(appType: String) {
        binding.permissionLayoutHolder.visibility = View.VISIBLE
        binding.permissionLayout.btnPermission.setOnClickListener {
            openDirectoryPicker(appType)
        }
    }

    private fun openDirectoryPicker(appType: String) {
        currentAppType = appType

        val initialUri = when (appType) {
            Constants.TYPE_WHATSAPP_MAIN -> Constants.getWhatsappUri()
            Constants.TYPE_WHATSAPP_BUSINESS -> Constants.getWhatsappBusinessUri()
            else -> null
        }

        Log.d(TAG, "Launching directory picker with hint: $initialUri")
        
        Toast.makeText(
            requireContext(),
            "Please click 'USE THIS FOLDER' to allow access to WhatsApp statuses",
            Toast.LENGTH_LONG
        ).show()

        try {
            // Using the /document/ URI directly as a hint often works best 
            // for ACTION_OPEN_DOCUMENT_TREE on Android 11+
            directoryPickerLauncher.launch(initialUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching directory picker: ${e.message}")
            try {
                directoryPickerLauncher.launch(null)
            } catch (e2: Exception) {
                Toast.makeText(requireContext(), "Error: ${e2.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleDirectorySelection(uri: Uri) {
        try {
            val resolver = requireActivity().contentResolver
            
            // Take persistable URI permission
            resolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            when (currentAppType) {
                Constants.TYPE_WHATSAPP_MAIN -> {
                    SharedPrefUtils.putPrefString(SharedPrefKeys.PREF_KEY_WP_TREE_URI, uri.toString())
                    SharedPrefUtils.putPrefBoolean(SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED, true)
                    
                    Toast.makeText(requireContext(), "WhatsApp directory selected", Toast.LENGTH_SHORT).show()
                    binding.permissionLayoutHolder.visibility = View.GONE
                    getWhatsAppStatuses()
                }

                Constants.TYPE_WHATSAPP_BUSINESS -> {
                    SharedPrefUtils.putPrefString(SharedPrefKeys.PREF_KEY_WP_BUSINESS_TREE_URI, uri.toString())
                    SharedPrefUtils.putPrefBoolean(SharedPrefKeys.PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED, true)

                    Toast.makeText(requireContext(), "WhatsApp Business directory selected", Toast.LENGTH_SHORT).show()
                    binding.permissionLayoutHolder.visibility = View.GONE
                    getWhatsAppBusinessStatuses()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling directory selection: ${e.message}")
            Toast.makeText(requireContext(), "Failed to save directory: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupViewPager(isBusiness: Boolean = false) {
        val adapter = if (isBusiness) {
            MediaViewPagerAdapter(
                requireActivity(),
                Constants.MEDIA_TYPE_WHATSAPP_BUSINESS_IMAGES,
                Constants.MEDIA_TYPE_WHATSAPP_BUSINESS_VIDEOS
            )
        } else {
            MediaViewPagerAdapter(requireActivity())
        }

        binding.statusViewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.statusViewPager) { tab, pos ->
            tab.text = viewPagerTitles.getOrNull(pos) ?: "Unknown"
        }.attach()
    }

    private fun getWhatsAppStatuses() {
        binding.permissionLayoutHolder.visibility = View.GONE
        viewModel.getWhatsAppStatuses()
    }

    private fun getWhatsAppBusinessStatuses() {
        binding.permissionLayoutHolder.visibility = View.GONE
        viewModel.getWhatsAppBusinessStatuses()
    }

    private fun refreshWhatsAppStatuses() {
        binding.swipeRefreshLayout.isRefreshing = true
        getWhatsAppStatuses()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun refreshWhatsAppBusinessStatuses() {
        binding.swipeRefreshLayout.isRefreshing = true
        getWhatsAppBusinessStatuses()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
