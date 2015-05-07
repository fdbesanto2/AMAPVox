#include "rivliblibrary.h"



//morceau de code trouvé sur Internet qui permet de convertir les jstring contenant des accents
char* JNU_GetStringNativeChars(JNIEnv* env, jstring jstr) {

    jclass Class_java_lang_String = env->FindClass("java/lang/String");
    jmethodID MID_String_getBytes = env->GetMethodID(Class_java_lang_String, "getBytes", "()[B");

    jbyteArray bytes = 0;

    char* result = 0;

    if (env->EnsureLocalCapacity(2) < 0) {
        return 0;  // out of memory error
    }

    bytes = (jbyteArray) env->CallObjectMethod(jstr, MID_String_getBytes);

    jboolean exc = env->ExceptionCheck();

    if (!exc) {
        jint len = env->GetArrayLength(bytes);
        result = (char*)malloc(len+1);
        if (result == 0) {
            env->DeleteLocalRef(bytes);

            return 0;
        }
        env->GetByteArrayRegion(bytes, 0, len, (jbyte*)result);
        result[len] = 0; // NULL-terminate
    }
    else {
        printf("Exception occured...\n");
    }
    env->DeleteLocalRef(bytes);

    return result;
}

JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_afficherBonjour(JNIEnv , jobject){

    printf(" Bonjour\n ");
    return;
}

JNIEXPORT jlong JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_instantiate(JNIEnv *, jobject){

    rpx_extraction_struct* extraction_dll = new rpx_extraction_struct;
    memset(extraction_dll, 0, sizeof(rpx_extraction_struct));

    extraction_dll->connexion  = std::tr1::shared_ptr<basic_rconnection>();


    return (jlong)extraction_dll;
}

JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_delete(JNIEnv *, jobject, jlong pointer){



    long pointerAddress = (long)pointer;
    rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

    extraction_dll->connexion.reset();

    delete extraction_dll->decoder;

    delete extraction_dll->pointcloud;

    delete extraction_dll->serializer;

    delete extraction_dll;
}

JNIEXPORT int JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_open(JNIEnv *env, jobject, jlong pointer, jstring file_name){

    try
    {
        long pointerAddress = (long)pointer;
        rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

        const char *str1 = JNU_GetStringNativeChars(env, file_name);

        extraction_dll->connexion = basic_rconnection::create(str1);

        extraction_dll->connexion->open();

        extraction_dll->decoder = new decoder_rxpmarker(*extraction_dll->connexion);

        puechabonfilter filter;
        extraction_dll->serializer = new FastSerializer(std::cout, filter);
        extraction_dll->pointcloud = new mypointcloud(*extraction_dll->serializer, env);

    }catch ( const std::exception &  ){
        return -1;
    }


    return 0;
}

JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_closeConnexion(JNIEnv *, jobject, jlong pointer){

    try
    {
        long pointerAddress = (long)pointer;
        rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

        extraction_dll->connexion->close();

    }catch ( const std::exception & e ){
        std::cout << "Cannot close rxp file: " << e.what() << std::endl;
    }

}

JNIEXPORT jboolean JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_hasShot(JNIEnv *, jobject, jlong pointer){

    long pointerAddress = (long)pointer;
    rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

    return (jboolean)!extraction_dll->decoder->eoi();
}

JNIEXPORT jobject JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_getNextShot(JNIEnv *, jobject, jlong pointer){

    long pointerAddress = (long)pointer;
    rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

    if(extraction_dll->pointcloud->shots->empty()) {
        buffer buf;
        while(!extraction_dll->decoder->eoi() && extraction_dll->pointcloud->shots->empty()){
            extraction_dll->decoder->get(buf);
            extraction_dll->pointcloud->dispatch(buf.begin(), buf.end());
        }

    }


    if(!extraction_dll->pointcloud->shots->empty()){
        jobject shotTemp;
        jobject *shotPtr = extraction_dll->pointcloud->shots->top();
        shotTemp = *shotPtr;
        extraction_dll->pointcloud->shots->pop();
        delete shotPtr;
        return shotTemp;
    }


    return NULL;
}
