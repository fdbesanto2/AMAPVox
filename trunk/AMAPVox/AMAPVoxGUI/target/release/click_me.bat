@ECHO OFF

for /f "skip=1" %%p in ('wmic os get freephysicalmemory') do ( 
  set availableMemory=%%p
  goto :done
)
:done
echo available memory: %availableMemory%
set /A maxJVMMemory=availableMemory-524288

if %maxJVMMemory% LEQ 0 (GOTO :defaultMemory) else (GOTO :common)

:defaultMemory
echo test
set /A maxJVMMemory=2097152
GOTO :common

:common
echo Java max heap size fixed to %maxJVMMemory%

java -jar -Xmx%maxJVMMemory%k "%~dp0%AMAPVoxGUI-1.0-r191fa6b6.jar"