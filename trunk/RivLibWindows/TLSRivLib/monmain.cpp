#include <riegl/scanlib.hpp>


#include "mypointcloud.h"
#include "puechabonfilter.h"
#include "Params.h"
#include "newserializer.h"
#include "Fast_serializer.h"
#include "simpleserializer.h"

#include <sstream>


using namespace std;
using namespace scanlib;

#define PROGRAMME "TLSRivLib64"



void badParam() {
	cout << "usage : " << PROGRAMME << " -v|h|vm|hm|0 filename" << endl;
	cout << endl;
	cout << "retourne les tirs et echos par tir LIDAR filtres selons les criteres fournis par Grégoire Vincent" << endl;
	cout << "programme utilise pour les scans LIDAR effectués à Puechabon" << endl;
	cout << endl;
	cout << "parametres : " << endl;
	cout << "-v\t\tpour un scan vertical filtré avec uniquement les tirs ayant 0 ou 1 echo" << endl;
	cout << "-vm\t\tpour un scan vertical filtré avec tous les tirs" << endl;
	cout << "-h\t\tpour un scan horizontal filtré avec uniquement les tirs ayant 0 ou 1 echo" << endl;
	cout << "-hm\t\tpour un scan horizontal filtré avec tous les tirs" << endl;
	cout << "-0\t\tpour un scan sans filtre" << endl;
	cout << "filename\tfichier RXP genere par le RIEGL VZ400" << endl;
	cout << endl;
}

int main(int argc, char* argv[]) {


	try {
		if (argc != 3) {
			badParam();
			return 1;
		}
		string v = string(argv[1]);
		if (
			(v.compare("-h") != 0) && (v.compare("-v") != 0) && (v.compare("-0") != 0) &&
			(v.compare("-hm") != 0) && (v.compare("-vm") != 0)
			) {
			badParam();
			return 1;
		}
		Params param;
		if (v.compare("-v") == 0) {
			param = VERTICAL;
		}
		else if (v.compare("-vm") == 0) {
			param = VERTICALMULTIPLE;
		}
		else if (v.compare("-h") == 0) {
			param = HORIZONTAL;
		}
		else if (v.compare("-hm") == 0) {
			param = HORIZONTALMULTIPLE;
		}
		else {
			param = NOFILTER;
		}

	}
	catch (exception& e) {
		cerr << e.what() << endl;
		return 1;
	}
	catch (...) {
		cerr << "unknown exception" << endl;
		return 1;
	}
	return 0;
}

