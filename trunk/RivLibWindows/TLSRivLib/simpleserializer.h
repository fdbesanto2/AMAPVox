/*
 * serializetarget.h
 *
 *  Created on: 13 mai 2013
 *      Author: theveny
 */

#ifndef SIMPLESERIALIZER_H_
#define SIMPLESERIALIZER_H_

#include <iostream>
#include "riegl/scanlib.hpp"
#include "shotfilter.h"
#include "serializer.h"

using namespace std;
using namespace scanlib;

class simpleserializer:public serializer {
public:
	simpleserializer(ostream& st, shotfilter& filter):serializer(st,filter) {};
	virtual ~simpleserializer() {};

	virtual void write(pointcloud* p);
};

#endif /* SIMPLESERIALIZER_H_ */
