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
    rivliblibrary.cpp \
    mypointcloud.cpp \
    puechabonfilter.cpp \
    fastserializer.cpp \
    shotfilter.cpp

HEADERS += \
    rivliblibrary.h \
    serializer.h \
    shotfilter.h \
    mypointcloud.h \
    puechabonfilter.h \
    fastserializer.h

unix {
    target.path = /usr/lib
    INSTALLS += target
}

unix:!macx: LIBS += -L$$PWD/lib/ -lscanlib-mt-s

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libscanlib-mt-s.a

unix:!macx: LIBS += -L$$PWD/lib/ -lrftlib-mt-s

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/librftlib-mt-s.a

unix:!macx: LIBS += -L$$PWD/lib/ -lctrllib-mt-s

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libctrllib-mt-s.a

unix:!macx: LIBS += -L$$PWD/lib/ -lboost_thread-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_thread-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/ -lboost_system-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_system-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/ -lboost_regex-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_regex-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/ -lboost_filesystem-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_filesystem-mt-s-1_43_0-vns.a

unix:!macx: LIBS += -L$$PWD/lib/ -lboost_date_time-mt-s-1_43_0-vns

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libboost_date_time-mt-s-1_43_0-vns.a
