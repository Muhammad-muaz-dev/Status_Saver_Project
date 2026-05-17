package com.example.statussaver.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.statussaver.ui.adapters.VideoPreviewAdapter
import com.example.statussaver.databinding.ActivityVideosPreviewBinding
import com.example.statussaver.models.MediaModel
import com.example.statussaver.utils.Constants

class VideosPreview : AppCompatActivity() {

    private val TAG = "VideosPreview"
    private val binding by lazy { ActivityVideosPreviewBinding.inflate(layoutInflater) }
    private lateinit var adapter: VideoPreviewAdapter
    private var sharedPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        setContentView(binding.root)

        // Initialize the SINGLE shared player instance
        sharedPlayer = ExoPlayer.Builder(this).build()

        binding.apply {
            val list = intent.getSerializableExtra(Constants.MEDIA_LIST_KEY) as? ArrayList<MediaModel> ?: arrayListOf()
            val scrollTo = intent.getIntExtra(Constants.MEDIA_SCROLL_KEY, 0)

            adapter = VideoPreviewAdapter(list, this@VideosPreview, sharedPlayer!!)
            videoRecyclerView.adapter = adapter

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(videoRecyclerView)
            videoRecyclerView.scrollToPosition(scrollTo)

            // Auto-play initial video after a short delay
            Handler(Looper.getMainLooper()).postDelayed({
                adapter.playAt(scrollTo, videoRecyclerView)
            }, 200)

            btnback.setOnClickListener { finish() }

            videoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                        val centerView = snapHelper.findSnapView(layoutManager)
                        if (centerView != null && layoutManager != null) {
                            val position = layoutManager.getPosition(centerView)
                            adapter.playAt(position, videoRecyclerView)
                        }
                    } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        sharedPlayer?.pause()
                    }
                }
            })
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onPause() {
        super.onPause()
        sharedPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the single player immediately - no UI freezing!
        sharedPlayer?.stop()
        sharedPlayer?.release()
        sharedPlayer = null
    }
}
