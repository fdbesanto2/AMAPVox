#!/bin/sh

freeMemory=`cat /proc/meminfo | grep MemFree | awk '{ print $2 }'`
freeMemory=$((($freeMemory/90)*100))
echo Java max heap size fixed to ${freeMemory}k, is 90% of available memory
java -jar -Xmx${freeMemory}k VoxeLidarGUIFX-1.0-r$BUILD_NUMBER$.jar