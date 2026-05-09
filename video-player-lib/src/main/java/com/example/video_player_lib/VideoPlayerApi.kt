package com.example.video_player_lib

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.media3.exoplayer.ExoPlayer
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel
import com.example.video_player_lib.utils.ExoPlayerUtils
import dagger.hilt.android.UnstableApi

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
object VideoPlayerApi {
    private var exoPlayer: ExoPlayer? = null
    private var context: Context? = null
    private var viewModel: VideoPlayerViewModel? = null
    private var uri: Uri? = null

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    // Prepare player with URI (like preparePlayer in ViewModel)
            /**
             * Initializes the video player with the given context.
             * @param context The application context.
            //             */
    fun initialize(context: Context): VideoPlayerApi {
        if (exoPlayer == null) {
            this.context = context
            // i want to use for mapp module
            exoPlayer = ExoPlayerUtils.createExoPlayer(context)
            viewModel = VideoPlayerViewModel(exoPlayer!!)
        }
        return this
    }

    fun setLongPressSpeed(speed: Float) {
        viewModel?.onLongPress(speed)
    }

    fun setDoubleTapSeek(seek: Long) {
        viewModel?.setDoubleTapSeek(seek)
    }

    fun setFastPlaybackSpeed(speed: Float) {
        viewModel?.setFastPlaySpreed(speed)
    }

    fun setListener(listener: VideoPlayerListener) {
        fun onPlaybackStateChanged(state: Int): String {
            return when (state) {
                ExoPlayer.STATE_IDLE -> "IDLE"
                ExoPlayer.STATE_BUFFERING -> "BUFFERING"
                ExoPlayer.STATE_READY -> "READY"
                ExoPlayer.STATE_ENDED -> "ENDED"
                else -> "UNKNOWN"
            }
        }

        fun onIsPlayingChanged(isPlaying: Boolean): String {
            return if (isPlaying) "PLAYING" else "PAUSED"
        }

        fun onPlayerError(error: String): String {
            return "ERROR: $error"
        }
    }


    fun getFullPlayerView(uri: Uri): View {
        return ExoPlayerUtils.getFullPlayerView(context!!, viewModel!!, uri)
    }
}


interface VideoPlayerListener {
    fun onPlaybackStateChanged(state: Int) // IDLE, BUFFERING, READY, ENDED
    fun onIsPlayingChanged(isPlaying: Boolean)
    fun onPlayerError(error: String)
}
