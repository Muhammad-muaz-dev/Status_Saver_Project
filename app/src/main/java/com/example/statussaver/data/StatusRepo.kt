package com.example.statussaver.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import com.example.statussaver.models.MEDIA_TYPE_IMAGE
import com.example.statussaver.models.MEDIA_TYPE_VIDEO
import com.example.statussaver.models.MediaModel
import com.example.statussaver.utils.Constants
import com.example.statussaver.utils.SharedPrefKeys
import com.example.statussaver.utils.SharedPrefUtils
import com.example.statussaver.utils.getFileExtension
import com.example.statussaver.utils.isStatusExist


class StatusRepo(val context: Context) {

    val whatsAppStatusesLiveData = MutableLiveData<ArrayList<MediaModel>>()
    val whatsAppBusinessStatusesLiveData = MutableLiveData<ArrayList<MediaModel>>()

    val activity = context as Activity

    private val wpStatusesList = ArrayList<MediaModel>()
    private val wpBusinessStatusesList = ArrayList<MediaModel>()

    private val TAG = "StatusRepo"

    fun getAllStatuses(whatsAppType: String = Constants.TYPE_WHATSAPP_MAIN) {
        val treeUri = when (whatsAppType) {
            Constants.TYPE_WHATSAPP_MAIN -> {
                SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_TREE_URI, "")?.toUri()
            }

            else -> {
                SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_BUSINESS_TREE_URI, "")
                    ?.toUri()
            }
        } ?: return

        Log.d(TAG, "getAllStatuses: $treeUri")

        activity.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val rootFolder = DocumentFile.fromTreeUri(activity, treeUri)
        
        if (whatsAppType == Constants.TYPE_WHATSAPP_MAIN) {
            wpStatusesList.clear()
        } else {
            wpBusinessStatusesList.clear()
        }

        rootFolder?.let {
            val foldersToScan = mutableListOf<DocumentFile>()
            
            // Check the root folder itself
            foldersToScan.add(it)

            // Look for the hidden .Statuses folder
            it.findFile(".Statuses")?.let { statusFolder ->
                foldersToScan.add(statusFolder)
            }
            
            // Also optionally scan standard media folders
            it.findFile("WhatsApp Images")?.let { foldersToScan.add(it) }
            it.findFile("WhatsApp Video")?.let { foldersToScan.add(it) }
            it.findFile("WhatsApp Business Images")?.let { foldersToScan.add(it) }
            it.findFile("WhatsApp Business Video")?.let { foldersToScan.add(it) }

            foldersToScan.forEach { folder ->
                folder.listFiles().forEach { file ->
                    if (file.name != ".nomedia" && file.isFile) {
                        val extension = getFileExtension(file.name!!).lowercase()
                        val type = when (extension) {
                            "mp4", "mkv", "avi" -> MEDIA_TYPE_VIDEO
                            "jpg", "jpeg", "png" -> MEDIA_TYPE_IMAGE
                            else -> null
                        }

                        if (type != null) {
                            val model = MediaModel(
                                pathUri = file.uri.toString(),
                                fileName = file.name!!,
                                type = type,
                                isDownloaded = context.isStatusExist(file.name!!)
                            )
                            if (whatsAppType == Constants.TYPE_WHATSAPP_MAIN) {
                                wpStatusesList.add(model)
                            } else {
                                wpBusinessStatusesList.add(model)
                            }
                        }
                    }
                }
            }

            if (whatsAppType == Constants.TYPE_WHATSAPP_MAIN) {
                Log.d(TAG, "getAllStatuses: Pushing Value to Wp live Data, count: ${wpStatusesList.size}")
                whatsAppStatusesLiveData.postValue(wpStatusesList)
            } else {
                Log.d(TAG, "getAllStatuses: Pushing Value to Wp Business live Data, count: ${wpBusinessStatusesList.size}")
                whatsAppBusinessStatusesLiveData.postValue(wpBusinessStatusesList)
            }
        }
    }
}
