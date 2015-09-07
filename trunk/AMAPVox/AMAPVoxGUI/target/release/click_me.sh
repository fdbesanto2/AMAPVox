#!/bin/sh

freeMemory=`cat /proc/meminfo | grep MemFree | awk '{ print $2 }'`
offset=2097152
maxMemory=$(($freeMemory - $offset))
echo Java max heap size fixed to ${maxMemory}k
java -jar -Xmx${maxMemory}k AMAPVoxGUI-1.0-r$BUILD_NUMBER$.jar