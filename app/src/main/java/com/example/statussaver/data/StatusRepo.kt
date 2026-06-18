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

    private val activity = context as Activity
    private val TAG = "StatusRepo"

    fun getAllStatuses(whatsAppType: String = Constants.TYPE_WHATSAPP_MAIN) {
        // Get the correct URI based on type
        val treeUriString = when (whatsAppType) {
            Constants.TYPE_WHATSAPP_MAIN -> {
                SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_TREE_URI, "")
            }
            Constants.TYPE_WHATSAPP_BUSINESS -> {
                SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_BUSINESS_TREE_URI, "")
            }
            else -> ""
        }

        Log.d(TAG, "getAllStatuses: whatsAppType=$whatsAppType")
        Log.d(TAG, "getAllStatuses: treeUriString=$treeUriString")

        // Return early if URI is empty
        if (treeUriString.isNullOrEmpty()) {
            Log.w(TAG, "getAllStatuses: URI is empty for type $whatsAppType - returning empty list")
            when (whatsAppType) {
                Constants.TYPE_WHATSAPP_MAIN -> {
                    whatsAppStatusesLiveData.postValue(ArrayList())
                }
                Constants.TYPE_WHATSAPP_BUSINESS -> {
                    whatsAppBusinessStatusesLiveData.postValue(ArrayList())
                }
            }
            return
        }

        val treeUri = treeUriString.toUri()
        Log.d(TAG, "getAllStatuses: Converted URI: $treeUri")

        try {
            // Take persistable URI permission
            activity.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val fileDocument = DocumentFile.fromTreeUri(activity, treeUri)

            if (fileDocument == null) {
                Log.e(TAG, "getAllStatuses: DocumentFile is null for $treeUri")
                when (whatsAppType) {
                    Constants.TYPE_WHATSAPP_MAIN -> {
                        whatsAppStatusesLiveData.postValue(ArrayList())
                    }
                    Constants.TYPE_WHATSAPP_BUSINESS -> {
                        whatsAppBusinessStatusesLiveData.postValue(ArrayList())
                    }
                }
                return
            }

            // Create a new list for this specific type
            val statusesList = ArrayList<MediaModel>()

            Log.d(TAG, "getAllStatuses: treeRoot name: ${fileDocument.name}, uri: ${fileDocument.uri}")

            // Determine which folder to use
            var statusesFolder: DocumentFile? = null

            val rootName = fileDocument.name?.lowercase() ?: ""
            if (rootName == ".statuses" || rootName == "statuses") {
                statusesFolder = fileDocument
            } else {
                // Look for .Statuses in current folder
                statusesFolder = fileDocument.findFile(".Statuses") ?: fileDocument.findFile("Statuses")

                // If still not found, and we are in 'WhatsApp', look in 'Media/.Statuses'
                if (statusesFolder == null && (rootName == "whatsapp" || rootName == "whatsapp business")) {
                    val media = fileDocument.findFile("Media")
                    statusesFolder = media?.findFile(".Statuses") ?: media?.findFile("Statuses")
                }
            }

            val finalFolder = statusesFolder ?: fileDocument
            Log.d(TAG, "getAllStatuses: Using folder: ${finalFolder.name}")

            // List files safely
            val files = finalFolder.listFiles()
            Log.d(TAG, "getAllStatuses: Found ${files.size} items in folder")

            files.forEach { file ->
                if (file.name != null && file.name != ".nomedia" && file.isFile) {
                    val isDownloaded = context.isStatusExist(file.name!!)
                    val extension = getFileExtension(file.name!!).lowercase()

                    val type = if (extension == "mp4" || extension == "webm" || extension == "mkv" ||
                        extension == "avi" || extension == "3gp" || extension == "mov") {
                        MEDIA_TYPE_VIDEO
                    } else if (extension == "jpg" || extension == "jpeg" || extension == "png" ||
                        extension == "gif" || extension == "webp") {
                        MEDIA_TYPE_IMAGE
                    } else {
                        // Default to image
                        MEDIA_TYPE_IMAGE
                    }

                    val model = MediaModel(
                        pathUri = file.uri.toString(),
                        fileName = file.name!!,
                        type = type,
                        isDownloaded = isDownloaded
                    )

                    statusesList.add(model)
                }
            }

            Log.d(TAG, "getAllStatuses: Final list size for $whatsAppType: ${statusesList.size}")

            // Post the value only once with the complete list
            when (whatsAppType) {
                Constants.TYPE_WHATSAPP_MAIN -> {
                    Log.d(TAG, "getAllStatuses: Posting ${statusesList.size} items to WhatsApp LiveData")
                    whatsAppStatusesLiveData.postValue(statusesList)
                }
                Constants.TYPE_WHATSAPP_BUSINESS -> {
                    Log.d(TAG, "getAllStatuses: Posting ${statusesList.size} items to WhatsApp Business LiveData")
                    whatsAppBusinessStatusesLiveData.postValue(statusesList)
                }
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "getAllStatuses: SecurityException - Permission denied: ${e.message}")
            when (whatsAppType) {
                Constants.TYPE_WHATSAPP_MAIN -> {
                    whatsAppStatusesLiveData.postValue(ArrayList())
                }
                Constants.TYPE_WHATSAPP_BUSINESS -> {
                    whatsAppBusinessStatusesLiveData.postValue(ArrayList())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllStatuses: Exception: ${e.message}", e)
            when (whatsAppType) {
                Constants.TYPE_WHATSAPP_MAIN -> {
                    whatsAppStatusesLiveData.postValue(ArrayList())
                }
                Constants.TYPE_WHATSAPP_BUSINESS -> {
                    whatsAppBusinessStatusesLiveData.postValue(ArrayList())
                }
            }
        }
    }
}