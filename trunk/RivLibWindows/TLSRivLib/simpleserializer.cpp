/*
 * serializetarget.cpp
 *
 *  Created on: 13 mai 2013
 *      Author: theveny
 */

#include "simpleserializer.h"
#include <sstream>
using namespace std;


void simpleserializer::write(pointcloud* p) {
	ostringstream ss;
//	bool echoKept = false;	// vrai si on a au moins un echo
	if(filter.shotToKeep(p)) {
		ss
			<< "S"
//			<< ", " << p->time
			<< ", " << p->beam_origin[0]
			<< ", " << p->beam_origin[1]
			<< ", " << p->beam_origin[2]
			<< ", " << p->beam_direction[0]
			<< ", " << p->beam_direction[1]
			<< ", " << p->beam_direction[2]
			<< endl;
		for(pointcloud::target_count_type i = 0; i < p->target_count; ++i) {
			target t = p->targets[i];
			if(filter.echoToKeep(t)) {
//				echoKept = true;
				ss
					<< "E"
					<< ", " << t.echo_range
					<< ", " << t.amplitude
					<< ", " << t.reflectance
					<< ", " << t.deviation
					<< endl;
			}
		}
		//if(echoKept)
			stream << ss.str();
	}

}
