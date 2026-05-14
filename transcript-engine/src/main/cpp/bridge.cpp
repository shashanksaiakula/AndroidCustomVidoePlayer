#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <cstring>
#include <android/log.h>

#include "whisper.h"

static whisper_context * g_ctx = nullptr;

static std::vector<float> read_wav_file(
        const char * path
) {

    std::ifstream file(
            path,
            std::ios::binary
    );

    if (!file.is_open()) {

        return {};
    }

    char chunk_id[4];
    int chunk_size;

    // Skip RIFF header
    file.seekg(12);

    // Find DATA chunk dynamically
    while (file.read(chunk_id, 4)) {

        file.read(
                reinterpret_cast<char*>(&chunk_size),
                4
        );

        if (
                std::strncmp(
                        chunk_id,
                        "data",
                        4
                ) == 0
                ) {

            break;
        }

        file.seekg(
                chunk_size,
                std::ios::cur
        );
    }

    std::vector<int16_t> pcm(
            chunk_size / sizeof(int16_t)
    );

    file.read(
            reinterpret_cast<char*>(pcm.data()),
            chunk_size
    );

    std::vector<float> samples(
            pcm.size()
    );

    for (size_t i = 0; i < pcm.size(); ++i) {

        samples[i] =
                pcm[i] / 32768.0f;
    }

    return samples;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_transcript_1engine_WhisperBridge_loadModel(
        JNIEnv *env,
        jobject thiz,
        jstring modelPath
) {

    const char *model_path =
            env->GetStringUTFChars(
                    modelPath,
                    0
            );

    __android_log_print(
            ANDROID_LOG_ERROR,
            "WHISPER_NATIVE",
            "LOADING MODEL"
    );

    g_ctx =
            whisper_init_from_file(
                    model_path
            );

    env->ReleaseStringUTFChars(
            modelPath,
            model_path
    );

    if (!g_ctx) {

        __android_log_print(
                ANDROID_LOG_ERROR,
                "WHISPER_NATIVE",
                "MODEL LOAD FAILED"
        );

        return false;
    }

    __android_log_print(
            ANDROID_LOG_ERROR,
            "WHISPER_NATIVE",
            "MODEL LOADED"
    );

    return true;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_transcript_1engine_WhisperBridge_transcribeAudio(
        JNIEnv *env,
        jobject thiz,
        jstring audioPath
) {

    if (!g_ctx) {

        return env->NewStringUTF(
                "MODEL NOT LOADED"
        );
    }

    const char *audio_path =
            env->GetStringUTFChars(
                    audioPath,
                    0
            );

    __android_log_print(
            ANDROID_LOG_ERROR,
            "WHISPER_NATIVE",
            "READING WAV FILE"
    );

    auto samples =
            read_wav_file(audio_path);

    env->ReleaseStringUTFChars(
            audioPath,
            audio_path
    );

    if (samples.empty()) {

        __android_log_print(
                ANDROID_LOG_ERROR,
                "WHISPER_NATIVE",
                "EMPTY AUDIO"
        );

        return env->NewStringUTF(
                "EMPTY AUDIO"
        );
    }

    __android_log_print(
            ANDROID_LOG_ERROR,
            "WHISPER_NATIVE",
            "SAMPLES SIZE: %d",
            (int)samples.size()
    );

    whisper_full_params params =
            whisper_full_default_params(
                    WHISPER_SAMPLING_GREEDY
            );

    // PERFORMANCE
    params.n_threads = 8;

    // TRANSCRIPT QUALITY
    params.translate = false;
    params.no_context = false;

    params.single_segment = false;

    // LOGGING
    params.print_progress = false;
    params.print_special = false;
    params.print_realtime = false;
    params.print_timestamps = true;

    // NO LIMITS
    params.max_len = 0;
    params.max_tokens = 0;

    __android_log_print(
            ANDROID_LOG_ERROR,
            "WHISPER_NATIVE",
            "BEFORE whisper_full"
    );

    int result =
            whisper_full(
                    g_ctx,
                    params,
                    samples.data(),
                    samples.size()
            );

    __android_log_print(
            ANDROID_LOG_ERROR,
            "WHISPER_NATIVE",
            "AFTER whisper_full"
    );

    if (result != 0) {

        __android_log_print(
                ANDROID_LOG_ERROR,
                "WHISPER_NATIVE",
                "TRANSCRIPTION FAILED"
        );

        return env->NewStringUTF(
                "TRANSCRIPTION FAILED"
        );
    }

    int n_segments =
            whisper_full_n_segments(
                    g_ctx
            );

    __android_log_print(
            ANDROID_LOG_ERROR,
            "WHISPER_NATIVE",
            "SEGMENTS COUNT: %d",
            n_segments
    );

    std::string transcript;

    for (int i = 0; i < n_segments; ++i) {

        const char * text =
                whisper_full_get_segment_text(
                        g_ctx,
                        i
                );

        int64_t t0 =
                whisper_full_get_segment_t0(
                        g_ctx,
                        i
                );

        int64_t t1 =
                whisper_full_get_segment_t1(
                        g_ctx,
                        i
                );

        transcript += "[";

        transcript += std::to_string(
                t0 / 100
        );

        transcript += "s -> ";

        transcript += std::to_string(
                t1 / 100
        );

        transcript += "s] ";

        transcript += text;

        transcript += "\n";
    }

    __android_log_print(
            ANDROID_LOG_ERROR,
            "WHISPER_NATIVE",
            "TRANSCRIPTION FINISHED"
    );

    return env->NewStringUTF(
            transcript.c_str()
    );
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_transcript_1engine_WhisperBridge_releaseModel(
        JNIEnv *env,
        jobject thiz
) {

    if (g_ctx) {

        whisper_free(g_ctx);

        g_ctx = nullptr;

        __android_log_print(
                ANDROID_LOG_ERROR,
                "WHISPER_NATIVE",
                "MODEL RELEASED"
        );
    }
}

