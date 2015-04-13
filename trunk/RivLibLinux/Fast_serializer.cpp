#include "Fast_serializer.h"


Fast_serializer::Fast_serializer(JNIEnv *env, jmethodID method, jmethodID addShotMethod, jmethodID callConstructorMethod, jclass shotClass, jobject shots, std::ostream& st, shotfilter& filter) :serializer(st, filter)
{
	this->env = env;
	this->method = method;
	this->addShotMethod = addShotMethod;
	this->shots = shots;

	this->callConstructorMethod = callConstructorMethod;
	this->shotClass = shotClass;

	count = 0;
}


Fast_serializer::~Fast_serializer(void)
{
	
}

void Fast_serializer::write(pointcloud* p) {
	
	int nbShots = 0;
	
	jdouble tmp[7];

	for(pointcloud::target_count_type i = 0; i < p->target_count; ++i) {
			
			target t = p->targets[i];
			tmp[i] = t.echo_range;
			nbShots++;
	}
	

	if(nbShots>0) { // si on a des tirs, on rajoute


		jdoubleArray echos = env->NewDoubleArray(nbShots);
		env->SetDoubleArrayRegion(echos, 0, nbShots, tmp);
		
		env->CallVoidMethod(shots, method, nbShots,
			p->beam_origin[0], p->beam_origin[1], p->beam_origin[2],
			p->beam_direction[0], p->beam_direction[1], p->beam_direction[2], echos);
		
		/*
		jobject objet = env->NewObject(shotClass, callConstructorMethod, nbShots,
			p->beam_origin[0], p->beam_origin[1], p->beam_origin[2],
			p->beam_direction[0], p->beam_direction[1], p->beam_direction[2], echos);

		env->CallVoidMethod(shots, addShotMethod, objet);

		env->DeleteLocalRef(objet);
		*/

		env->DeleteLocalRef(echos);

		//count++;
	}

	

}
