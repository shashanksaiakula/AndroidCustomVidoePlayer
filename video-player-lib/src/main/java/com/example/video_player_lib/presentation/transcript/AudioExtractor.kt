package com.example.video_player_lib.presentation.transcript

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class AudioExtractor(private val context: Context) {

    /**
     * Extracts audio from video file and converts to 16kHz mono WAV
     * Returns the path to the extracted audio file
     */
    fun extractAudioFromVideo(videoUri: Uri): File? {
        val outputFile = File(context.cacheDir, "extracted_audio.wav")

        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        try {
            extractor.setDataSource(context, videoUri, null)

            // Find audio track
            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1) {
                return null // No audio track found
            }

            extractor.selectTrack(audioTrackIndex)

            // Get audio properties
            val sampleRate = audioFormat!!.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            // Decode and resample to 16kHz mono
            val pcmData = decodeAudioTrack(extractor, audioFormat, sampleRate, channelCount)

            // Write WAV file
            writeWavFile(outputFile, pcmData, 16000, 1)

            return outputFile

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            codec?.release()
            extractor.release()
        }
    }

    private fun decodeAudioTrack(
        extractor: MediaExtractor,
        format: MediaFormat,
        originalSampleRate: Int,
        originalChannels: Int
    ): ByteArray {

        val mime = format.getString(MediaFormat.KEY_MIME)!!
        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(format, null, null, 0)
        decoder.start()

        val bufferInfo = MediaCodec.BufferInfo()
        val pcmDataList = mutableListOf<ByteArray>()
        var totalSize = 0

        var inputDone = false
        var outputDone = false

        while (!outputDone) {
            // Feed input
            if (!inputDone) {
                val inputBufferId = decoder.dequeueInputBuffer(10000)
                if (inputBufferId >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferId)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)

                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        val presentationTime = extractor.sampleTime
                        decoder.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTime, 0)
                        extractor.advance()
                    }
                }
            }

            // Get output
            val outputBufferId = decoder.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferId >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputBufferId)!!

                if (bufferInfo.size > 0) {
                    val chunk = ByteArray(bufferInfo.size)
                    outputBuffer.get(chunk)
                    outputBuffer.clear()

                    pcmDataList.add(chunk)
                    totalSize += chunk.size
                }

                decoder.releaseOutputBuffer(outputBufferId, false)

                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputDone = true
                }
            }
        }

        decoder.stop()
        decoder.release()

        // Combine all chunks
        val pcmData = ByteArray(totalSize)
        var offset = 0
        for (chunk in pcmDataList) {
            System.arraycopy(chunk, 0, pcmData, offset, chunk.size)
            offset += chunk.size
        }

        // Resample to 16kHz mono
        return resampleAndConvertToMono(pcmData, originalSampleRate, originalChannels, 16000)
    }

    private fun resampleAndConvertToMono(
        input: ByteArray,
        inputSampleRate: Int,
        inputChannels: Int,
        outputSampleRate: Int
    ): ByteArray {

        // Convert byte array to short array (16-bit PCM)
        val inputShorts = ShortArray(input.size / 2)
        ByteBuffer.wrap(input).asShortBuffer().get(inputShorts)

        // Convert to mono if stereo
        val monoShorts = if (inputChannels == 2) {
            ShortArray(inputShorts.size / 2) { i ->
                ((inputShorts[i * 2].toInt() + inputShorts[i * 2 + 1].toInt()) / 2).toShort()
            }
        } else {
            inputShorts
        }

        // Resample
        val ratio = inputSampleRate.toDouble() / outputSampleRate.toDouble()
        val outputLength = (monoShorts.size / ratio).toInt()
        val resampledShorts = ShortArray(outputLength) { i ->
            val srcIndex = (i * ratio).toInt()
            if (srcIndex < monoShorts.size) monoShorts[srcIndex] else 0
        }

        // Convert back to byte array
        val output = ByteArray(resampledShorts.size * 2)
        ByteBuffer.wrap(output).asShortBuffer().put(resampledShorts)

        return output
    }

    private fun writeWavFile(file: File, pcmData: ByteArray, sampleRate: Int, channels: Int) {
        FileOutputStream(file).use { fos ->
            val totalDataLen = pcmData.size + 36
            val byteRate = sampleRate * channels * 2 // 16-bit = 2 bytes

            // WAV header
            fos.write("RIFF".toByteArray())
            fos.write(intToByteArray(totalDataLen), 0, 4)
            fos.write("WAVE".toByteArray())
            fos.write("fmt ".toByteArray())
            fos.write(intToByteArray(16), 0, 4) // Sub-chunk size
            fos.write(shortToByteArray(1), 0, 2) // Audio format (1 = PCM)
            fos.write(shortToByteArray(channels.toShort()), 0, 2)
            fos.write(intToByteArray(sampleRate), 0, 4)
            fos.write(intToByteArray(byteRate), 0, 4)
            fos.write(shortToByteArray((channels * 2).toShort()), 0, 2) // Block align
            fos.write(shortToByteArray(16), 0, 2) // Bits per sample
            fos.write("data".toByteArray())
            fos.write(intToByteArray(pcmData.size), 0, 4)
            fos.write(pcmData)
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            ((value.toInt() shr 8) and 0xff).toByte()
        )
    }
}