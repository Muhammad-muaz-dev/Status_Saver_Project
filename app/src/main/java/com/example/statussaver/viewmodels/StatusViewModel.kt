package com.example.statussaver.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.statussaver.data.StatusRepo
import com.example.statussaver.models.MEDIA_TYPE_IMAGE
import com.example.statussaver.models.MEDIA_TYPE_VIDEO
import com.example.statussaver.models.MediaModel
import com.example.statussaver.utils.Constants
import com.example.statussaver.utils.SharedPrefKeys
import com.example.statussaver.utils.SharedPrefUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StatusViewModel(val repo: StatusRepo) : ViewModel() {
    private val TAG = "StatusViewModel"

    // wp main
    val whatsAppImagesLiveData = MutableLiveData<ArrayList<MediaModel>>()
    val whatsAppVideosLiveData = MutableLiveData<ArrayList<MediaModel>>()

    // wp business
    val whatsAppBusinessImagesLiveData = MutableLiveData<ArrayList<MediaModel>>()
    val whatsAppBusinessVideosLiveData = MutableLiveData<ArrayList<MediaModel>>()

    // saved
    val savedImagesLiveData = MutableLiveData<ArrayList<MediaModel>>()
    val savedVideosLiveData = MutableLiveData<ArrayList<MediaModel>>()

    init {
        SharedPrefUtils.init(repo.context)
        
        // Setup observers for repository data
        repo.whatsAppStatusesLiveData.observeForever { list ->
            whatsAppImagesLiveData.postValue(ArrayList(list.filter { it.type == MEDIA_TYPE_IMAGE }))
            whatsAppVideosLiveData.postValue(ArrayList(list.filter { it.type == MEDIA_TYPE_VIDEO }))
        }
        
        repo.whatsAppBusinessStatusesLiveData.observeForever { list ->
            whatsAppBusinessImagesLiveData.postValue(ArrayList(list.filter { it.type == MEDIA_TYPE_IMAGE }))
            whatsAppBusinessVideosLiveData.postValue(ArrayList(list.filter { it.type == MEDIA_TYPE_VIDEO }))
        }

        val wpPermissions = SharedPrefUtils.getPrefBoolean(SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED, false)
        val wpBusinessPermissions = SharedPrefUtils.getPrefBoolean(SharedPrefKeys.PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED, false)

        if (wpPermissions) getWhatsAppStatuses()
        if (wpBusinessPermissions) getWhatsAppBusinessStatuses()
    }

    fun getWhatsAppStatuses() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllStatuses(Constants.TYPE_WHATSAPP_MAIN)
        }
    }

    fun getWhatsAppBusinessStatuses() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllStatuses(Constants.TYPE_WHATSAPP_BUSINESS)
        }
    }

    fun getSavedMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedImages = ArrayList<MediaModel>()
            val savedVideos = ArrayList<MediaModel>()

            val downloadsDir = java.io.File(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                repo.context.getString(com.example.statussaver.R.string.app_name)
            )

            if (downloadsDir.exists()) {
                val files = downloadsDir.listFiles()
                files?.forEach { file ->
                    if (file.isFile && file.name != ".nomedia") {
                        val extension = com.example.statussaver.utils.getFileExtension(file.name)
                        val fileType = when (extension.lowercase()) {
                            "jpg", "jpeg", "png", "gif" -> MEDIA_TYPE_IMAGE
                            "mp4", "avi", "mkv" -> MEDIA_TYPE_VIDEO
                            else -> null
                        }

                        if (fileType != null) {
                            val model = MediaModel(
                                pathUri = android.net.Uri.fromFile(file).toString(),
                                fileName = file.name,
                                type = fileType,
                                isDownloaded = true
                            )

                            if (fileType == MEDIA_TYPE_IMAGE) {
                                savedImages.add(model)
                            } else {
                                savedVideos.add(model)
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                savedImagesLiveData.postValue(savedImages)
                savedVideosLiveData.postValue(savedVideos)
            }
        }
    }
}
