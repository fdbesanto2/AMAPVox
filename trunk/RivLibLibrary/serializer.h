#ifndef SERIALIZER
#define SERIALIZER

#include <ostream>
#include "riegl/scanlib.hpp"
#include "shotfilter.h"

using namespace scanlib;

class serializer {
private :
public:
    std::ostream& stream;
    shotfilter& filter;

    serializer(std::ostream& st, shotfilter& filter):stream(st),filter(filter){}
    virtual ~serializer() {}

    virtual void write(pointcloud* p) = 0;

};

#endif // SERIALIZER

