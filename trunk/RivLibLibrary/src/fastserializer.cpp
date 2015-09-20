#include "fastserializer.h"

FastSerializer::FastSerializer(std::ostream& st, shotfilter& filter) :serializer(st, filter)
{

}

FastSerializer::~FastSerializer(void)
{

}

void FastSerializer::write(pointcloud* ) {

}

