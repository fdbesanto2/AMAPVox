@ECHO OFF

for /f "skip=1" %%p in ('wmic os get freephysicalmemory') do ( 
  set m=%%p
  goto :done
)
:done
echo free: %m%
set /A freeMemory=m-2097152
echo Java max heap size fixed to %freeMemory%, is total available memory minus 2go

java -jar -Xmx%freeMemory%k VoxeLidarGUIFX-1.0-r$BUILD_NUMBER$.jar