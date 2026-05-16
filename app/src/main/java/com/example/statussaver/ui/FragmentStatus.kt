package com.example.statussaver.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.statussaver.ui.adapters.MediaViewPagerAdapter
import com.example.statussaver.data.StatusRepo
import com.example.statussaver.databinding.FragmentStatusBinding
import com.example.statussaver.utils.Constants
import com.example.statussaver.utils.SharedPrefKeys
import com.example.statussaver.utils.SharedPrefUtils
import com.example.statussaver.utils.getFolderPermissions
import com.example.statussaver.utils.isAppInstalled
import com.example.statussaver.viewmodels.StatusViewModel
import com.example.statussaver.viewmodels.factories.StatusViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class FragmentStatus : Fragment() {

    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!  // Safe binding access
    private lateinit var type: String
    private val whatsappRequestCode = 101
    private val whatsappBusinessRequestCode = 102
    private val viewPagerTitles = listOf("Images", "Videos")
    private lateinit var viewModel: StatusViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            type = it.getString(Constants.FRAGMENT_TYPE_KEY, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
        // Set up swipe-to-refresh functionality
        binding.swipeRefreshLayout.setOnRefreshListener {
            when (type) {
                Constants.TYPE_WHATSAPP_MAIN -> refreshWhatsAppStatuses()
                Constants.TYPE_WHATSAPP_BUSINESS -> refreshWhatsAppBusinessStatuses()
                else -> Log.e("FragmentStatus", "Unknown fragment type: $type")
            }
        }

        when (type) {
            Constants.TYPE_WHATSAPP_MAIN -> setupWhatsAppUI()
            Constants.TYPE_WHATSAPP_BUSINESS -> setupWhatsAppBusinessUI()
            else -> Log.e("FragmentStatus", "Unknown fragment type: $type")
        }
    }

    private fun setupWhatsAppUI() {
        val isPermissionGranted = SharedPrefUtils.getPrefBoolean(
            SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED, false
        )

        if (isPermissionGranted) {
            getWhatsAppStatuses()
        } else {
            binding.permissionLayoutHolder.visibility = View.VISIBLE
            binding.permissionLayout.btnPermission.setOnClickListener {
                getFolderPermissions(
                    requireContext(),
                    whatsappRequestCode,
                    Constants.getWhatsappUri()
                )
            }

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
            binding.permissionLayoutHolder.visibility = View.VISIBLE
            binding.permissionLayout.btnPermission.setOnClickListener {
                getFolderPermissions(
                    requireContext(),
                    whatsappBusinessRequestCode,
                    Constants.getWhatsappBusinessUri()
                )
            }

        }

        setupViewPager(isBusiness = true)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK && data?.data != null) {
            val treeUri = data.data!!

            runCatching {
                val resolver = requireActivity().contentResolver
                val hasPermission = resolver.persistedUriPermissions.any {
                    it.uri == treeUri && it.isReadPermission
                }

                if (!hasPermission) {
                    resolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                when (requestCode) {
                    whatsappRequestCode -> {
                        SharedPrefUtils.putPrefString(
                            SharedPrefKeys.PREF_KEY_WP_TREE_URI, treeUri.toString()
                        )
                        SharedPrefUtils.putPrefBoolean(
                            SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED, true
                        )
                        fetchStatuses { getWhatsAppStatuses() }
                    }

                    whatsappBusinessRequestCode -> {
                        SharedPrefUtils.putPrefString(
                            SharedPrefKeys.PREF_KEY_WP_BUSINESS_TREE_URI, treeUri.toString()
                        )
                        SharedPrefUtils.putPrefBoolean(
                            SharedPrefKeys.PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED, true
                        )
                        fetchStatuses { getWhatsAppBusinessStatuses() }
                    }
                }
            }.onFailure { e ->
                Log.e("FragmentStatus", "Error granting persistable permission: ${e.message}")
            }
        }
    }

    private fun fetchStatuses(action: () -> Unit) {
        requireActivity().runOnUiThread(action)
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
        fetchStatuses {
            getWhatsAppStatuses()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun refreshWhatsAppBusinessStatuses() {
        binding.swipeRefreshLayout.isRefreshing = true
        fetchStatuses {
            getWhatsAppBusinessStatuses()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }
}
