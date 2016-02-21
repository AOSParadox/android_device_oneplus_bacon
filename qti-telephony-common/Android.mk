# enable this build only when platform library is available
ifeq ($(TARGET_BUILD_JAVA_SUPPORT_LEVEL),platform)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src/java)

LOCAL_JAVA_LIBRARIES := telephony-common

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := qti-telephony-common

include $(BUILD_JAVA_LIBRARY)

endif
