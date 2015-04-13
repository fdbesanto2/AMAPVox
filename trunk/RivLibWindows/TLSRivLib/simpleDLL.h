using namespace std;

#include <riegl/scanlib.hpp>

#include "Fast_serializer.h"
#include "puechabonfilter.h"
#include "Params.h"
#include <list>

#include "Shot.h"
#include "ShotComplete.h"
#include "mypointcloud.h"

using namespace scanlib;


#ifndef DLLTEST
#define DLLTEST __declspec(dllexport) 
#endif


namespace simpleDLLNS
{

	class simpleDLL
	{
		public:

			typedef  void (*callback)(char *, int size, Shot*);

			static /*DLLTEST*/ void simpleCall(char* test);

			static /*DLLTEST*/ void simpleConnection(char* fileName, int size);

			static /*DLLTEST*/ void registerCallback(callback myc);
	};
}


