/*
 * peuchabonfilter.h
 *
 *  Created on: 21 mai 2013
 *      Author: theveny
 */

#ifndef PUECHABONFILTER_H_
#define PUECHABONFILTER_H_

#include "shotfilter.h"
#include "Params.h"

class puechabonfilter: public shotfilter {
public:
	puechabonfilter(Params param);
    virtual ~puechabonfilter();

	virtual bool shotToKeep(pointcloud* p);
	virtual bool echoToKeep(target& t);

private :
	double getPhi(double t[]);
	double getTheta(double t[]);

	bool verticalShotFilter(pointcloud* p, bool multiple);
	bool horizontalShotFilter(pointcloud* p, bool multiple);
	bool verticalEchoFilter(target& t);
	bool horizontalEchoFilter(target& t);
	bool isFilterable(pointcloud* p, bool multiple);

	static const double pi;
	const static int COUNT_FILTER = 1000;

	double radToDeg;
	double degToRad;
	int count;
	Params param;
};



#endif /* PUECHABONFILTER_H_ */
