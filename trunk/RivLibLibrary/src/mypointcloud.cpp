/*
 * myclass.cpp
 *
 *  Created on: 15 mars 2013
 *      Author: theveny
 */

#include "mypointcloud.h"

using namespace mpc;

mypointcloud::mypointcloud(serializer& ser, JNIEnv *env, ShotType shotType) : pointcloud(false), serialize(ser) {

    this->env = env;
    this->shots = new stack<jobject*>();
    this->shotType = shotType;
}

mypointcloud::~mypointcloud() {
    delete shots;
}

void mypointcloud::on_echo_transformed(echo_type echo){
	pointcloud::on_echo_transformed(echo);
}

void mypointcloud::on_shot() {
	pointcloud::on_shot();
}

void mypointcloud::on_shot_end() {

	pointcloud::on_shot_end();

    int nbEchos = 0;
    jdouble tmp[7];
    jfloat tmp2[7];

    for(pointcloud::target_count_type i = 0; i < target_count; ++i) {

            target t = targets[i];
            if(i < 7){
                tmp[i] = t.echo_range;

                if(shotType == WITH_REFLECTANCE){
                    tmp2[i] = t.reflectance;
                }

                nbEchos++;
            }
    }

    jdoubleArray echos = env->NewDoubleArray(nbEchos);
    env->SetDoubleArrayRegion(echos, 0, nbEchos, tmp);

    jfloatArray reflectances = NULL;

    if(shotType != SIMPLE){
        reflectances = env->NewFloatArray(nbEchos);
        env->SetFloatArrayRegion(reflectances, 0, nbEchos, tmp2);
    }

    jobject* shotTemp = new jobject();
    jclass shotClass = env->FindClass("fr/amap/amapvox/io/tls/rxp/Shot");

    if(shotType == SIMPLE){

        jmethodID shotConstructor = env->GetMethodID(shotClass, "<init>", "(IDDDDDD[D)V");
        *shotTemp = env->NewObject(shotClass, shotConstructor, (jint)nbEchos,
                                   (jdouble)beam_origin[0], (jdouble)beam_origin[1], (jdouble)beam_origin[2],
                                    (jdouble)beam_direction[0], (jdouble)beam_direction[1], (jdouble)beam_direction[2], echos);
    }else{
        jmethodID shotConstructor = env->GetMethodID(shotClass, "<init>", "(IDDDDDD[D[F)V");
        *shotTemp = env->NewObject(shotClass, shotConstructor, (jint)nbEchos,
                                   (jdouble)beam_origin[0], (jdouble)beam_origin[1], (jdouble)beam_origin[2],
                                    (jdouble)beam_direction[0], (jdouble)beam_direction[1], (jdouble)beam_direction[2], echos, reflectances);
    }




    if(shotType != SIMPLE){
        env->DeleteLocalRef(reflectances);
    }

    shots->push(shotTemp);

}
