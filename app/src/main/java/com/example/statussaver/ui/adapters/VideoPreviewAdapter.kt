package com.example.statussaver.ui.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.example.statussaver.R
import com.example.statussaver.databinding.ItemVideoPreviewBinding
import com.example.statussaver.models.MediaModel
import com.example.statussaver.utils.isStatusExist
import com.example.statussaver.utils.saveStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoPreviewAdapter(
    private val list: ArrayList<MediaModel>,
    private val context: Context,
    private val sharedPlayer: ExoPlayer
) : RecyclerView.Adapter<VideoPreviewAdapter.ViewHolder>() {

    private var currentPlayerView: PlayerView? = null

    inner class ViewHolder(val binding: ItemVideoPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mediaModel: MediaModel) {
            binding.apply {
                // Important: Ensure no stale player reference on bind
                if (playerView.player == sharedPlayer && currentPlayerView != playerView) {
                    playerView.player = null
                }
                
                updateDownloadIcon(mediaModel)
                tools.repost.setOnClickListener { repostMedia(mediaModel) }
                tools.share.setOnClickListener { shareMedia(mediaModel) }
                tools.download.setOnClickListener { saveVideo(mediaModel) }
            }
        }

        private fun updateDownloadIcon(mediaModel: MediaModel) {
            mediaModel.isDownloaded = context.isStatusExist(mediaModel.fileName)
            val downloadImage = if (mediaModel.isDownloaded) {
                R.drawable.ic_downloaded
            } else {
                R.drawable.ic_download
            }
            binding.tools.statusDownload.setImageResource(downloadImage)
        }

        private fun saveVideo(mediaModel: MediaModel) {
            CoroutineScope(Dispatchers.IO).launch {
                val isDownloaded = context.saveStatus(mediaModel)
                Handler(Looper.getMainLooper()).post {
                    if (isDownloaded) {
                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                        mediaModel.isDownloaded = true
                        updateDownloadIcon(mediaModel)
                    } else {
                        Toast.makeText(context, "Unable to Save", Toast.LENGTH_SHORT).show()
                        binding.tools.statusDownload.setImageResource(R.drawable.ic_download)
                    }
                }
            }
        }

        private fun repostMedia(mediaModel: MediaModel) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(mediaModel.pathUri))
                putExtra(Intent.EXTRA_TEXT, "Check out this video status!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Repost via"))
            } catch (e: Exception) {
                Toast.makeText(context, "No app available to repost", Toast.LENGTH_SHORT).show()
            }
        }

        private fun shareMedia(mediaModel: MediaModel) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(mediaModel.pathUri))
                putExtra(Intent.EXTRA_TEXT, "Check out this video status!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Share via"))
            } catch (e: Exception) {
                Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemVideoPreviewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    fun playAt(position: Int, recyclerView: RecyclerView) {
        if (position < 0 || position >= list.size) return
        
        val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ViewHolder
        if (holder != null) {
            val newPlayerView = holder.binding.playerView
            
            // 1. If the player is already attached to a different view, detach it first
            if (currentPlayerView != null && currentPlayerView != newPlayerView) {
                currentPlayerView?.player = null
            }
            
            // 2. Attach to the new view
            newPlayerView.player = sharedPlayer
            currentPlayerView = newPlayerView
            
            // 3. Prepare and play
            val mediaItem = MediaItem.fromUri(list[position].pathUri)
            sharedPlayer.stop() // Stop previous to ensure clean state
            sharedPlayer.clearMediaItems()
            sharedPlayer.setMediaItem(mediaItem)
            sharedPlayer.repeatMode = Player.REPEAT_MODE_ONE
            sharedPlayer.prepare()
            sharedPlayer.playWhenReady = true
        }
    }
}
