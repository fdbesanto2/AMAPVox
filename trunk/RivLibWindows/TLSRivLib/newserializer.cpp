/*
 * newserializer.cpp
 *
 *  Created on: 18 juin 2014
 *      Author: theveny
 */

#include "newserializer.h"



using namespace std;


newserializer::newserializer(ostream& st, shotfilter& filter):serializer(st,filter) {
	shotCounter = 0;
}

newserializer::~newserializer() {

}


void newserializer::write(pointcloud* p) {
	
	
	ostringstream ss;

	if(filter.shotToKeep(p)) {

		int count = shotCounter + 1;
		//ss << count << " ";
		int nbShots = 0;
		
		ostringstream shotStream;
		
		for(pointcloud::target_count_type i = 0; i < p->target_count; ++i) {
			target t = p->targets[i];
			if(filter.echoToKeep(t)) {
				nbShots ++;
				shotStream << " " << t.echo_range;
			}
		}
		ss
			<< nbShots << " "
			<< p->beam_origin[0] << " "
			<< p->beam_origin[1] << " "
			<< p->beam_origin[2] << " "
			<< p->beam_direction[0] << " "
			<< p->beam_direction[1] << " "
			<< p->beam_direction[2];

		if(nbShots) { // si on a des tirs, on rajoute
			ss << shotStream.str();
		}
		
		ss<<endl;
		shotCounter = count;
		
		stream << ss.str();
		
	}


}
