/*
 * myclass.h
 *
 *  Created on: 15 mars 2013
 *      Author: theveny
 */

#ifndef MYPOINTCLOUD_H_
#define MYPOINTCLOUD_H_

#include "serializer.h"
#include "puechabonfilter.h"
#include "riegl/pointcloud.hpp"
#include <iostream>

using namespace scanlib;
using namespace std;

class mypointcloud: public scanlib::pointcloud {

public:
    mypointcloud(serializer& ser);
    virtual ~mypointcloud();
protected :
    void on_echo_transformed(echo_type echo);
    void on_shot();
    void on_shot_end();
private :
    serializer& serialize;
};


#endif // MYPOINTCLOUD_H
