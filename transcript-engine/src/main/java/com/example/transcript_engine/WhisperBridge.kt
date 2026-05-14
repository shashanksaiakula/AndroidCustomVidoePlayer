package com.example.transcript_engine

class WhisperBridge {

    external fun loadModel(
        modelPath: String
    ): Boolean

    external fun transcribeAudio(
        audioPath: String
    ): String

    external fun releaseModel()

    companion object {

        init {
            System.loadLibrary("whisper")
        }
    }
}