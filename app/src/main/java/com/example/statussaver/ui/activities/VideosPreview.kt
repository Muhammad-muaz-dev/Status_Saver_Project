package com.example.statussaver.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.statussaver.ui.adapters.VideoPreviewAdapter
import com.example.statussaver.databinding.ActivityVideosPreviewBinding
import com.example.statussaver.models.MediaModel
import com.example.statussaver.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideosPreview : AppCompatActivity() {

    private val TAG = "VideosPreview"
    private val binding by lazy { ActivityVideosPreviewBinding.inflate(layoutInflater) }
    private lateinit var adapter: VideoPreviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        setContentView(binding.root)

        binding.apply {
            val list = intent.getSerializableExtra(Constants.MEDIA_LIST_KEY) as? ArrayList<MediaModel> ?: arrayListOf()
            val scrollTo = intent.getIntExtra(Constants.MEDIA_SCROLL_KEY, 0)

            adapter = VideoPreviewAdapter(list, this@VideosPreview)
            videoRecyclerView.adapter = adapter

            // Snap to the current item
            PagerSnapHelper().attachToRecyclerView(videoRecyclerView)
            videoRecyclerView.scrollToPosition(scrollTo)


            binding?.btnback?.setOnClickListener {  finish() }
            // Handle RecyclerView scrolling
            videoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        Log.d(TAG, "RecyclerView is being dragged. Stopping all players.")
                        stopAllPlayers()
                    }
                }
            })
        }
    }

    // Stop all video players in RecyclerView
    private fun stopAllPlayers() {
        CoroutineScope(Dispatchers.Main).launch {
            for (i in 0 until binding.videoRecyclerView.childCount) {
                val child = binding.videoRecyclerView.getChildAt(i)
                val viewHolder = binding.videoRecyclerView.getChildViewHolder(child)
                if (viewHolder is VideoPreviewAdapter.ViewHolder) {
                    viewHolder.releasePlayer()  // ✅ Fix: Using correct function
                }
            }
        }
    }
    private fun hideSystemBars() {

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.hide(
            WindowInsetsCompat.Type.systemBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onPause() {
        super.onPause()
        stopAllPlayers()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAllPlayers()
    }
}
