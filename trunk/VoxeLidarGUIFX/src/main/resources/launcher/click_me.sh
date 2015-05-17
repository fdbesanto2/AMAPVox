#!/bin/sh

freeMemory=`cat /proc/meminfo | grep MemFree | awk '{ print $2 }'`
freeMemory=$($freeMemory-2097152)
echo Java max heap size fixed to ${freeMemory}k, is total available memory minus 2go
java -jar -Xmx${freeMemory}k VoxeLidarGUIFX-1.0-r$BUILD_NUMBER$.jar