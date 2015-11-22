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

    enum ShotType {REFLECTANCE = 2, DEVIATION = 3, AMPLITUDE = 4};

    class mypointcloud: public scanlib::pointcloud
    {

    public:

        mypointcloud(serializer& ser, JNIEnv *env);
        virtual ~mypointcloud();
        stack<jobject*> *shots;
        void setExportReflectance(bool exportReflectance);
        void setExportDeviation(bool exportDeviation);
        void setExportAmplitude(bool exportAmplitude);

    protected :
        void on_echo_transformed(echo_type echo);
        void on_shot();
        void on_shot_end();

    private :
        JNIEnv *env;
        serializer& serialize;
        bool exportReflectance;
        bool exportDeviation;
        bool exportAmplitude;
    };
}



#endif /* MYPINTCLOUD_H_ */
