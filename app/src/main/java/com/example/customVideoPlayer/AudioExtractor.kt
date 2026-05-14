package com.example.customVideoPlayer

import android.content.Context
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

object AudioExtractor {

    suspend fun extractAudio(
        context: Context,
        videoPath: String
    ): String? {

        return suspendCancellableCoroutine { continuation ->

            val outputFile = File(
                context.cacheDir,
                "temp_audio.wav"
            )

            if (outputFile.exists()) {
                outputFile.delete()
            }

            val command =
                "-y -i \"$videoPath\" -vn -acodec pcm_s16le -ar 16000 -ac 1 \"${outputFile.absolutePath}\""

            FFmpegKit.executeAsync(command) { session ->

                val returnCode = session.returnCode

                if (ReturnCode.isSuccess(returnCode)) {

                    Log.e(
                        "FFMPEG_SUCCESS",
                        outputFile.absolutePath
                    )

                    continuation.resume(
                        outputFile.absolutePath
                    )

                } else {

                    Log.e(
                        "FFMPEG_ERROR",
                        session.allLogsAsString
                    )

                    continuation.resume(null)
                }
            }
        }
    }
}