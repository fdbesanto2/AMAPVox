#!/bin/sh

freeMemory=`free | grep Mem | awk '{print $4}'`
offset=2097152
maxMemory=$(($freeMemory - $offset))
echo Java max heap size fixed to ${maxMemory}k
cd ${0%/*}
java -jar -Xmx${maxMemory}k AMAPVoxGUI-$VERSION$-r$BUILD_NUMBER$.jar