package com.example.video_player_lib.api.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@UnstableApi
class VideoPlayerViewModel @Inject constructor(
    val exoPlayer: ExoPlayer,
    @ApplicationContext private val context: Context // Add this
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
    private val _valume = MutableStateFlow(1f)
    val valume = _valume.asStateFlow()
    private val _showVolume = MutableStateFlow(false)
    val showVolume = _showVolume.asStateFlow()
    private val _mute = MutableStateFlow(false)
    val mute = _mute.asStateFlow()
    var srartTimer: Job? = null
    var audioManager: AudioManager? = null
    private val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
    private val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
    private val EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE"
    private val _listOfNotes = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val listOfNotes = _listOfNotes.asStateFlow()

//    private val _id = MutableStateFlow<MutableList<Long>>(mutableListOf())
    private val _id = MutableStateFlow(0L)
    val id = _id.asStateFlow()


    // register receiver
    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action != VOLUME_CHANGED_ACTION) return
            val stream = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1)
            if (stream != AudioManager.STREAM_MUSIC) return

            val newVol = intent.getIntExtra(EXTRA_VOLUME_STREAM_VALUE, 0)
            val max =
                audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 1
            val fraction = newVol.toFloat() / max.toFloat()
            _valume.value = fraction
            exoPlayer.volume = fraction
            _mute.value = (newVol == 0)
        }
    }

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.let { am ->
            val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val fraction = current.toFloat() / max.toFloat()
            _valume.value = fraction
            exoPlayer.volume = fraction
            _mute.value = (current == 0)
        }
        context.registerReceiver(volumeReceiver, IntentFilter(VOLUME_CHANGED_ACTION))
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

    fun preparePlayer(uri: Uri, mimeType: String? = null) {
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
        Log.e("check", "isFullScreen: $isFullScreen")
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

    fun stop() {
        exoPlayer.stop()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun play() {
        exoPlayer.play()
    }

    private var lastNonZeroVolume = _valume.value.coerceAtLeast(0f)

    fun volumeReading(volumeFraction: Float) {
        val am = audioManager ?: return
        val v = volumeFraction.coerceIn(0f, 1f) // ensure 0..1

        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val targetVolume = (v * max).roundToInt() // nearest step

        // set system stream volume (requires MODIFY_AUDIO_SETTINGS in manifest)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)

        // normalized player volume 0f..1f
        val normalized = targetVolume.toFloat() / max.toFloat()

        // remember last non-zero for unmute
        if (normalized > 0f) lastNonZeroVolume = normalized

        _valume.value = normalized
        exoPlayer.volume = normalized
        _mute.value = (targetVolume == 0)
    }

    fun showVolume(valume: Boolean) {
        _showVolume.value = valume
    }

    fun getIdFromUri(uri: Uri) {
//        id.value.add(uri.lastPathSegment?.substringAfterLast("media/")?.toLongOrNull() ?: 0L)
        _id.value = uri.lastPathSegment?.substringAfterLast("media/")?.toLongOrNull() ?: 0L
        Log.e("check", "getIdFromUri: ${_id.value}")
    }

    fun playNext(){
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem() // Modern ExoPlayer alternative to seekToNext()
            exoPlayer.prepare()             // Ensure the player prepares the new video source
            exoPlayer.play()                // Force playback to begin immediately
        } else {
            Log.e("Player", "No next video item available in the playlist.")
        }
    }
    fun playPervious(){
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem() // Modern ExoPlayer alternative to seekToPrevious()
            exoPlayer.prepare()
            exoPlayer.play()
        } else {
            // Optional: Restart the current video if there is no previous item
            exoPlayer.seekTo(0)
            exoPlayer.play()
        }
    }

    fun mute(enable: Boolean) {
        _mute.value = enable
        if (enable) {
            // mute but remember last non-zero volume
            // do not change system stream here if you already changed in VolumeReading
            exoPlayer.volume = 0f
            _valume.value = 0f
            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)

        } else {
            val restore = if (_valume.value > 0f) _valume.value else 1f
            exoPlayer.volume = restore
            _valume.value = restore
            // optionally restore system stream volume too:
            val max = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, (restore * max).toInt(), 0)
        }
    }

    fun pauseVideo(): Long {
        exoPlayer.pause()
        return exoPlayer.currentPosition
    }

    fun addNote(note: String, id: Long) {
        Log.e("check", "addNote: $note, ID: $id")
        _listOfNotes.update { currentState ->
            currentState.toMutableMap().apply {
                val notes = getOrDefault(id, emptyList()) + note
                put(id, notes)
            }
        }
    }
    // in onCleared()
    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(volumeReceiver)
        } catch (_: Exception) {
        }
    }
}