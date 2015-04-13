#-------------------------------------------------
#
# Project created by QtCreator 2015-04-09T08:49:16
#
#-------------------------------------------------

QT       -= gui

TARGET = LasZipLibrary
TEMPLATE = lib

DEFINES += LASZIPLIBRARY_LIBRARY

SOURCES += \
    lasziplibrary.cpp

HEADERS += \
    lasziplibrary.h

unix {
    target.path = /usr/lib
    INSTALLS += target
}

unix:!macx: LIBS += -L$$PWD/lib/ -lLasZip

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libLasZip.a
