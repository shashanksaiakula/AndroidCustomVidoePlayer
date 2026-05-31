package com.example.video_player_lib.api.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transcript_engine.WhisperBridge
import com.example.video_player_lib.utils.AudioExtractor
import com.example.video_player_lib.utils.UriUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranscriptViewModel : ViewModel() {

    // Instantiate WhisperBridge inside the ViewModel
    private val bridge = WhisperBridge()

    // UI States exposed to Composable
    var uriData by mutableStateOf<Uri?>(null)
    private val _transcript= MutableStateFlow<Map<String, String>>(emptyMap())
    val transcript = _transcript.asStateFlow()
    var isLoading by mutableStateOf(false)
        private set
    var isModelLoaded by mutableStateOf(false)
        private set

    /**
     * Call this inside a LaunchedEffect(Unit) in your Composable
     */
    fun loadWhisperModel(context: Context, copyModelToStorage: (Context) -> String) {
        if (isModelLoaded) return // Prevent reloading if already loaded

        val appContext = context.applicationContext
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val modelPath = copyModelToStorage(appContext)
                    val loaded = bridge.loadModel(modelPath)
                    isModelLoaded = loaded
                    Log.e("WHISPER_MODEL", "Model loaded status: $loaded")
                } catch (e: Exception) {
                    Log.e("WHISPER_MODEL_ERROR", e.stackTraceToString())
                }
            }
        }
    }

    /**
     * Core heavy transcription pipeline
     */
    fun processVideo(context: Context, uri: Uri?) {
        if (uri == null) return
        if (isLoading) return // Prevent concurrent duplicate runs

        val appContext = context.applicationContext

        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Copy original file to local app storage
                val realVideoPath = withContext(Dispatchers.IO) {
                    UriUtils.copyUriToFile(appContext, uri)
                }
                Log.e("WHISPER", "VIDEO COPIED")

                // 2. Extract wav track from video on background thread
                val wavPath = withContext(Dispatchers.IO) {
                    AudioExtractor.extractAudio(appContext, realVideoPath)
                }

                if (wavPath == null) {
                    Log.e("WHISPER", "AUDIO EXTRACTION FAILED")
                    return@launch
                }

                // 3. Process transcription via JNI Bridge
                Log.e("WHISPER", "STARTING JNI")
                val result = withContext(Dispatchers.IO) {
                    bridge.transcribeAudio(wavPath)
                }

                _transcript.value = result
                Log.e("WHISPER_RESULT", _transcript.value.entries.toString())

            } catch (e: Exception) {
                Log.e("WHISPER_ERROR", e.stackTraceToString())
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Clears transcription values when switching states
     */
    fun updateUri(newUri: Uri?) {
        uriData = newUri
        _transcript.value = emptyMap<String, String>() // Reset text for the next video
    }

    /**
     * Replaces DisposableEffect: Triggered automatically when ViewModel is cleared/destroyed
     */
    override fun onCleared() {
        super.onCleared()
        try {
            bridge.releaseModel()
            Log.e("WHISPER_MODEL", "Model released successfully")
        } catch (e: Exception) {
            Log.e("WHISPER_MODEL_RELEASE_ERR", e.stackTraceToString())
        }
    }
}
