package com.example.video_player_lib.api.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
class VideoPlayerViewModel @Inject constructor(
    val exoPlayer: ExoPlayer
) : ViewModel() {

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen = _isFullScreen.asStateFlow()
    private val _resize = MutableStateFlow(AspectRatioFrameLayout.RESIZE_MODE_FIT)
    val resize = _resize.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _FastPlaybackSpeed = MutableStateFlow(2f)
    val FastPlaybackSpeed = _FastPlaybackSpeed.asStateFlow()

    private val _doubleTapSeek = MutableStateFlow(10000L)
    val doubleTapSeek = _doubleTapSeek.asStateFlow()

    var srartTimer: Job? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startTimer()
                } else {
                    stopTimer()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _duration.value = exoPlayer.duration
                }
            }
        })
    }

    private fun startTimer() {
        srartTimer?.cancel()
        srartTimer = viewModelScope.launch {
            while (true) {
                _currentPosition.value = exoPlayer.currentPosition
                delay(100)
            }
        }
    }

    private fun stopTimer() {
        srartTimer?.cancel()
    }

    fun seekByForward(seek: Long) {
        exoPlayer.seekTo(exoPlayer.currentPosition + seek)
    }

    fun seekByReverse(seek: Long) {
        exoPlayer.seekTo(exoPlayer.currentPosition - seek)
    }

    fun praperPlayer(uri: Uri, mimeType: String? = null) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .apply { if (mimeType != null) setMimeType(mimeType) }
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun onRelease() {
        exoPlayer.release()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun isFullScreen(isFullScreen: Boolean) {
        _isFullScreen.value = isFullScreen
    }

    fun updateSize(resize: Int) {
        _resize.value = resize
    }

    fun togglePlayPause(playPause: String) {
        if (playPause == "Play") {
            exoPlayer.play()
        } else if (playPause == "Replay") {
            exoPlayer.seekTo(0L)
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }
    fun onLongPress(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    fun setFastPlaySpreed(speed: Float) {
        _FastPlaybackSpeed.value = speed
    }

    fun setDoubleTapSeek(seek: Long) {
        _doubleTapSeek.value = seek
    }

    fun onClose(){
        exoPlayer.release()
    }
}