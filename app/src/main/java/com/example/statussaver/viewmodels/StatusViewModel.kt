package com.example.statussaver.viewmodels

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.statussaver.data.StatusRepo
import com.example.statussaver.models.MEDIA_TYPE_IMAGE
import com.example.statussaver.models.MEDIA_TYPE_VIDEO
import com.example.statussaver.models.MediaModel
import com.example.statussaver.utils.Constants
import com.example.statussaver.utils.SharedPrefKeys
import com.example.statussaver.utils.SharedPrefUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StatusViewModel(val repo: StatusRepo) : ViewModel() {
    private val wpStatusLiveData get() = repo.whatsAppStatusesLiveData
    private val wpBusinessStatusLiveData get() = repo.whatsAppBusinessStatusesLiveData
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

    private var isPermissionsGranted = false

    init {
        SharedPrefUtils.init(repo.context)

        val wpPermissions =
            SharedPrefUtils.getPrefBoolean(SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED, false)
        val wpBusinessPermissions = SharedPrefUtils.getPrefBoolean(
            SharedPrefKeys.PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED,
            false
        )

        isPermissionsGranted = wpPermissions && wpBusinessPermissions
        Log.d(TAG, "Status View Model: isPermissions=> $isPermissionsGranted ")
        if (isPermissionsGranted) {
            Log.d(TAG, "Status View Model: Permissions Already Granted Getting Statuses ")
            CoroutineScope(Dispatchers.IO).launch {
                repo.getAllStatuses()

            }
            CoroutineScope(Dispatchers.IO).launch {
                repo.getAllStatuses(Constants.TYPE_WHATSAPP_BUSINESS)
            }
        }
    }


    fun getWhatsAppImages() {
        wpStatusLiveData.observe(repo.activity as LifecycleOwner) {
            val tempList = ArrayList<MediaModel>()
            it.forEach {mediaModel->
                if (mediaModel.type == MEDIA_TYPE_IMAGE){
                    tempList.add(mediaModel)
                }
            }
            whatsAppImagesLiveData.postValue(tempList)
        }
    }
    fun getWhatsAppVideos() {
        wpStatusLiveData.observe(repo.activity as LifecycleOwner) {
            val tempList = ArrayList<MediaModel>()
            it.forEach {mediaModel->
                if (mediaModel.type == MEDIA_TYPE_VIDEO){
                    tempList.add(mediaModel)
                }
            }
            whatsAppVideosLiveData.postValue(tempList)
        }
    }


    fun getWhatsAppStatuses() {
        CoroutineScope(Dispatchers.IO).launch {
            repo.getAllStatuses() // Always fetch statuses, even if permissions are already granted
            withContext(Dispatchers.Main) {
                whatsAppImagesLiveData.postValue(repo.whatsAppStatusesLiveData.value?.filter { it.type == MEDIA_TYPE_IMAGE } as ArrayList<MediaModel>?)
                whatsAppVideosLiveData.postValue(repo.whatsAppStatusesLiveData.value?.filter { it.type == MEDIA_TYPE_VIDEO } as ArrayList<MediaModel>?)
            }
        }
    }

    fun getWhatsAppBusinessStatuses() {
        CoroutineScope(Dispatchers.IO).launch {
            repo.getAllStatuses(Constants.TYPE_WHATSAPP_BUSINESS)
            withContext(Dispatchers.Main) {
                whatsAppBusinessImagesLiveData.postValue(repo.whatsAppBusinessStatusesLiveData.value?.filter { it.type == MEDIA_TYPE_IMAGE } as ArrayList<MediaModel>?)
                whatsAppBusinessVideosLiveData.postValue(repo.whatsAppBusinessStatusesLiveData.value?.filter { it.type == MEDIA_TYPE_VIDEO } as ArrayList<MediaModel>?)
            }
        }
    }


    fun getWhatsAppBusinessImages() {
        wpBusinessStatusLiveData.observe(repo.activity as LifecycleOwner) {
            val tempList = ArrayList<MediaModel>()
            it.forEach {mediaModel->
                if (mediaModel.type == MEDIA_TYPE_IMAGE){
                    tempList.add(mediaModel)
                }
            }
            whatsAppBusinessImagesLiveData.postValue(tempList)
        }
    }
    fun getWhatsAppBusinessVideos() {
        wpBusinessStatusLiveData.observe(repo.activity as LifecycleOwner) {
            val tempList = ArrayList<MediaModel>()
            it.forEach {mediaModel->
                if (mediaModel.type == MEDIA_TYPE_VIDEO){
                    tempList.add(mediaModel)
                }
            }
            whatsAppBusinessVideosLiveData.postValue(tempList)
        }
    }

    fun getSavedMedia() {
        CoroutineScope(Dispatchers.IO).launch {
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

                        val model = MediaModel(
                            pathUri = android.net.Uri.fromFile(file).toString(),
                            fileName = file.name,
                            type = fileType ?: MEDIA_TYPE_IMAGE,
                            isDownloaded = true
                        )

                        if (fileType == MEDIA_TYPE_IMAGE) {
                            savedImages.add(model)
                        } else if (fileType == MEDIA_TYPE_VIDEO) {
                            savedVideos.add(model)
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











