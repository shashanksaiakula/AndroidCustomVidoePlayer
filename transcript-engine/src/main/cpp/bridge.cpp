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

    // If a context already exists, free it first to avoid leaking memory or leaving threads running
    if (g_ctx) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
        __android_log_print(ANDROID_LOG_ERROR, "WHISPER_NATIVE", "Existing model context freed before loading new model");
    }

    g_ctx = whisper_init_from_file(model_path);

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
JNIEXPORT jobject JNICALL
Java_com_example_transcript_1engine_WhisperBridge_transcribeAudio(
        JNIEnv *env,
        jobject thiz,
        jstring audioPath
) {
    // 1. Initialize LinkedHashMap to preserve order
    jclass hashMapClass = env->FindClass("java/util/LinkedHashMap");
    jmethodID hashMapInit = env->GetMethodID(hashMapClass, "<init>", "()V");
    jobject hashMapObj = env->NewObject(hashMapClass, hashMapInit);
    jmethodID hashMapPut = env->GetMethodID(hashMapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    if (!g_ctx) {
        __android_log_print(ANDROID_LOG_ERROR, "WHISPER_NATIVE", "Context not initialized");
        return hashMapObj;
    }

    // 2. Get audio path and read samples
    const char *audio_path = env->GetStringUTFChars(audioPath, 0);
    __android_log_print(ANDROID_LOG_ERROR, "WHISPER_NATIVE", "READING WAV FILE: %s", audio_path);
    auto samples = read_wav_file(audio_path);
    env->ReleaseStringUTFChars(audioPath, audio_path);

    if (samples.empty()) {
        __android_log_print(ANDROID_LOG_ERROR, "WHISPER_NATIVE", "EMPTY AUDIO SAMPLES");
        return hashMapObj;
    }

    // 3. Configure Whisper parameters
    // Use conservative number of threads on mobile devices to reduce heat and battery usage.
    // You can expose this as a setting if you want to tune per-device.
    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.n_threads = 2; // lower default to reduce CPU load / heat
    params.translate = false;
    params.language  = "en"; // Or use "auto"
    params.print_timestamps = true;

    // 4. Run the actual inference
    __android_log_print(ANDROID_LOG_ERROR, "WHISPER_NATIVE", "STARTING whisper_full");
    int result = whisper_full(g_ctx, params, samples.data(), samples.size());

    if (result != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "WHISPER_NATIVE", "TRANSCRIPTION FAILED: %d", result);
        return hashMapObj;
    }

    // 5. Extract segments into the LinkedHashMap
    int n_segments = whisper_full_n_segments(g_ctx);
    __android_log_print(ANDROID_LOG_ERROR, "WHISPER_NATIVE", "SEGMENTS FOUND: %d", n_segments);

    for (int i = 0; i < n_segments; ++i) {
        const char *text = whisper_full_get_segment_text(g_ctx, i);
        int64_t t0 = whisper_full_get_segment_t0(g_ctx, i);
        int64_t t1 = whisper_full_get_segment_t1(g_ctx, i);

        // Convert centiseconds to seconds for the key
        std::string timeKey = "[" + std::to_string(t0 / 100) + "s -> " + std::to_string(t1 / 100) + "s]";

        jstring jKey = env->NewStringUTF(timeKey.c_str());
        jstring jValue = env->NewStringUTF(text);

        env->CallObjectMethod(hashMapObj, hashMapPut, jKey, jValue);

        env->DeleteLocalRef(jKey);
        env->DeleteLocalRef(jValue);
    }

    return hashMapObj;
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

