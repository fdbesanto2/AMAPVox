#ifndef RIVLIBLIBRARY
#define RIVLIBLIBRARY

#ifdef WIN64
    #include "win64/jni.h"
    #include "win64/jni_md.h"
#else
    #include "linux64/jni.h"
    #include "linux64/jni_md.h"
#endif

#include "fastserializer.h"
#include "serializer.h"
#include "mypointcloud.h"
#include "puechabonfilter.h"
#include "shotfilter.h"

#include <riegl/scanlib.hpp>
#include "riegl/pointcloud.hpp"

#include <stdlib.h>
#include <stdio.h>
#include <cstring>
#include <iostream>
#include <fstream>

using namespace scanlib;


#ifndef _Included_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction
#define _Included_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction
#ifdef __cplusplus

typedef struct rpx_extraction {

    std::tr1::shared_ptr<scanlib::basic_rconnection> connexion;
    FastSerializer* serializer;
    decoder_rxpmarker* decoder;
    mypointcloud* pointcloud;
    jmethodID *shotConstructor;

} rpx_extraction_struct;

extern "C" {
#endif

    JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_afficherBonjour
        (JNIEnv *, jobject);

    JNIEXPORT jlong JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_instantiate
        (JNIEnv *, jobject);

    JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_delete
        (JNIEnv *, jobject, jlong pointer);

    JNIEXPORT int JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_open
    (JNIEnv *, jobject, jlong pointer, jstring file_name);

    JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_closeConnexion
    (JNIEnv *, jobject, jlong pointer);

    JNIEXPORT jobject JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_getNextShot
    (JNIEnv *, jobject, jlong pointer);

    JNIEXPORT jboolean JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_hasShot
    (JNIEnv *, jobject, jlong pointer);

#ifdef __cplusplus
}
#endif
#endif

#endif // RIVLIBLIBRARY

