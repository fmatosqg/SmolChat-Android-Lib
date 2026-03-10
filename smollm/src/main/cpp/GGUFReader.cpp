#include "gguf.h"
#include <jni.h>
#include <string>

#include <sys/stat.h>
#include <unistd.h>
#include <android/log.h>

extern "C" JNIEXPORT jlong JNICALL
Java_io_shubham0204_smollm_GGUFReader_getGGUFContextNativeHandle(JNIEnv* env, jobject thiz, jstring modelPath) {
    jboolean         isCopy        = true;
    const char*      modelPathCStr = env->GetStringUTFChars(modelPath, &isCopy);
    gguf_init_params initParams    = { .no_alloc = true, .ctx = nullptr };
    gguf_context*    ggufContext   = gguf_init_from_file(modelPathCStr, initParams);
    env->ReleaseStringUTFChars(modelPath, modelPathCStr);
    return reinterpret_cast<jlong>(ggufContext);
}

extern "C" JNIEXPORT jlong JNICALL
Java_io_shubham0204_smollm_GGUFReader_getGGUFContextNativeHandleFromFd(JNIEnv* env, jobject thiz, jint fd) {
    char path[256];
    sprintf(path, "/proc/self/fd/%d", fd);

    __android_log_print(ANDROID_LOG_INFO, "GGuf-JNI", "Current PID: %d", getpid());

    unsigned char dump[128];
    off_t offset = 52; // Assuming the provided startOffset
    offset = 394288492 - 10;
    offset = 371720344 - 10;
    if (pread(fd, dump, 128, offset) == 128) {
        char hex_dump[128 * 3 + 1];
        char ascii_dump[128 + 1];
        for (int i = 0; i < 128; i++) {
            sprintf(hex_dump + (i * 3), "%02x ", dump[i]);
            ascii_dump[i] = isprint(dump[i]) ? dump[i] : '.';
        }
        hex_dump[128 * 3] = '\0';
        ascii_dump[128] = '\0';
        __android_log_print(ANDROID_LOG_INFO, "GGuf-JNI", "Bytes at offset %lld (hex): %s", (long long)offset, hex_dump);
        __android_log_print(ANDROID_LOG_INFO, "GGuf-JNI", "Bytes at offset %lld (ascii): %s", (long long)offset, ascii_dump);
    } else {
        __android_log_print(ANDROID_LOG_ERROR, "GGuf-JNI", "Failed to pread 128 bytes from fd %d at offset %lld", fd, (long long)offset);
    }

    jstring modelPath = env->NewStringUTF(path);
    jlong handle = Java_io_shubham0204_smollm_GGUFReader_getGGUFContextNativeHandle(env, thiz, modelPath);
    
    if (handle == 0) {
        __android_log_print(ANDROID_LOG_ERROR, "GGuf-JNI", "gguf_init_from_file returned NULL for %s", path);
    } else {
        __android_log_print(ANDROID_LOG_INFO, "GGuf-JNI", "gguf_init_from_file success for %s", path);
    }
    
    return handle;
}

#include "ggml-impl.h"

extern "C" JNIEXPORT jlong JNICALL
Java_io_shubham0204_smollm_GGUFReader_getGGUFContextNativeHandleFromBuffer(JNIEnv* env, jobject thiz, jobject buffer) {
    void*  data = env->GetDirectBufferAddress(buffer);
    jlong  size = env->GetDirectBufferCapacity(buffer);
    
    if (data == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "GGuf-JNI", "GetDirectBufferAddress returned NULL");
        return 0;
    }

    __android_log_print(ANDROID_LOG_INFO, "GGuf-JNI", "Loading GGUF from buffer: address=%p, size=%lld", data, (long long)size);

    FILE* file = fmemopen(data, (size_t)size, "rb");
    if (file == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "GGuf-JNI", "fmemopen failed: %s", strerror(errno));
        return 0;
    }

    gguf_init_params initParams  = { .no_alloc = true, .ctx = nullptr };
    gguf_context*    ggufContext = gguf_init_from_file_impl(file, initParams);
    
    fclose(file);

    if (ggufContext == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "GGuf-JNI", "gguf_init_from_file_impl returned NULL");
    } else {
        __android_log_print(ANDROID_LOG_INFO, "GGuf-JNI", "gguf_init_from_file_impl success");
    }
    
    return reinterpret_cast<jlong>(ggufContext);
}

extern "C" JNIEXPORT jlong JNICALL
Java_io_shubham0204_smollm_GGUFReader_getContextSize(JNIEnv* env, jobject thiz, jlong nativeHandle) {
    gguf_context* ggufContext       = reinterpret_cast<gguf_context*>(nativeHandle);
    int64_t       architectureKeyId = gguf_find_key(ggufContext, "general.architecture");
    if (architectureKeyId == -1)
        return -1;
    std::string architecture       = gguf_get_val_str(ggufContext, architectureKeyId);
    std::string contextLengthKey   = architecture + ".context_length";
    int64_t     contextLengthKeyId = gguf_find_key(ggufContext, contextLengthKey.c_str());
    if (contextLengthKeyId == -1)
        return -1;
    uint32_t contextLength = gguf_get_val_u32(ggufContext, contextLengthKeyId);
    return contextLength;
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_shubham0204_smollm_GGUFReader_getChatTemplate(JNIEnv* env, jobject thiz, jlong nativeHandle) {
    gguf_context* ggufContext       = reinterpret_cast<gguf_context*>(nativeHandle);
    int64_t       chatTemplateKeyId = gguf_find_key(ggufContext, "tokenizer.chat_template");
    std::string   chatTemplate;
    if (chatTemplateKeyId == -1) {
        chatTemplate = "";
    } else {
        chatTemplate = gguf_get_val_str(ggufContext, chatTemplateKeyId);
    }
    return env->NewStringUTF(chatTemplate.c_str());
}
