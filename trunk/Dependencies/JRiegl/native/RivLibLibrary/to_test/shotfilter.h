#ifndef FILTER_H_
#define FILTER_H_

#include "riegl/scanlib.hpp"

using namespace scanlib;

class shotfilter {
public:
    shotfilter();
    virtual ~shotfilter();

    virtual bool shotToKeep(pointcloud* p) = 0;
    virtual bool echoToKeep(target& t) = 0;
};

#endif /* FILTER_H_ */
