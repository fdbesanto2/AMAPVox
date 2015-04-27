#ifndef FASTSERIALIZER_H
#define FASTSERIALIZER_H

#include "serializer.h"
#include "puechabonfilter.h"


#ifdef WIN64
    #include "win64/jni.h"
    #include "win64/jni_md.h"
#else
    #include "linux64/jni.h"
    #include "linux64/jni_md.h"
#endif

class FastSerializer : public serializer
{
    public:
        FastSerializer(std::ostream& st, shotfilter& filter);
        ~FastSerializer(void);
        virtual void write(pointcloud* p);

};

#endif // FASTSERIALIZER_H
