/*
 * myclass.h
 *
 *  Created on: 15 mars 2013
 *      Author: theveny
 */

#ifndef MYPOINTCLOUD_H_
#define MYPOINTCLOUD_H_

#include "serializer.h"
#include "jni.h"
#include "puechabonfilter.h"
#include <stack>
#include "riegl/pointcloud.hpp"
#include <iostream>

using namespace scanlib;
using namespace std;

class mypointcloud: public scanlib::pointcloud {

public:
    mypointcloud(serializer& ser, JNIEnv *env, jmethodID* shotConstructor );
    virtual ~mypointcloud();
    stack<jobject*> *shots;
    jmethodID *shotConstructor;
protected :
	void on_echo_transformed(echo_type echo);
	void on_shot();
	void on_shot_end();

private :
    JNIEnv *env;
    serializer& serialize;
};

#endif /* MYPINTCLOUD_H_ */
