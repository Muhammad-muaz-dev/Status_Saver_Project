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
        val treeUriString = when (whatsAppType) {
            Constants.TYPE_WHATSAPP_MAIN -> {
                SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_TREE_URI, "")
            }

            else -> {
                SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_BUSINESS_TREE_URI, "")
            }
        }
        
        if (treeUriString.isNullOrEmpty()) return
        
        val treeUri = treeUriString.toUri()
        Log.d(TAG, "getAllStatuses: $treeUri")

        activity.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val fileDocument = DocumentFile.fromTreeUri(activity, treeUri)

        if (whatsAppType == Constants.TYPE_WHATSAPP_MAIN) {
            wpStatusesList.clear()
        } else {
            wpBusinessStatusesList.clear()
        }

        fileDocument?.let { treeRoot ->
            Log.d(TAG, "getAllStatuses: treeRoot name: ${treeRoot.name}, uri: ${treeRoot.uri}")
            
            // If the user selected a parent folder (like 'Media' or 'WhatsApp'), try to find the '.Statuses' subfolder
            // We check common names and also look for them in the current folder or one level down
            var statusesFolder: DocumentFile? = null
            
            val rootName = treeRoot.name?.lowercase() ?: ""
            if (rootName == ".statuses" || rootName == "statuses") {
                statusesFolder = treeRoot
            } else {
                // Look for .Statuses in current folder
                statusesFolder = treeRoot.findFile(".Statuses") ?: treeRoot.findFile("Statuses")
                
                // If still not found, and we are in 'WhatsApp', look in 'Media/.Statuses'
                if (statusesFolder == null && (rootName == "whatsapp" || rootName == "whatsapp business")) {
                    val media = treeRoot.findFile("Media")
                    statusesFolder = media?.findFile(".Statuses") ?: media?.findFile("Statuses")
                }
            }
            
            // Fallback to treeRoot if no .Statuses folder found
            val finalFolder = statusesFolder ?: treeRoot
            Log.d(TAG, "getAllStatuses: Using folder: ${finalFolder.name}")

            finalFolder.listFiles().forEach { file ->
                if (file.name != null && file.name != ".nomedia" && file.isFile) {
                    val isDownloaded = context.isStatusExist(file.name!!)
                    val extension = getFileExtension(file.name!!).lowercase()
                    
                    val type = if (extension == "mp4" || extension == "webm" || extension == "mkv" || extension == "avi" || extension == "3gp" || extension == "mov") {
                        MEDIA_TYPE_VIDEO
                    } else if (extension == "jpg" || extension == "jpeg" || extension == "png" || extension == "gif" || extension == "webp") {
                        MEDIA_TYPE_IMAGE
                    } else {
                        // Default to image if unknown extension, many images have no extension in some versions
                        // or have names like "status_12345"
                        MEDIA_TYPE_IMAGE
                    }

                    val model = MediaModel(
                        pathUri = file.uri.toString(),
                        fileName = file.name!!,
                        type = type,
                        isDownloaded = isDownloaded
                    )
                    
                    if (whatsAppType == Constants.TYPE_WHATSAPP_MAIN) {
                        wpStatusesList.add(model)
                    } else {
                        wpBusinessStatusesList.add(model)
                    }
                }
            }
            
            // If we still have nothing and we were in 'Media', maybe we should scan other folders?
            // But usually .Statuses is enough.
            when (whatsAppType) {
                Constants.TYPE_WHATSAPP_MAIN -> {
                    Log.d(TAG, "getAllStatuses: Pushing Value to Wp live Data")
                    whatsAppStatusesLiveData.postValue(wpStatusesList)
                }

                else -> {
                    Log.d(TAG, "getAllStatuses: Pushing Value to Wp Business live Data")
                    whatsAppBusinessStatusesLiveData.postValue(wpBusinessStatusesList)
                }

            }

        }


    }


}
