#include "rivliblibrary.h"

jclass shotClass;
jmethodID shotConstructor;
puechabonfilter filter;
std::tr1::shared_ptr<basic_rconnection> cnx ;

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

JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_afficherBonjour(JNIEnv *env, jobject){

    printf(" Bonjour\n ");
    return;
}

JNIEXPORT jlong JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_instantiate(JNIEnv *env, jobject){

    rpx_extraction_struct* extraction_dll = new rpx_extraction_struct;
    memset(extraction_dll, 0, sizeof(rpx_extraction_struct));

    extraction_dll->connexion  = new std::tr1::shared_ptr<basic_rconnection>();

    jclass c = env->FindClass("fr/ird/voxelidar/voxelisation/extraction/tls/Shot");
    if (c == NULL){
        return -1;
    }

    shotClass = (jclass)env->NewGlobalRef(c);

    shotConstructor = env->GetMethodID(shotClass, "<init>", "(IDDDDDD[D)V");
    if (shotConstructor == NULL){
        return -1;
    }

    return (jlong)extraction_dll;
}

JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_delete(JNIEnv *env, jobject, jlong pointer){

    if (shotClass != NULL) {
        env->DeleteGlobalRef(shotClass);
    }

    long pointerAddress = pointer;
    rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

    delete extraction_dll->connexion;
    delete extraction_dll->decoder;
    delete extraction_dll->pointcloud;
    delete extraction_dll->serializer;

    delete extraction_dll;
}

JNIEXPORT int JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_open(JNIEnv *env, jobject, jlong pointer, jstring file_name){

    long pointerAddress = pointer;
    rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

    const char *str1 = JNU_GetStringNativeChars(env, file_name);

    cnx = basic_rconnection::create(str1);
    extraction_dll->connexion = &cnx;

    (*extraction_dll->connexion)->open();

    std::cout << "successful connection" << std::endl;

    extraction_dll->decoder = new decoder_rxpmarker(*extraction_dll->connexion);


    extraction_dll->serializer = new FastSerializer(std::cout, filter);
    extraction_dll->pointcloud = new mypointcloud(*extraction_dll->serializer);


    return 0;
}

JNIEXPORT void JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_closeConnexion(JNIEnv *, jobject, jlong pointer){

    std::cout << "test1" << std::endl;
    long pointerAddress = pointer;

    std::cout << "test2" << std::endl;
    rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

    std::cout << pointerAddress << std::endl;
    std::cout << (*extraction_dll->connexion)->id << std::endl;

    (*extraction_dll->connexion)->close();

    std::cout << "test4" << std::endl;

}

JNIEXPORT jboolean JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_hasShot(JNIEnv *, jobject, jlong pointer){

    long pointerAddress = pointer;
    rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

    bool hasShot = (*extraction_dll->connexion)->eoi();
    //bool hasShot = extraction_dll->decoder->eoi();

    return (jboolean)hasShot;
}

JNIEXPORT jobject JNICALL Java_fr_ird_voxelidar_voxelisation_extraction_tls_RxpExtraction_getNextShot(JNIEnv *env, jobject, jlong pointer){

    long pointerAddress = pointer;
    rpx_extraction_struct *extraction_dll  = (rpx_extraction_struct*)pointerAddress;

    buffer buf;

    extraction_dll->decoder->get(buf);
    extraction_dll->pointcloud->dispatch(buf.begin(), buf.end());

    if(!extraction_dll->decoder->eoi()){

        int nbShots = 0;

        jdouble tmp[7];

        for(pointcloud::target_count_type i = 0; i < extraction_dll->pointcloud->target_count; ++i) {

                target t = extraction_dll->pointcloud->targets[i];
                tmp[i] = t.echo_range;
                nbShots++;
        }

        jdoubleArray echos = env->NewDoubleArray(nbShots);
        env->SetDoubleArrayRegion(echos, 0, nbShots, tmp);

        jobject shot = env->NewObject(shotClass, shotConstructor);

        env->CallVoidMethod(shot, shotConstructor, nbShots,
                            extraction_dll->pointcloud->beam_origin[0], extraction_dll->pointcloud->beam_origin[1], extraction_dll->pointcloud->beam_origin[2],
                             extraction_dll->pointcloud->beam_direction[0], extraction_dll->pointcloud->beam_direction[1], extraction_dll->pointcloud->beam_direction[2], echos);

        env->DeleteLocalRef(shot);
        env->DeleteLocalRef(echos);
    }


    return NULL;
}
