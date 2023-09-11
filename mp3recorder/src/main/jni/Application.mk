APP_ABI := armeabi-v7a x86
#APP_ABI:=armeabi-v7a x86 x86_64 mips
APP_CPPFLAGS := -std=c++11
APP_CPPFLAGS += -fexceptions
APP_CFLAGS += -DSTDC_HEADERS
APP_PLATFORM := android-16
APP_ABI := all

LOCAL_CFLAGS := -std=c++11

APP_STL := c++_shared