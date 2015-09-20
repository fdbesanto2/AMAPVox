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


namespace mpc
{

    enum ShotType { SIMPLE = 1, WITH_REFLECTANCE = 2};

    class mypointcloud: public scanlib::pointcloud
    {

    public:

        mypointcloud(serializer& ser, JNIEnv *env, ShotType shotType);
        virtual ~mypointcloud();
        stack<jobject*> *shots;

    protected :
        void on_echo_transformed(echo_type echo);
        void on_shot();
        void on_shot_end();

    private :
        JNIEnv *env;
        serializer& serialize;
        ShotType shotType;

    };
}



#endif /* MYPINTCLOUD_H_ */
