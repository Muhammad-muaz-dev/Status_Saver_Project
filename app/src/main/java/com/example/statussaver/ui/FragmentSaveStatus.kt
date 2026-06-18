package com.example.statussaver.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.statussaver.databinding.FragmentSaveStatusBinding
import com.example.statussaver.ui.adapters.MediaViewPagerAdapter
import com.example.statussaver.utils.Constants
import com.google.android.material.tabs.TabLayoutMediator
import pub.devrel.easypermissions.EasyPermissions

class FragmentSaveStatus : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentSaveStatusBinding? = null
    private val binding get() = _binding!!

    private val viewPagerTitles = listOf("Images", "Videos")

    private val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSaveStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (EasyPermissions.hasPermissions(requireContext(), *perms)) {
            setupViewPager()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs storage permissions to save and display statuses.",
                123,
                *perms
            )
        }
    }

    private fun setupViewPager() {
        val adapter = MediaViewPagerAdapter(
            requireActivity(),
            Constants.MEDIA_TYPE_SAVED_IMAGES,
            Constants.MEDIA_TYPE_SAVED_VIDEOS
        )

        binding.saveStatusViewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.saveStatusViewPager) { tab, pos ->
            tab.text = viewPagerTitles.getOrNull(pos) ?: "Unknown"
        }.attach()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        setupViewPager()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // Permission denied flow
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
