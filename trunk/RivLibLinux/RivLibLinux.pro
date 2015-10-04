#-------------------------------------------------
#
# Project created by QtCreator 2015-03-05T11:02:58
#
#-------------------------------------------------

QT       -= gui
TARGET = RivLibLinux
TEMPLATE = lib

DEFINES += RIVLIBLINUX_LIBRARY

SOURCES += \
    shotfilter.cpp \
    mypointcloud.cpp \
    Fast_serializer.cpp \
    RxpExtraction.cpp \
    puechabonfilter.cpp

HEADERS += \
    shotfilter.h \
    serializer.h \
    mypointcloud.h \
    Fast_serializer.h \
    RxpExtraction.h \
    puechabonfilter.h




unix:!macx: LIBS += -L$$PWD/lib/ -lscanlib-mt-s

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libscanlib-mt-s.a


unix:!macx: LIBS += -L$$PWD/lib/ -lboost_regex-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_regex-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/ -lrftlib-mt-s

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/librftlib-mt-s.a

unix:!macx: LIBS += -L$$PWD/lib/ -lctrllib-mt-s

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libctrllib-mt-s.a


unix:!macx: LIBS += -L$$PWD/lib/ -lboost_filesystem-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_filesystem-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/ -lboost_thread-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_thread-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/ -lboost_system-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_system-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/ -lboost_date_time-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_date_time-mt-s-1_43_0-vns.a
