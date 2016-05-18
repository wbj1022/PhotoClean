LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
$(shell $(LOCAL_PATH)/getdb.sh $(LOCAL_PATH) >/dev/null )
LOCAL_MODULE_TAGS := optional

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_PACKAGE_NAME := CleanSDK

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_STATIC_JAVA_LIBRARIES := SecurePlus
include $(BUILD_PACKAGE)