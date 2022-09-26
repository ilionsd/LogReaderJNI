//
// Created by ilion on 22-Sep-22.
//
#include <android/log.h>
#include <cstring>
#include <jni.h>
#include <cassert>

#include "LogReader.h"

// Android log function wrappers
static const char* kTAG = "logreader-jni";
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))

// processing callback to handler class
typedef struct LogReader_context {
    JavaVM *javaVM;
    jclass logReaderClazz;
    jclass byteArrayClazz;
    jfieldID nativeLogReaderPtrFieldID;
    int done;
} LogReaderContext;
LogReaderContext g_ctx;

/*
 * processing one time initialization:
 *     Cache the javaVM into our context
 *     Find class ID for LogReaderImpl
 *     Find method ID for LogReaderImpl::foundLine(byte[] line)
 *     Find method ID for LogReaderImpl::onProcessingStarted()
 *     Find method ID for LogReaderImpl::onProcessingEnded()
 *     Make global reference since we are using them from a native thread
 * Note:
 *     All resources allocated here are never released by application
 *     we rely on system to free all global refs when it goes away;
 *     the pairing function JNI_OnUnload() never gets called at all.
 */
JNIEXPORT jint JNICALL
JNI_OnLoad(
    JavaVM* vm,
    void* reserved
) {
    JNIEnv* env;
    memset(&g_ctx, 0, sizeof(g_ctx));

    g_ctx.javaVM = vm;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR; // JNI version not supported.
    }

    jclass logReaderClazz = env->FindClass(
            "com/sburov/logparser/data/local/logreader/LogReaderImpl");
    g_ctx.logReaderClazz = (jclass) env->NewGlobalRef(logReaderClazz);

    jclass byteArrayClazz = env->FindClass(
            "[B");
    g_ctx.byteArrayClazz = (jclass) env->NewGlobalRef(byteArrayClazz);

    g_ctx.nativeLogReaderPtrFieldID = env->GetFieldID(g_ctx.logReaderClazz,
        "nativeLogReaderPtr", "J");

    g_ctx.done = 0;
    return  JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeCreateInstance(
    JNIEnv *env,
    jobject thiz
) {
    auto *pLogReader = new CLogReader();
    auto nativeLogReaderPtr = reinterpret_cast<jlong>(pLogReader);
    env->SetLongField(thiz, g_ctx.nativeLogReaderPtrFieldID, nativeLogReaderPtr);
}

extern "C" JNIEXPORT void JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeReleaseInstance(
    JNIEnv *env,
    jobject thiz
) {
    jlong nativeLogReaderPtr = env->GetLongField(thiz, g_ctx.nativeLogReaderPtrFieldID);
    auto* pLogReaderClass = reinterpret_cast<CLogReader *>(nativeLogReaderPtr);
    delete pLogReaderClass;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeIsRunning(
    JNIEnv *env,
    jobject thiz
) {
    jlong nativeLogReaderPtr = env->GetLongField(thiz, g_ctx.nativeLogReaderPtrFieldID);
    auto *pLogReader = reinterpret_cast<CLogReader *>(nativeLogReaderPtr);
    return (pLogReader->IsRunning())
        ? JNI_TRUE
        : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeSetFilter(
    JNIEnv* env,
    jobject thiz,
    jbyteArray jFilterArray,
    jsize jLength
) {
    jlong nativeLogReaderPtr = env->GetLongField(thiz, g_ctx.nativeLogReaderPtrFieldID);
    auto *pLogReader = reinterpret_cast<CLogReader *>(nativeLogReaderPtr);
    bool isCopy;
    jbyte* jFilter = env->GetByteArrayElements(jFilterArray,
                                                               reinterpret_cast<jboolean *>(&isCopy));
    const size_t bufferSize = 1024 * 1024;
    assert((jLength < bufferSize));
    char filter[bufferSize] = {0};
    memcpy(filter, jFilter, static_cast<size_t>(jLength) * sizeof(jbyte));
    bool success = pLogReader->SetFilter(filter);
    env->ReleaseByteArrayElements(jFilterArray, jFilter, JNI_ABORT);
    return (success)
        ? JNI_TRUE
        : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeAddSourceBlock(
    JNIEnv* env,
    jobject thiz,
    jbyteArray jBlockArray,
    jsize jLength
) {
    jlong nativeLogReaderPtr = env->GetLongField(thiz, g_ctx.nativeLogReaderPtrFieldID);
    auto *pLogReader = reinterpret_cast<CLogReader *>(nativeLogReaderPtr);
    bool isCopy;
    jbyte* jBlock = env->GetByteArrayElements(jBlockArray,
                                                              reinterpret_cast<jboolean *>(&isCopy));
    bool success = pLogReader->AddSourceBlock(
            reinterpret_cast<char *>(jBlock),
            static_cast<size_t>(jLength));
    env->ReleaseByteArrayElements(jBlockArray, jBlock, JNI_ABORT);
    return (success)
           ? JNI_TRUE
           : JNI_FALSE;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeGetMatches(
        JNIEnv *env,
        jobject thiz
) {
    jlong nativeLogReaderPtr = env->GetLongField(thiz, g_ctx.nativeLogReaderPtrFieldID);
    auto *pLogReader = reinterpret_cast<CLogReader *>(nativeLogReaderPtr);
    auto matchingLines = pLogReader->GetMatches();
    if (matchingLines.empty()) {
        return NULL;
    }
    jobjectArray jMatchingLinesArray = env->NewObjectArray(static_cast<jsize>(matchingLines.size()),
                                                           g_ctx.byteArrayClazz, NULL);
    for (std::size_t k = 0; k < matchingLines.size(); k++) {
        size_t length = strlen(matchingLines[k].get());
        jbyteArray jLineArray = env->NewByteArray(static_cast<jsize>(length));
        env->SetByteArrayRegion(jLineArray,
                                0, static_cast<jsize>(length),
                                reinterpret_cast<const jbyte *>(matchingLines[k].get()));
        env->SetObjectArrayElement(jMatchingLinesArray, static_cast<jsize>(k), jLineArray);
    }
    return jMatchingLinesArray;
}