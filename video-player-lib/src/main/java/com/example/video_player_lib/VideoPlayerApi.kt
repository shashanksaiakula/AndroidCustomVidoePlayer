package com.example.video_player_lib

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.video_player_lib.api.viewmodel.TranscriptViewModel
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel
import com.example.video_player_lib.utils.ExoPlayerUtils

@OptIn(UnstableApi::class)
class VideoPlayerApi(private val context: Context) {
    private var exoPlayer: ExoPlayer? = ExoPlayerUtils.createExoPlayer(context)
    private var viewModel: VideoPlayerViewModel? = VideoPlayerViewModel(exoPlayer!!,context)
    private val transcriptViewModel = TranscriptViewModel(
        dictionaryRepository = TODO()
    )
    private var listener: VideoPlayerListener? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            listener?.onPlaybackStateChanged(state)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            listener?.onIsPlayingChanged(isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
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

    fun skipForword(seek: Long) {
        viewModel?.seekByForward(seek)
    }

    fun skipBackword(seek: Long) {
        viewModel?.seekByReverse(seek)
    }

    fun setLongPressSpeed(speed: Float) {
        viewModel?.onLongPress(speed)
    }

    fun seekTo(position: Long) {
        viewModel?.seekTo(position)
    }

    fun setFastPlaybackSpeed(speed: Float) {
        viewModel?.setFastPlaySpreed(speed)
    }

    fun setListener(listener: VideoPlayerListener) {
        this.listener = listener
    }

    fun fastFarwordSpeed(speed: Float) {
        viewModel?.setFastPlaySpreed(speed)
    }

    fun prepare(uri: Uri) {
        viewModel?.preparePlayer(uri)
        viewModel!!.getIdFromUri(uri)
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

    fun getId(): Long {
        return viewModel!!.id.value
    }

    fun getExoplayer(): ExoPlayer {
        return viewModel!!.exoPlayer
    }

    fun addNote(note: String, id: Long) {
        viewModel!!.addNote(note, id)
    }

    fun getNotesList(): Map<Long, List<String>> {
        return viewModel!!.listOfNotes.value
    }
    fun getTimeStamp() : String {
        return viewModel!!.getTileStamp()
    }

    fun isFullScreen() : Boolean {
        return viewModel!!.isFullScreen.value
    }

    fun getFullPlayerView(uri: Uri, showOverLayUI: Boolean, transcriptViewModel: TranscriptViewModel): View {
        viewModel!!.getIdFromUri(uri)
        return ExoPlayerUtils.getFullPlayerView(context, viewModel!!, uri, showOverLayUI, transcriptViewModel)
    }
}

interface VideoPlayerListener {
    fun onPlaybackStateChanged(state: Int)
    fun onIsPlayingChanged(isPlaying: Boolean)
    fun onPlayerError(error: String)
}
