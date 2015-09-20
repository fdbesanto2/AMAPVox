/*
 * peuchabonfilter.cpp
 *
 *  Created on: 21 mai 2013
 *      Author: theveny
 */

#include "puechabonfilter.h"
#include <cmath>
#include <iostream>

puechabonfilter::puechabonfilter(Params param) {

	const double pi = 3.1415926535897;
	radToDeg = 180.0 / pi;
	degToRad = pi / 180.0;
	count = 0;
	this->param = param;
}

puechabonfilter::~puechabonfilter() {
	// TODO Auto-generated destructor stub
}

bool puechabonfilter::shotToKeep(pointcloud* p) {
	bool ret = false;

	switch(this->param) {
	case VERTICAL:
		ret = verticalShotFilter(p, false);
		break;
	case VERTICALMULTIPLE:
		ret = verticalShotFilter(p, true);
		break;
	case HORIZONTAL:
		ret = horizontalShotFilter(p, false);
		break;
	case HORIZONTALMULTIPLE:
		ret = horizontalShotFilter(p, true);
		break;
	case NOFILTER :
		ret = true;
		break;
	}
	return ret;
}

/**
 * returns true if target passes filter tests
 */
bool puechabonfilter::echoToKeep(target& t) {
	bool ret = true;
	switch (this->param) {
		case VERTICAL:
		case VERTICALMULTIPLE:
			ret = verticalEchoFilter(t);
			break;
		case HORIZONTAL:
		case HORIZONTALMULTIPLE:
			ret = horizontalEchoFilter(t);
			break;
		case NOFILTER :
			ret = true;
			break;
	}
	return ret;
}

/**
 * returns phi angle from a unit direction vector
 * phi is the vertical angle (hausse)
 */
double puechabonfilter::getPhi(double t[]) {
	double phi = acos(t[2]);
	return phi;
}

/**
 * returns theta angle from a unit direction vector
 * theta is the horizontal angle (azimut)
 */
double puechabonfilter::getTheta(double t[]) {
	double phi = getPhi(t);
	double theta = 0;
	if(phi != 0) {
		theta = asin(t[1]/ sin(phi));
	} else {
		theta = asin(t[1]);
	}

	return theta;
}
/**
 * filtre les tirs pour les scan verticaux
 * si multiple est vrai on garde les tirs a echo multiples, sinon on ne prends que les tirs avec 0 ou 1 echo
 * on garde le tir s'il a des echos ou s'il est au dessus de l'horizontale (tir sans echo)
 */
bool puechabonfilter::verticalShotFilter(pointcloud* p, bool multiple) {
	bool ret = false ;

	if(this->isFilterable(p, multiple)) {
		ret = true;
	} else if (p->target_count == 0) { // si on est sur un tir sans écho
		ret = (getPhi(p->beam_direction) * radToDeg <= 90);
	}
	return ret;
}

/**
 * filtre les echos
 * retourne vrai si on doit garder l'echo, faux sinon
 * on garde si l'echo a une deviation <= 30 et une reflectance <= 0
 */
bool puechabonfilter::verticalEchoFilter(target& t) {
	bool ret = true;
	ret = ret && (t.deviation <= 30); // on ne veut que les déviations <= 30
	ret = ret && (t.reflectance <= 0); // tri de la réflectance
	return ret;
}

/**
 * filtre pourles scan horizontaux
 * si multiple est vrai on garde les tirs a echo multiples, sinon on ne prends que les tirs avec 0 ou 1 echo
 */
bool puechabonfilter::horizontalShotFilter(pointcloud* p, bool multiple) {
	bool ret = false ;
	if(this->isFilterable(p, multiple)) {
		ret = true;
	} else if (p->target_count == 0) { // si on est sur un tir sans écho
		double phi = getPhi(p->beam_direction) * radToDeg;
		ret = !((phi > 90) && (phi < 270));
	}
	return ret;
}

/**
 * filtre les echos
 * retourne vrai si l'écho correspond aux criteres
 * faux sinon
 */
bool puechabonfilter::horizontalEchoFilter(target& t) {
	bool ret = true;
	ret = ret && (t.deviation <= 30); // on ne veut que les déviations <= 30
	ret = ret && (t.reflectance <= 0); // tri de la réflectance
	return ret;
}

/*
 * retourne vrai si on doit filtrer le ou les échos
 */
bool puechabonfilter::isFilterable(pointcloud* p, bool multiple) {
	bool toKeep = false;
	if(multiple) {
		toKeep = p->target_count >= 1;
	} else {
		toKeep = p->target_count == 1;
	}
	return toKeep;
}

