package com.example.video_player_lib

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel
import com.example.video_player_lib.utils.ExoPlayerUtils

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class VideoPlayerApi(private val context: Context) {
    private var exoPlayer: ExoPlayer? = ExoPlayerUtils.createExoPlayer(context)
    private var viewModel: VideoPlayerViewModel? = VideoPlayerViewModel(exoPlayer!!)
    private var listener: VideoPlayerListener? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            listener?.onPlaybackStateChanged(state)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            listener?.onIsPlayingChanged(isPlaying)
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            listener?.onPlayerError(error.message ?: "Unknown Error")
        }
    }

    init {
        exoPlayer?.addListener(playerListener)
    }

    companion object {
        // Keep for backward compatibility with existing code
        fun initialize(context: Context): VideoPlayerApi {
            return VideoPlayerApi(context)
        }
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
        this.listener = listener
    }

    fun prepare(uri: Uri) {
        viewModel?.preparePlayer(uri)
    }

    fun play() {
        viewModel?.play()
    }

    fun pause() {
        viewModel?.pause()
    }

    fun stop() {
        viewModel?.stop()
    }

    fun onClose() {
        exoPlayer?.removeListener(playerListener)
        viewModel?.onClose()
        exoPlayer = null
        viewModel = null
    }

    fun getFullPlayerView(uri: Uri, showOverLayUI: Boolean): View {
        return ExoPlayerUtils.getFullPlayerView(context, viewModel!!, uri, showOverLayUI)
    }
}

interface VideoPlayerListener {
    fun onPlaybackStateChanged(state: Int)
    fun onIsPlayingChanged(isPlaying: Boolean)
    fun onPlayerError(error: String)
}
