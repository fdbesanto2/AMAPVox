/*
 * peuchabonfilter.cpp
 *
 *  Created on: 21 mai 2013
 *      Author: theveny
 */

#include "puechabonfilter.h"
#include <iostream>

puechabonfilter::puechabonfilter() {

}

puechabonfilter::~puechabonfilter() {
	// TODO Auto-generated destructor stub
}

bool puechabonfilter::shotToKeep(pointcloud* ) {

    bool ret = true;
	return ret;
}

/**
 * returns true if target passes filter tests
 */
bool puechabonfilter::echoToKeep(target& ) {
    return true;
}

