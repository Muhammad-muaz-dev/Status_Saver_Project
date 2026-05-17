package com.example.statussaver.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
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
        Log.d(TAG, "❌ directoryPickerLauncher callback received")
        Log.d(TAG, "   Uri: $uri")
        Log.d(TAG, "   Uri is null: ${uri == null}")

        if (uri != null) {
            Log.d(TAG, "✅ Valid URI received, calling handleDirectorySelection()")
            handleDirectorySelection(uri)
        } else {
            Log.w(TAG, "❌ URI is null - User likely cancelled the picker")
            Toast.makeText(requireContext(), "No directory selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "→ onCreate() called")

        arguments?.let {
            type = it.getString(Constants.FRAGMENT_TYPE_KEY, "")
            Log.d(TAG, "  Fragment type: $type")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "→ onCreateView() called")
        _binding = FragmentStatusBinding.inflate(inflater, container, false)

        val repo = StatusRepo(requireActivity())
        viewModel = ViewModelProvider(
            requireActivity(),
            StatusViewModelFactory(repo)
        )[StatusViewModel::class.java]

        setupUI()
        Log.d(TAG, "✅ onCreateView() completed")
        return binding.root
    }

    private fun setupUI() {
        Log.d(TAG, "→ setupUI() called, type: $type")

        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "🔄 Swipe refresh triggered")
            when (type) {
                Constants.TYPE_WHATSAPP_MAIN -> refreshWhatsAppStatuses()
                Constants.TYPE_WHATSAPP_BUSINESS -> refreshWhatsAppBusinessStatuses()
                else -> Log.e(TAG, "❌ Unknown fragment type: $type")
            }
        }

        when (type) {
            Constants.TYPE_WHATSAPP_MAIN -> {
                Log.d(TAG, "→ Setting up WhatsApp Main UI")
                setupWhatsAppUI()
            }
            Constants.TYPE_WHATSAPP_BUSINESS -> {
                Log.d(TAG, "→ Setting up WhatsApp Business UI")
                setupWhatsAppBusinessUI()
            }
            else -> Log.e(TAG, "❌ Unknown fragment type: $type")
        }
    }

    private fun setupWhatsAppUI() {
        Log.d(TAG, "→ setupWhatsAppUI() called")

        val isPermissionGranted = SharedPrefUtils.getPrefBoolean(
            SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED, false
        )
        Log.d(TAG, "  WhatsApp permission granted: $isPermissionGranted")

        if (isPermissionGranted) {
            Log.d(TAG, "  ✅ Permission already granted, fetching statuses")
            getWhatsAppStatuses()
        } else {
            Log.d(TAG, "  ❌ Permission NOT granted, showing permission prompt")
            showPermissionPrompt(Constants.TYPE_WHATSAPP_MAIN)
        }
        setupViewPager()
    }

    private fun setupWhatsAppBusinessUI() {
        Log.d(TAG, "→ setupWhatsAppBusinessUI() called")

        val isPermissionGranted = SharedPrefUtils.getPrefBoolean(
            SharedPrefKeys.PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED, false
        )
        Log.d(TAG, "  WhatsApp Business permission granted: $isPermissionGranted")

        if (isPermissionGranted) {
            Log.d(TAG, "  ✅ Permission already granted, fetching statuses")
            getWhatsAppBusinessStatuses()
        } else {
            Log.d(TAG, "  ❌ Permission NOT granted, showing permission prompt")
            showPermissionPrompt(Constants.TYPE_WHATSAPP_BUSINESS)
        }
        setupViewPager(isBusiness = true)
    }

    private fun showPermissionPrompt(appType: String) {
        Log.d(TAG, "→ showPermissionPrompt() called for: $appType")

        try {
            binding.permissionLayoutHolder.visibility = View.VISIBLE
            Log.d(TAG, "  ✅ Permission layout made visible")

            binding.permissionLayout.btnPermission.setOnClickListener {
                Log.d(TAG, "  👆 Permission button clicked")
                Log.d(TAG, "     App type: $appType")
                openDirectoryPicker(appType)
            }
            Log.d(TAG, "  ✅ Permission button click listener set")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in showPermissionPrompt: ${e.message}", e)
        }
    }

    private fun openDirectoryPicker(appType: String) {
        Log.d(TAG, "→ openDirectoryPicker() called for: $appType")

        currentAppType = appType

        val initialUri = when (appType) {
            Constants.TYPE_WHATSAPP_MAIN -> Constants.getWhatsappUri()
            Constants.TYPE_WHATSAPP_BUSINESS -> Constants.getWhatsappBusinessUri()
            else -> null
        }

        // For ACTION_OPEN_DOCUMENT_TREE, the initial URI hint works best when 
        // it follows the format: content://.../tree/[id]/document/[id]
        val pickerInitialUri = initialUri?.let { uri ->
            val uriString = uri.toString()
            if (uriString.contains("/tree/") && !uriString.contains("/document/")) {
                val id = uriString.substringAfter("/tree/")
                Uri.parse("$uriString/document/$id")
            } else {
                uri
            }
        }

        Log.d(TAG, "  Launching directory picker with hint: $pickerInitialUri")

        try {
            directoryPickerLauncher.launch(pickerInitialUri)
            Log.d(TAG, "  ✅ directoryPickerLauncher.launch() called")
        } catch (e: Exception) {
            Log.e(TAG, "  ❌ Error launching directory picker: ${e.message}")
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDirectorySelection(uri: Uri) {
        Log.d(TAG, "→ handleDirectorySelection() called")
        Log.d(TAG, "  Uri: $uri")
        Log.d(TAG, "  currentAppType: $currentAppType")

        try {
            val resolver = requireActivity().contentResolver
            Log.d(TAG, "  ✅ Got content resolver")

            // Log the URI structure
            Log.d(TAG, "  URI Details:")
            Log.d(TAG, "    - Scheme: ${uri.scheme}")
            Log.d(TAG, "    - Authority: ${uri.authority}")
            Log.d(TAG, "    - Path: ${uri.path}")

            // Take persistent read permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "  Taking persistable URI permission...")
                resolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Log.d(TAG, "  ✅ Persistable URI permission granted")
            }

            when (currentAppType) {
                Constants.TYPE_WHATSAPP_MAIN -> {
                    Log.d(TAG, "  Handling WhatsApp Main selection")
                    SharedPrefUtils.putPrefString(
                        SharedPrefKeys.PREF_KEY_WP_TREE_URI,
                        uri.toString()
                    )
                    Log.d(TAG, "    ✅ Saved URI to PREF_KEY_WP_TREE_URI")

                    SharedPrefUtils.putPrefBoolean(
                        SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED,
                        true
                    )
                    Log.d(TAG, "    ✅ Set PREF_KEY_WP_PERMISSION_GRANTED to true")

                    Toast.makeText(
                        requireContext(),
                        "WhatsApp directory selected",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.permissionLayoutHolder.visibility = View.GONE
                    Log.d(TAG, "    ✅ Hidden permission layout")

                    getWhatsAppStatuses()
                }

                Constants.TYPE_WHATSAPP_BUSINESS -> {
                    Log.d(TAG, "  Handling WhatsApp Business selection")
                    SharedPrefUtils.putPrefString(
                        SharedPrefKeys.PREF_KEY_WP_BUSINESS_TREE_URI,
                        uri.toString()
                    )
                    Log.d(TAG, "    ✅ Saved URI to PREF_KEY_WP_BUSINESS_TREE_URI")

                    SharedPrefUtils.putPrefBoolean(
                        SharedPrefKeys.PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED,
                        true
                    )
                    Log.d(TAG, "    ✅ Set PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED to true")

                    Toast.makeText(
                        requireContext(),
                        "WhatsApp Business directory selected",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.permissionLayoutHolder.visibility = View.GONE
                    Log.d(TAG, "    ✅ Hidden permission layout")

                    getWhatsAppBusinessStatuses()
                }

                else -> {
                    Log.e(TAG, "  ❌ Unknown app type in selection: $currentAppType")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling directory selection: ${e.message}", e)
            Log.e(TAG, "   Stack trace:", e)
            Toast.makeText(
                requireContext(),
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupViewPager(isBusiness: Boolean = false) {
        Log.d(TAG, "→ setupViewPager() called, isBusiness: $isBusiness")

        try {
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
            Log.d(TAG, "  ✅ ViewPager adapter set")

            TabLayoutMediator(binding.tabLayout, binding.statusViewPager) { tab, pos ->
                tab.text = viewPagerTitles.getOrNull(pos) ?: "Unknown"
            }.attach()
            Log.d(TAG, "  ✅ TabLayoutMediator attached")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up ViewPager: ${e.message}", e)
        }
    }

    private fun getWhatsAppStatuses() {
        Log.d(TAG, "→ getWhatsAppStatuses() called")
        try {
            binding.permissionLayoutHolder.visibility = View.GONE
            Log.d(TAG, "  ✅ Hidden permission layout")
            viewModel.getWhatsAppStatuses()
            Log.d(TAG, "  ✅ Called viewModel.getWhatsAppStatuses()")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in getWhatsAppStatuses: ${e.message}", e)
        }
    }

    private fun getWhatsAppBusinessStatuses() {
        Log.d(TAG, "→ getWhatsAppBusinessStatuses() called")
        try {
            binding.permissionLayoutHolder.visibility = View.GONE
            Log.d(TAG, "  ✅ Hidden permission layout")
            viewModel.getWhatsAppBusinessStatuses()
            Log.d(TAG, "  ✅ Called viewModel.getWhatsAppBusinessStatuses()")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in getWhatsAppBusinessStatuses: ${e.message}", e)
        }
    }

    private fun refreshWhatsAppStatuses() {
        Log.d(TAG, "→ refreshWhatsAppStatuses() called")
        binding.swipeRefreshLayout.isRefreshing = true
        getWhatsAppStatuses()
        binding.swipeRefreshLayout.isRefreshing = false
        Log.d(TAG, "  ✅ WhatsApp refresh completed")
    }

    private fun refreshWhatsAppBusinessStatuses() {
        Log.d(TAG, "→ refreshWhatsAppBusinessStatuses() called")
        binding.swipeRefreshLayout.isRefreshing = true
        getWhatsAppBusinessStatuses()
        binding.swipeRefreshLayout.isRefreshing = false
        Log.d(TAG, "  ✅ WhatsApp Business refresh completed")
    }

    override fun onDestroyView() {
        Log.d(TAG, "→ onDestroyView() called")
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "  ✅ Binding cleared")
    }
}