package com.example.statussaver.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.statussaver.ui.adapters.MediaAdapter
import com.example.statussaver.data.StatusRepo
import com.example.statussaver.databinding.FragmentMediaBinding
import com.example.statussaver.models.MediaModel
import com.example.statussaver.utils.Constants
import com.example.statussaver.viewmodels.StatusViewModel
import com.example.statussaver.viewmodels.factories.StatusViewModelFactory

class FragmentMedia : Fragment() {
    private var mActivity: Context?=null
    private val binding by lazy {
        FragmentMediaBinding.inflate(layoutInflater)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity=context
    }

    override fun onDetach() {
        super.onDetach()
        mActivity=null
    }
    lateinit var viewModel: StatusViewModel
    lateinit var adapter: MediaAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            arguments?.let {
                val repo = StatusRepo(requireActivity())
                viewModel = ViewModelProvider(
                    requireActivity(),
                    StatusViewModelFactory(repo)
                )[StatusViewModel::class.java]
                val mediaType = it.getString(Constants.MEDIA_TYPE_KEY, "")
                when (mediaType) {
                    Constants.MEDIA_TYPE_WHATSAPP_IMAGES -> {
                        viewModel.whatsAppImagesLiveData.observe(requireActivity()) { unFilteredList ->
                            val filteredList = unFilteredList.distinctBy { model ->
                                model.fileName
                            }
                            Log.d("statuses","the statuses fetching are $filteredList")

                            val list = ArrayList<MediaModel>()
                            filteredList.forEach { model ->
                                list.add(model)
                            }
                            Log.d("statuses","the statuses fetching are ${list.size}")
                            adapter = MediaAdapter(list, requireActivity())
                            mediaRecyclerView.adapter = adapter
                            if (list.size == 0) {
                                tempMediaText.visibility = View.VISIBLE
                            } else {
                                tempMediaText.visibility = View.GONE
                            }

                        }
                    }

                    Constants.MEDIA_TYPE_WHATSAPP_VIDEOS -> {
                        viewModel.whatsAppVideosLiveData.observe(requireActivity()) { unFilteredList ->
                            val filteredList = unFilteredList.distinctBy { model ->
                                model.fileName
                            }

                            val list = ArrayList<MediaModel>()
                            filteredList.forEach { model ->
                                list.add(model)
                            }
                            adapter = MediaAdapter(list, requireActivity())
                            mediaRecyclerView.adapter = adapter
                            if (list.size == 0) {
                                tempMediaText.visibility = View.VISIBLE
                            } else {
                                tempMediaText.visibility = View.GONE
                            }
                        }
                    }

                    Constants.MEDIA_TYPE_WHATSAPP_BUSINESS_IMAGES -> {
                        viewModel.whatsAppBusinessImagesLiveData.observe(requireActivity()) { unFilteredList ->
                            val filteredList = unFilteredList.distinctBy { model ->
                                model.fileName
                            }
                            Log.d("statuses business","the statuses fetching are $filteredList")

                            val list = ArrayList<MediaModel>()
                            filteredList.forEach { model ->
                                list.add(model)
                            }
                            Log.d("statuses business","the statuses fetching are ${list.size}")
                            adapter = MediaAdapter(list, requireActivity())
                            mediaRecyclerView.adapter = adapter
                            if (list.size == 0) {
                                tempMediaText.visibility = View.VISIBLE
                            } else {
                                tempMediaText.visibility = View.GONE
                            }
                        }
                    }

                    Constants.MEDIA_TYPE_WHATSAPP_BUSINESS_VIDEOS -> {
                        viewModel.whatsAppBusinessVideosLiveData.observe(requireActivity()) { unFilteredList ->
                            val filteredList = unFilteredList.distinctBy { model ->
                                model.fileName
                            }

                            val list = ArrayList<MediaModel>()
                            filteredList.forEach { model ->
                                list.add(model)
                            }
                            adapter = MediaAdapter(list, requireActivity())
                            mediaRecyclerView.adapter = adapter
                            if (list.size == 0) {
                                tempMediaText.visibility = View.VISIBLE
                            } else {
                                tempMediaText.visibility = View.GONE
                            }
                        }
                    }

                    Constants.MEDIA_TYPE_SAVED_IMAGES -> {
                        viewModel.getSavedMedia()
                        viewModel.savedImagesLiveData.observe(requireActivity()) { list ->
                            adapter = MediaAdapter(list, requireActivity())
                            mediaRecyclerView.adapter = adapter
                            if (list.size == 0) {
                                tempMediaText.visibility = View.VISIBLE
                            } else {
                                tempMediaText.visibility = View.GONE
                            }
                        }
                    }

                    Constants.MEDIA_TYPE_SAVED_VIDEOS -> {
                        viewModel.getSavedMedia()
                        viewModel.savedVideosLiveData.observe(requireActivity()) { list ->
                            adapter = MediaAdapter(list, requireActivity())
                            mediaRecyclerView.adapter = adapter
                            if (list.size == 0) {
                                tempMediaText.visibility = View.VISIBLE
                            } else {
                                tempMediaText.visibility = View.GONE
                            }
                        }
                    }
                }


            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onDestroy() {
        super.onDestroy()
        mActivity=null
    }
}