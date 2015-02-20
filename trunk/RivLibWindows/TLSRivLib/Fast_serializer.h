#ifndef FAST_SERIALIZER_H
#define FAST_SERIALIZER_H

#include "Shot.h"
#include "serializer.h"
#include <jni.h> 
#include <sstream>

#include <list>

using namespace std;

class Fast_serializer : public serializer
{
	public:
		Fast_serializer(JNIEnv *env, jmethodID method, jmethodID addShotMethod, jmethodID callConstructorMethod, jclass shotClass, jobject shots, ostream& st, shotfilter& filter);
		~Fast_serializer(void);
		virtual void write(pointcloud* p);

		int count;
		JNIEnv *env;
		jmethodID method;
		jmethodID addShotMethod;
		jobject shots;

		jmethodID callConstructorMethod;
		jclass shotClass;
};

#endif