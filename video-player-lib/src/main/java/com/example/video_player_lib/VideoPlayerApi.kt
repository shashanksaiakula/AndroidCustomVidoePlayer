package com.example.video_player_lib

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.media3.exoplayer.ExoPlayer
import com.example.video_player_lib.presentation.VideoPickerViewModel
import com.example.video_player_lib.repository.VideoRepositoryImpl
import com.example.video_player_lib.utils.ExoPlayerUtils
import dagger.hilt.android.UnstableApi

@OptIn(UnstableApi::class)
object VideoPlayerApi {
    private var exoPlayer: ExoPlayer? = null
    private var context: Context? = null
    private var viewModel: VideoPickerViewModel? = null

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    // Prepare player with URI (like preparePlayer in ViewModel)
            /**
             * Initializes the video player with the given context.
             * @param context The application context.
             */
    fun initialize(context: Context) : VideoPlayerApi {
        if (exoPlayer == null) {
            this.context = context
            // i want to use for mapp module
            exoPlayer = ExoPlayerUtils.createExoPlayer(context)
            val repository = VideoRepositoryImpl(context)
            viewModel = VideoPickerViewModel(repository, exoPlayer!!)
        }
            return this
    }

    /**
     * Prepares the player with a video URI.
     * @param uri The video URI.
     * @param mimeType Optional MIME type.
     */
    // Prepare player with URI (like preparePlayer in ViewModel)
    fun prepare(uri: Uri, mimeType: String? = null) {
        viewModel?.preparePlayer(uri, mimeType)
    }

    fun play() {
        viewModel?.togglePlayPause()
    }

    fun pause() {
        viewModel?.togglePlayPause()
    }

    fun seekTo(position: Long) {
        viewModel?.seekTo(position)
    }

    fun skipForward(skipForward: Long = 5000) {
        val newPos = (exoPlayer?.currentPosition ?: 0) + skipForward
        seekTo(newPos)
    }

    fun skipBackward(skipBackward: Long = 5000) {
        val newPos = (exoPlayer?.currentPosition ?: 0) - skipBackward
        seekTo(newPos)
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        context = null
        viewModel = null
    }

    fun onLongPress(speed: Float) {
        viewModel?.onLongPress(speed)
    }

    fun onLongDefault() {
        viewModel?.onLongPress()
    }
    fun stop() {
        exoPlayer?.stop()
    }

    private var playerListener: VideoPlayerListener? = null

    fun setListener(listener: VideoPlayerListener) {
        this.playerListener = listener
        // Attach to ExoPlayer
        exoPlayer?.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                playerListener?.onPlaybackStateChanged(playbackState)
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playerListener?.onIsPlayingChanged(isPlaying)
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playerListener?.onPlayerError(error.message ?: "Unknown Error")
            }
        })
    }
    /** Returns percentage (0-100) of content buffered */
    fun getBufferedPercentage(): Int = exoPlayer?.bufferedPercentage ?: 0

    /** Returns the actual buffered position in Ms */
    fun getBufferedPosition(): Long = exoPlayer?.bufferedPosition ?: 0L

    // to play muultiple videos
    fun getMediaMetadata(): androidx.media3.common.MediaMetadata? = exoPlayer?.mediaMetadata

    // return player view
    fun getPlayerView(): View {
        return  ExoPlayerUtils.getPlayerView(context!!, exoPlayer)
    }
}


interface VideoPlayerListener {
    fun onPlaybackStateChanged(state: Int) // IDLE, BUFFERING, READY, ENDED
    fun onIsPlayingChanged(isPlaying: Boolean)
    fun onPlayerError(error: String)
}
