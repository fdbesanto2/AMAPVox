/*
 * myclass.cpp
 *
 *  Created on: 15 mars 2013
 *      Author: theveny
 */

#include "mypointcloud.h"

mypointcloud::mypointcloud(serializer& ser, JNIEnv *env, jmethodID* shotConstructor ) : pointcloud(false), serialize(ser) {

    this->env = env;
    this->shotConstructor = shotConstructor;
    this->shots = new stack<jobject*>();
}

mypointcloud::~mypointcloud() {
    //delete env;
    delete shotConstructor;
    delete shots;
    //delete shotClass;
    //delete shotConstructor;
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

    for(pointcloud::target_count_type i = 0; i < target_count; ++i) {

            target t = targets[i];
            tmp[i] = t.echo_range;
            nbEchos++;
    }

    jdoubleArray echos = env->NewDoubleArray(nbEchos);
    env->SetDoubleArrayRegion(echos, 0, nbEchos, tmp);

    jobject* shotTemp = new jobject();
    jclass shotClass = env->FindClass("fr/ird/voxelidar/voxelisation/extraction/Shot");


    *shotTemp = env->NewObject(shotClass, *shotConstructor, (jint)nbEchos,
                               (jdouble)beam_origin[0], (jdouble)beam_origin[1], (jdouble)beam_origin[2],
                                (jdouble)beam_direction[0], (jdouble)beam_direction[1], (jdouble)beam_direction[2], echos);

    shots->push(shotTemp);

}
