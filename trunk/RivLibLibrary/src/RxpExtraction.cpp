#include <stdio.h>
#include "RxpExtraction.h"

JNIEXPORT void JNICALL
Java_fr_ird_voxelidar_voxelisation_extraction_RxpExtraction_afficherBonjour(JNIEnv *env, jobject obj)
{
	printf(" Bonjour\n ");
	return;
}

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
        result = (char*)std::malloc(len+1);
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

JNIEXPORT bool JNICALL
Java_fr_ird_voxelidar_voxelisation_extraction_RxpExtraction_simpleExtraction(JNIEnv *env, jobject obj, jstring fileName, jobject shots)
{
	jclass shotsStruct = env->FindClass("fr/ird/voxelidar/voxelisation/extraction/Shots");

	if (shotsStruct == NULL){
		return false;
	}

	jclass shotClass = env->FindClass("fr/ird/voxelidar/voxelisation/extraction/Shot");

	if (shotClass == NULL){
		return false;
	}

	jmethodID constructor = env->GetMethodID(shotClass, "<init>", "(IDDDDDD[D)V");

	if (constructor == NULL){
		return false;
	}

	jmethodID method = env->GetMethodID(shotsStruct, "addShot", "(IDDDDDD[D)V");

	if (method == NULL){
		return false;
	}

	jmethodID addShotMethod = env->GetMethodID(shotsStruct, "addShot", "(Lfr/ird/voxelidar/voxelisation/extraction/Shot;)V");

	if (method == NULL){
		return false;
	}

	int i = 0;

	try{

		std::tr1::shared_ptr<basic_rconnection> conx;

        const char *str1 = JNU_GetStringNativeChars(env, fileName);

		conx = scanlib::basic_rconnection::create(str1);

		conx->open();
        std::cout << "successful connection" << endl;

		decoder_rxpmarker dec(conx);
        puechabonfilter filter;

        serializer *s = new Fast_serializer(env, method, addShotMethod, constructor, shotClass, shots, std::cout, filter);

		mypointcloud pc(*s);
		buffer buf;

		for (dec.get(buf); !dec.eoi(); dec.get(buf)) {
			pc.dispatch(buf.begin(), buf.end());
		}

		conx->close();

		delete s;


	}catch (int e){
		return false;
	}

	env->DeleteLocalRef(shotsStruct);
	env->DeleteLocalRef(shots);
	env->DeleteLocalRef(shotClass);

	return true;

}
