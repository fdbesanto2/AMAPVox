#include "my_class.h"

my_class::my_class(ostream& o): pointcloud(false), o_(o){
	shotID = 0;
}

void my_class::on_echo_transformed(echo_type echo){

	pointcloud::on_echo_transformed(echo);
	target& t(targets[target_count-1]);
	shotID++;

	/*
	for(pointcloud::target_count_type i = 0; i < p->target_count; ++i) {
		target t = p->targets[i];
		t.echo_range;
	}
	*/
	o_ << shotID << " "<< target_count << " "<< beam_origin[0] << " "<< beam_origin[1] << " "<< beam_origin[2] << " "<< beam_direction[0] << " "<< beam_direction[1] << " "<< beam_direction[2] << "\n";

	//o_ << t.vertex[0] << ", " << t.vertex[1] << ", " << t.vertex[2]<< ", " << t.time << endl;
}
