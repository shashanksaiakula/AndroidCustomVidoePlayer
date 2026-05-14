package com.example.transcript_engine

import android.content.Context
import java.io.File

fun copyModelToStorage(
    context: Context
): String {

    val file = File(
        context.filesDir,
        "ggml-tiny.en.bin"
    )

    if (file.exists()) {
        file.delete()
    }

    context.assets.open(
        "models/ggml-tiny.en.bin"
    ).use { input ->

        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return file.absolutePath
}