LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := iperf-3-10-1
LOCAL_SRC_FILES := libs/$(TARGET_ARCH_ABI)/iperf3.10.1.so
include $(PREBUILT_SHARED_LIBRARY)