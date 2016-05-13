LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
$(shell $(LOCAL_PATH)/version_generator.sh $(LOCAL_PATH))
LOCAL_MODULE_TAGS := optional

# # Any libraries that this library depends on
# LOCAL_JAVA_LIBRARIES := android.test.runner

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_MODULE := SecurePlus

include $(BUILD_STATIC_JAVA_LIBRARY)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
