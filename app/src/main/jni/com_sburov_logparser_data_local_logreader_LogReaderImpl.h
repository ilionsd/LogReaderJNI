//
// Created by ilion on 22-Sep-22.
//

#ifndef LOGPARSER_COM_SBUROV_LOGPARSER_DATA_LOCAL_LOGREADER_LOGREADERIMPL_H
#define LOGPARSER_COM_SBUROV_LOGPARSER_DATA_LOCAL_LOGREADER_LOGREADERIMPL_H

#include <jni.h>


JNIEXPORT void JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeCreateInstance
(JNIEnv *, jobject);

JNIEXPORT void JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeReleaseInstance
(JNIEnv *, jobject);

JNIEXPORT jboolean JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeSetFilter
(JNIEnv *, jobject, jbyteArray);

JNIEXPORT jboolean JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeAddSourceBlock
(JNIEnv *, jobject, jbyteArray, jint);

JNIEXPORT void JNICALL
Java_com_sburov_logparser_data_local_logreader_LogReaderImpl_nativeGetMatches
(JNIEnv *, jobject)

#endif //LOGPARSER_COM_SBUROV_LOGPARSER_DATA_LOCAL_LOGREADER_LOGREADERIMPL_H
