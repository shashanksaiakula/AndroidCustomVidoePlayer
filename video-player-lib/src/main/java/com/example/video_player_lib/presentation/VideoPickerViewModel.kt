package com.example.video_player_lib.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.video_player_lib.domin.model.LocalVideo
import com.example.video_player_lib.domin.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPickerViewModel @Inject constructor(
    private val repository: VideoRepository,
    val exoPlayer: ExoPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoPickerUiState())
    val uiState = _uiState.asStateFlow()

    private val _isPressed = MutableStateFlow(false)
    val isPressed = _isPressed.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen = _isFullScreen.asStateFlow()

    private val _isRotation = MutableStateFlow(false)
    val isRotation = _isRotation.asStateFlow()
    private val _selectedVideoId = MutableStateFlow(0L)
    val selectedVideoId = _selectedVideoId.asStateFlow()
    private val _tabSelected = MutableStateFlow("notes")
    val tabSelected = _tabSelected.asStateFlow()
    private val _listOfNotes = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val listOfNotes = _listOfNotes.asStateFlow()
    private var timerJob: Job? = null

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

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = exoPlayer.duration
                }
            }
        })
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                _currentPosition.value = exoPlayer.currentPosition
                delay(500)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun loadVideos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val videos = repository.getLocalVideos()
                _uiState.update { it.copy(videos = videos, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onBackPress(isPressed: Boolean) {
        _isPressed.value = isPressed
    }

    fun preparePlayer(uri: Uri, mimeType: String?) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .apply { if (mimeType != null) setMimeType(mimeType) }
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        _currentPosition.value = position
    }

    fun skipForward() {
        seekTo(exoPlayer.currentPosition + 5000)
    }

    fun skipBackward() {
        seekTo(exoPlayer.currentPosition - 5000)
    }

    fun skipForward(position: Long) {
        seekTo(exoPlayer.currentPosition + position)
    }

    fun skipBackward(position: Long) {
        seekTo(exoPlayer.currentPosition - position)
    }

    fun onLongPress() {
        setPlaybackSpeed(2f)
    }
    fun onLongPress(palyBackFast : Float){
        setPlaybackSpeed(palyBackFast)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    fun onDoubleTap(isRight: Boolean) {
        if (isRight) {
            skipForward()
        } else {
            skipBackward()
        }
    }

    fun viewFullScreen(isFullScreen: Boolean) {
        _isFullScreen.value = isFullScreen
    }

    fun onRotation(isRotation: Boolean) {
        _isRotation.value = isRotation
    }

    fun onVideoSelected(id: Long) {
        _selectedVideoId.value = id
    }

    fun selectTab(tab: String) {
        _tabSelected.value = tab
    }

    fun deleteVideo(video: LocalVideo?) {
        _uiState.update { currentState ->
            currentState.copy(
                videos = currentState.videos.filter { it.id != video?.id }
            )
        }
    }

    fun renameVideo(video: LocalVideo?, newName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                videos = currentState.videos.map {
                    if (it.id == video?.id) it.copy(name = newName) else it
                }
            )
        }
    }

    fun pauseVideo(): Long {
        exoPlayer.pause()
        return exoPlayer.currentPosition
    }

    fun resumeVideo() {
        exoPlayer.play()
    }

    fun addNote(note: String, id: Long) {
        _listOfNotes.update { currentState ->
            currentState.toMutableMap().apply {
                val notes = getOrDefault(id, emptyList()) + note
                put(id, notes)
            }
        }
    }

}

data class VideoPickerUiState(
    val videos: List<LocalVideo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
