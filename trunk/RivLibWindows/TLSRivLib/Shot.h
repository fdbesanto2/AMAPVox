#ifndef SHOT_H
#define SHOT_H

typedef struct Shot
{
	int nbShots;

	double beam_origin_x;
	double beam_origin_y;
	double beam_origin_z;

	double beam_direction_x;
	double beam_direction_y;
	double beam_direction_z;

}Shot;

#endif