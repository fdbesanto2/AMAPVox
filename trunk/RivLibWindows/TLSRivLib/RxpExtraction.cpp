#include <jni.h> 
#include <stdio.h> 
#include "RxpExtraction.h" 

JNIEXPORT void JNICALL
Java_fr_ird_voxelidar_voxelisation_extraction_RxpExtraction_afficherBonjour(JNIEnv *env, jobject obj)
{
	printf(" Bonjour\n ");
	return;
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

		shared_ptr<basic_rconnection> conx;

		const char *str1 = env->GetStringUTFChars(fileName, 0);

		conx = basic_rconnection::create(str1);

		conx->open();
		cout << "successful connection" << endl;

		decoder_rxpmarker dec(conx);
		puechabonfilter filter(Params::NOFILTER);

		serializer *s = new Fast_serializer(env, method, addShotMethod, constructor, shotClass, shots, cout, filter);

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