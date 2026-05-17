package com.example.statussaver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.statussaver.databinding.FragmentSaveStatusBinding
import com.example.statussaver.ui.adapters.MediaViewPagerAdapter
import com.example.statussaver.utils.Constants
import com.google.android.material.tabs.TabLayoutMediator

class FragmentSaveStatus : Fragment() {

    private var _binding: FragmentSaveStatusBinding? = null
    private val binding get() = _binding!!

    private val viewPagerTitles = listOf("Images", "Videos")

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
        setupViewPager()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
