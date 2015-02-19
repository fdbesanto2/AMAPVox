/*
 * serializetarget.h
 *
 *  Created on: 13 mai 2013
 *      Author: theveny
 */

#ifndef SERIALIZER_H_
#define SERIALIZER_H_

#include <iostream>
#include "riegl/scanlib.hpp"
#include "shotfilter.h"

using namespace std;
using namespace scanlib;

class serializer {
private :
public:
	ostream& stream;
	shotfilter& filter;

	serializer(ostream& st, shotfilter& filter):stream(st),filter(filter){};
	virtual ~serializer() {};

	virtual void write(pointcloud* p) = 0;

};

#endif /* SERIALIZER_H_ */
