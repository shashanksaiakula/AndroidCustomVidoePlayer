package com.example.video_player_lib.utils

import android.content.Context
import android.net.Uri
import java.io.File

object UriUtils {

    fun copyUriToFile(
        context: Context,
        uri: Uri
    ): String {

        val file = File(
            context.cacheDir,
            "temp_video.mp4"
        )

        context.contentResolver
            .openInputStream(uri)
            ?.use { input ->

                file.outputStream()
                    .use { output ->

                        input.copyTo(output)
                    }
            }

        return file.absolutePath
    }
}