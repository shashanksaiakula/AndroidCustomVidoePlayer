package com.example.video_player_lib.presentation.transcript

import android.app.Application
import android.content.res.AssetManager
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileInputStream

data class TranscriptState(
    val isProcessing: Boolean = false,
    val isModelLoaded: Boolean = false,
    val statusMessage: String = "Initializing...",
    val transcript: String = "",
    val progress: Float = 0f,
    val selectedVideoPath: String = ""
)

class VideoTranscriptViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = mutableStateOf(TranscriptState())
    val state: State<TranscriptState> = _state

    private var model: Model? = null
    private val audioExtractor = AudioExtractor(application)

    init {
        initializeModel()
    }

    private fun initializeModel() {
        _state.value = _state.value.copy(statusMessage = "Loading model...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val modelDir = File(getApplication<Application>().cacheDir, "vosk-model-small-en-us-0.15")

                if (!modelDir.exists()) {
                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(statusMessage = "Extracting model...")
                    }
                    copyAssets("vosk-model-small-en-us-0.15", modelDir)
                }

                model = Model(modelDir.absolutePath)

                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        isModelLoaded = true,
                        statusMessage = "Ready - Select a video"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        statusMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun copyAssets(assetPath: String, targetDir: File) {
        targetDir.mkdirs()

        val assetManager: AssetManager = getApplication<Application>().assets
        val files = assetManager.list(assetPath) ?: return

        for (filename in files) {
            val assetFile = "$assetPath/$filename"
            val outFile = File(targetDir, filename)

            if (assetManager.list(assetFile)?.isEmpty() == false) {
                copyAssets(assetFile, outFile)
            } else {
                assetManager.open(assetFile).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    fun processVideo(videoUri: Uri) {
        if (model == null) {
            _state.value = _state.value.copy(statusMessage = "Model not loaded")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        isProcessing = true,
                        statusMessage = "Extracting audio from video...",
                        progress = 0.1f,
                        transcript = ""
                    )
                }

                // Extract audio
                val audioFile = audioExtractor.extractAudioFromVideo(videoUri)

                if (audioFile == null) {
                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(
                            isProcessing = false,
                            statusMessage = "Error: No audio track found in video"
                        )
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        statusMessage = "Transcribing audio...",
                        progress = 0.3f
                    )
                }

                // Transcribe
                val transcript = transcribeAudio(audioFile)

                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        statusMessage = "Complete!",
                        transcript = transcript,
                        progress = 1f
                    )
                }

                // Cleanup
                audioFile.delete()

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        statusMessage = "Error: ${e.message}",
                        progress = 0f
                    )
                }
            }
        }
    }

    private suspend fun transcribeAudio(audioFile: File): String {
        val recognizer = Recognizer(model, 16000.0f)
        recognizer.setWords(true)

        val transcript = StringBuilder()
        val allWords = mutableListOf<WordTiming>()

        FileInputStream(audioFile).use { fis ->
            // Skip WAV header (44 bytes)
            fis.skip(44)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            val totalBytes = audioFile.length() - 44
            var processedBytes = 0L

            while (fis.read(buffer).also { bytesRead = it } > 0) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    val result = recognizer.result
                    val jsonResult = JSONObject(result)

                    // Get word-level results
                    val resultArray = jsonResult.optJSONArray("result")
                    if (resultArray != null && resultArray.length() > 0) {
                        for (i in 0 until resultArray.length()) {
                            val wordObj = resultArray.getJSONObject(i)
                            val word = wordObj.getString("word")
                            val start = wordObj.getDouble("start")
                            val end = wordObj.getDouble("end")

                            allWords.add(WordTiming(word, start, end))
                        }
                    }
                }

                processedBytes += bytesRead
                val progress = 0.3f + (processedBytes.toFloat() / totalBytes.toFloat()) * 0.5f

                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(progress = progress)
                }
            }

            // Get final result
            val finalResult = recognizer.finalResult
            val jsonFinal = JSONObject(finalResult)
            val resultArray = jsonFinal.optJSONArray("result")

            if (resultArray != null && resultArray.length() > 0) {
                for (i in 0 until resultArray.length()) {
                    val wordObj = resultArray.getJSONObject(i)
                    val word = wordObj.getString("word")
                    val start = wordObj.getDouble("start")
                    val end = wordObj.getDouble("end")

                    allWords.add(WordTiming(word, start, end))
                }
            }
        }

        recognizer.close()

        // Group words into sentences
        withContext(Dispatchers.Main) {
            _state.value = _state.value.copy(
                statusMessage = "Formatting sentences...",
                progress = 0.9f
            )
        }

        val sentences = groupWordsIntoSentences(allWords)

        // Format transcript
        for (sentence in sentences) {
            val startTime = formatTimestamp(sentence.startTime)
            val endTime = formatTimestamp(sentence.endTime)
            transcript.append("$startTime - $endTime\n${sentence.text}\n\n")
        }

        return transcript.toString()
    }

    // Add these data classes at the top of the file (outside the class)
    data class WordTiming(
        val word: String,
        val startTime: Double,
        val endTime: Double
    )

    data class Sentence(
        val text: String,
        val startTime: Double,
        val endTime: Double
    )

    // Add this function inside the ViewModel class
    private fun groupWordsIntoSentences(words: List<WordTiming>): List<Sentence> {
        if (words.isEmpty()) return emptyList()

        val sentences = mutableListOf<Sentence>()
        var currentSentence = mutableListOf<WordTiming>()

        for (word in words) {
            currentSentence.add(word)

            // Check if word ends with sentence-ending punctuation
            // or if there's a long pause (more than 1 second to next word)
            val isEndOfSentence = word.word.matches(Regex(".*[.!?]$"))
            val hasLongPause = words.indexOf(word) < words.size - 1 &&
                    words[words.indexOf(word) + 1].startTime - word.endTime > 1.0

            // Also create sentence every 10-15 words to avoid very long sentences
            val isTooLong = currentSentence.size >= 15

            if (isEndOfSentence || hasLongPause || isTooLong) {
                if (currentSentence.isNotEmpty()) {
                    val sentenceText = currentSentence.joinToString(" ") { it.word }
                    val startTime = currentSentence.first().startTime
                    val endTime = currentSentence.last().endTime

                    sentences.add(Sentence(sentenceText, startTime, endTime))
                    currentSentence = mutableListOf()
                }
            }
        }

        // Add remaining words as final sentence
        if (currentSentence.isNotEmpty()) {
            val sentenceText = currentSentence.joinToString(" ") { it.word }
            val startTime = currentSentence.first().startTime
            val endTime = currentSentence.last().endTime

            sentences.add(Sentence(sentenceText, startTime, endTime))
        }

        return sentences
    }

    private fun formatTimestamp(seconds: Double): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        val secs = (seconds % 60).toInt()

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    fun clearTranscript() {
        _state.value = _state.value.copy(
            transcript = "",
            selectedVideoPath = "",
            progress = 0f
        )
    }

    override fun onCleared() {
        super.onCleared()
        model?.close()
    }
}