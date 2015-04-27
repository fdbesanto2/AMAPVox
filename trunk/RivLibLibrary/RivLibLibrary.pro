#-------------------------------------------------
#
# Project created by QtCreator 2015-04-24T16:44:50
#
#-------------------------------------------------

QT       -= gui

TARGET = RivLibLibrary
TEMPLATE = lib

DEFINES += RIVLIBLIBRARY_LIBRARY

SOURCES += \
    src/Fast_serializer.cpp \
    src/mypointcloud.cpp \
    src/puechabonfilter.cpp \
    src/RxpExtraction.cpp \
    src/shotfilter.cpp

HEADERS += \
    src/Fast_serializer.h \
    src/mypointcloud.h \
    src/Parameters.h \
    src/puechabonfilter.h \
    src/RxpExtraction.h \
    src/serializer.h \
    src/shotfilter.h

unix {
    target.path = /usr/lib
    INSTALLS += target
}

win32:QMAKE_CFLAGS_DEBUG += /MT
win32:QMAKE_CXXFLAGS_DEBUG += /MT

win32:QMAKE_CFLAGS_RELEASE += /MT
win32:QMAKE_CXXFLAGS_RELEASE += /MT

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

win32:INCLUDEPATH += $$PWD/include/win64
win32:DEPENDPATH += $$PWD/include/win64

unix:INCLUDEPATH += $$PWD/include/linux64
unix:DEPENDPATH += $$PWD/include/linux64


win32: LIBS += -L$$PWD/lib/windows/ -lctrlifc-mt-s

win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/ctrlifc-mt-s.lib


win32: LIBS += -L$$PWD/lib/windows/ -llibctrllib-mt-s

win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/libctrllib-mt-s.lib
else:win32-g++: PRE_TARGETDEPS += $$PWD/lib/linux/libctrllib-mt-s.a

win32: LIBS += -L$$PWD/lib/windows/ -llibriboost_chrono-mt-s


win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/libriboost_chrono-mt-s.lib

win32: LIBS += -L$$PWD/lib/windows/ -llibriboost_date_time-mt-s


win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/libriboost_date_time-mt-s.lib
else:win32-g++: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_date_time-mt-s.a

win32: LIBS += -L$$PWD/lib/windows/ -llibriboost_filesystem-mt-s


win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/libriboost_filesystem-mt-s.lib
else:win32-g++: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_filesystem-mt-s.a

win32: LIBS += -L$$PWD/lib/windows/ -llibriboost_regex-mt-s


win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/libriboost_regex-mt-s.lib
else:win32-g++: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_regex-mt-s.a

win32: LIBS += -L$$PWD/lib/windows/ -llibriboost_system-mt-s


win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/libriboost_system-mt-s.lib
else:win32-g++: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_system-mt-s.a

win32: LIBS += -L$$PWD/lib/windows/ -llibriboost_thread-mt-s


win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/libriboost_thread-mt-s.lib
else:win32-g++: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_thread-mt-s.a


win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/scanifc-mt-s.lib

win32: LIBS += -L$$PWD/lib/windows/ -llibscanlib-mt-s


win32:!win32-g++: PRE_TARGETDEPS += $$PWD/lib/windows/libscanlib-mt-s.lib
else:win32-g++: PRE_TARGETDEPS += $$PWD/lib/linux/libscanlib-mt-s.a



unix:!macx: LIBS += -L$$PWD/lib/linux/ -lscanlib-mt-s
unix:!macx: PRE_TARGETDEPS += $$PWD/lib/linux/libscanlib-mt-s.a

unix:!macx: LIBS += -L$$PWD/lib/linux/ -lboost_regex-mt-s-1_43_0-vns
unix:!macx: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_regex-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/linux/ -lrftlib-mt-s
unix:!macx: PRE_TARGETDEPS += $$PWD/lib/linux/librftlib-mt-s.a

unix:!macx: LIBS += -L$$PWD/lib/linux/ -lctrllib-mt-s
unix:!macx: PRE_TARGETDEPS += $$PWD/lib/linux/libctrllib-mt-s.a

unix:!macx: LIBS += -L$$PWD/lib/linux/ -lboost_filesystem-mt-s-1_43_0-vns
unix:!macx: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_filesystem-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/linux/ -lboost_thread-mt-s-1_43_0-vns
unix:!macx: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_thread-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/linux/ -lboost_system-mt-s-1_43_0-vns
unix:!macx: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_system-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/linux/ -lboost_date_time-mt-s-1_43_0-vns
unix:!macx: PRE_TARGETDEPS += $$PWD/lib/linux/libboost_date_time-mt-s-1_43_0-vns.a


