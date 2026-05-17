package com.example.statussaver.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

fun getFolderPermissions(context: Context, REQUEST_CODE: Int, initialUri: Uri) {
    val activity = context as Activity
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

    // For ACTION_OPEN_DOCUMENT_TREE, the initial URI often works better if it's a tree URI.
    // We convert the document URI to a tree URI if possible.
    val scheme = initialUri.scheme
    val host = initialUri.host
    val path = initialUri.path

    val treeUri = if (scheme == "content" && host == "com.android.externalstorage.documents" && path?.startsWith("/document/") == true) {
        Uri.parse("content://$host" + path.replaceFirst("/document/", "/tree/"))
    } else {
        initialUri
    }

    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, treeUri)
    intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
    activity.startActivityForResult(intent, REQUEST_CODE)
}
