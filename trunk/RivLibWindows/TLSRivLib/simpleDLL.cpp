#include "simpleDLL.h"

namespace simpleDLLNS{

	
	void simpleDLL::simpleCall(char* test) {
		cout << test <<endl;
	}

	static simpleDLL::callback c;

	void simpleDLL::registerCallback(callback myc) {
	  c = myc;
	}

	void simpleDLL::simpleConnection(char* fileName, int size){

		/*
		Shot* shots = new Shot[size+1000];

		int i = 0;
		int *realSize = &i;

		try{

			shared_ptr<basic_rconnection> conx;
			conx = basic_rconnection::create(fileName);

			conx->open();
			
			decoder_rxpmarker dec(conx);
			puechabonfilter filter(Params::NOFILTER);
			
			serializer *s = new Fast_serializer(shots, realSize, cout, filter);

			mypointcloud pc(*s);
			buffer buf;

			for(dec.get(buf); !dec.eoi(); dec.get(buf)) {
				pc.dispatch(buf.begin(), buf.end());

				if(*realSize >= size){

					int j = *realSize;

					(*c)("received", j, shots);

					i = 0;
				}
			}
			
			conx->close();

			

		}catch(int e){
			//return shot;
		}

		(*c)("received", *realSize, shots);

		delete shots;
		*/
		
	}

}
