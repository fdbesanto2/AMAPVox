#include "simpleDLL.h"

using namespace simpleDLLNS;

void someCallback(char* msg, int i, Shot*)
{
	
}

int main(){

	simpleDLL dll;

	dll.registerCallback(&someCallback);
	//dll.simpleConnection("\\\\forestview01\\BDLidar\\TLS\\Puechabon2013\\PuechabonAvril\\PuechabonAvril2013.RiSCAN\\SCANS\\ScanPos001\\SINGLESCANS\\130403_091135.rxp", 1000);
	dll.simpleConnection("\\\\forestview01\\BDLidar\\TLS\\Puechabon2013\\PuechabonAvril\\PuechabonAvril2013.RiSCAN\\SCANS\\ScanPos001\\SINGLESCANS\\130403_091135.mon.rxp", 1000);

	return 0;
}