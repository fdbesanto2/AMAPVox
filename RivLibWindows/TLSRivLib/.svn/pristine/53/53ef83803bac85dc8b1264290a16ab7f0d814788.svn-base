/*
 * myclass.cpp
 *
 *  Created on: 15 mars 2013
 *      Author: theveny
 */

#include "mypointcloud.h"

mypointcloud::mypointcloud(serializer& ser) : pointcloud(false), serialize(ser) {
}

mypointcloud::~mypointcloud() {
	// TODO Auto-generated destructor stub
}

void mypointcloud::on_echo_transformed(echo_type echo){
	pointcloud::on_echo_transformed(echo);
}

void mypointcloud::on_shot() {
	pointcloud::on_shot();
}

void mypointcloud::on_shot_end() {
	pointcloud::on_shot_end();
	serialize.write(this);
}
