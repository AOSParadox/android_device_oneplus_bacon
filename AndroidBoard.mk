LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#----------------------------------------------------------------------
# Radio image
#----------------------------------------------------------------------
ifeq ($(ADD_RADIO_FILES), true)
radio_dir := $(LOCAL_PATH)/radio
RADIO_FILES := $(shell cd $(radio_dir) ; ls)
$(foreach f, $(RADIO_FILES), \
    $(call add-radio-file,radio/$(f)))
endif

TARGET_BOOTLOADER_EMMC_INTERNAL := $(LOCAL_PATH)/images/emmc_appsboot.mbn
$(TARGET_BOOTLOADER_EMMC_INTERNAL): $(TARGET_BOOTLOADER)

INSTALLED_RADIOIMAGE_TARGET += $(TARGET_BOOTLOADER_EMMC_INTERNAL)
$(call add-radio-file,images/logo.bin)
$(call add-radio-file,images/NON-HLOS.bin)
$(call add-radio-file,images/rpm.mbn)
$(call add-radio-file,images/sbl1.mbn)
$(call add-radio-file,images/sdi.mbn)
$(call add-radio-file,images/static_nvbk.bin)
$(call add-radio-file,images/tz.mbn)
