#include <riegl/scanlib.hpp>


#include "mypointcloud.h"
#include "puechabonfilter.h"
#include "Params.h"
#include "newserializer.h"
#include "Fast_serializer.h"
#include "simpleserializer.h"
#include "my_class.h"
#include "simpleDLL.h"

#include <sstream>


using namespace std;
using namespace scanlib;

#define PROGRAMME "TLSRivLib64"

void someCallback(char* msg, int i, Shot*)
{

}

void test(string filename, Params param) {

	simpleDLLNS::simpleDLL sd;
	sd.registerCallback(&someCallback);
	sd.simpleConnection("\\\\forestview01\\BDLidar\\TLS\\Puechabon2013\\PuechabonAvril\\PuechabonAvril2013.RiSCAN\\SCANS\\ScanPos001\\SINGLESCANS\\130403_091135.rxp", 1000000);
	

	/*
	simpleDLL dll;

	dll.registerCallback(&someCallback);
	//dll.simpleConnection("\\\\forestview01\\BDLidar\\TLS\\Puechabon2013\\PuechabonAvril\\PuechabonAvril2013.RiSCAN\\SCANS\\ScanPos001\\SINGLESCANS\\130403_091135.rxp", 1000);
	dll.simpleConnection("\\\\forestview01\\BDLidar\\TLS\\Puechabon2013\\PuechabonAvril\\PuechabonAvril2013.RiSCAN\\SCANS\\ScanPos001\\SINGLESCANS\\130403_091135.mon.rxp", 1000);
	*/

	//Shot* t = sd.simpleConnection("\\\\forestview01\\BDLidar\\TLS\\Puechabon2013\\PuechabonAvril\\PuechabonAvril2013.RiSCAN\\SCANS\\ScanPos001\\SINGLESCANS\\130403_091135.rxp");

	//Shot* t = sd.simpleConnection("C:\\Users\\Julien\\Documents\\Visual Studio 2012\\Projects\\TLSRivLib\\testmtd.rxp");
	//cout<<t->beam_direction_x<<endl;

	//Shot shot = sd.simpleConnection("C:\\Users\\Julien\\Documents\\Visual Studio 2012\\Projects\\TLSRivLib\\testmtd.rxp");

	/*
	shared_ptr<basic_rconnection> conx;
	conx = basic_rconnection::create(filename);
	conx->open();

	decoder_rxpmarker dec(conx);
	puechabonfilter filter(param);

	serializer *s = new Fast_serializer(cout, filter);
	mypointcloud my(*s);
	buffer buf;

	for(dec.get(buf); !dec.eoi(); dec.get(buf)) {
	my.dispatch(buf.begin(), buf.end());
	}

	delete(s);
	*/
	/*
	serializer *s = new newserializer(cout, filter);
	mypointcloud my(*s);
	buffer buf;

	for(dec.get(buf); !dec.eoi(); dec.get(buf)) {
	my.dispatch(buf.begin(), buf.end());
	}

	delete(s);
	*/
	//conx->close();
}

void test2(string filename){

	shared_ptr<basic_rconnection> rc = basic_rconnection::create(filename);
	decoder_rxpmarker dec(rc);
	my_class my(cout);
	buffer buf;
	rc->open();
	for (dec.get(buf); !dec.eoi(); dec.get(buf)) {
		my.dispatch(buf.begin(), buf.end());
	}
	rc->close();
}

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
		test(string(argv[2]), param);
		//test2(string(argv[2]));

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

