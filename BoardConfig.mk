# config.mk
#
# Product-specific compile-time definitions.
#

TARGET_ARCH := arm

# Currently unused, but may want to move some things into platform later
TARGET_KERNEL_ARCH := arm

-include $(QCPATH)/common/msm8974/BoardConfigVendor.mk
include device/qcom/common/BoardConfigCommon.mk

#TODO: Fix-me: Setting TARGET_HAVE_HDMI_OUT to false
# to get rid of compilation error.
TARGET_HAVE_HDMI_OUT := false
TARGET_USES_OVERLAY := true
NUM_FRAMEBUFFER_SURFACE_BUFFERS := 3
TARGET_NO_BOOTLOADER := true
TARGET_NO_RADIOIMAGE := true
TARGET_NO_RPC := true

TARGET_CPU_ABI  := armeabi-v7a
TARGET_CPU_ABI2 := armeabi
TARGET_ARCH_VARIANT := armv7-a-neon
TARGET_CPU_VARIANT := krait

TARGET_BOARD_PLATFORM := msm8974
TARGET_BOOTLOADER_BOARD_NAME := MSM8974

ADD_RADIO_FILES ?= true
TARGET_RELEASETOOLS_EXTENSIONS := device/oneplus/bacon

BOARD_KERNEL_BASE        := 0x00000000
BOARD_KERNEL_PAGESIZE    := 2048
BOARD_KERNEL_TAGS_OFFSET := 0x01E00000
BOARD_RAMDISK_OFFSET     := 0x02000000
TARGET_KERNEL_SOURCE := kernel/oneplus/msm8974
TARGET_KERNEL_CONFIG := baconx_defconfig
TARGET_USE_CM_RAMDISK := true

# Enables Adreno RS driver
OVERRIDE_RS_DRIVER := libRSDriver_adreno.so

TARGET_INIT_VENDOR_LIB := libinit_msm

# Shader cache config options
# Maximum size of the  GLES Shaders that can be cached for reuse.
# Increase the size if shaders of size greater than 12KB are used.
MAX_EGL_CACHE_KEY_SIZE := 12*1024

# Maximum GLES shader cache size for each app to store the compiled shader
# binaries. Decrease the size if RAM or Flash Storage size is a limitation
# of the device.
MAX_EGL_CACHE_SIZE := 2048*1024

TARGET_USERIMAGES_USE_EXT4 := true
TARGET_USERIMAGES_USE_F2FS := true
BOARD_CACHEIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_PERSISTIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_FLASH_BLOCK_SIZE := 131072 # (BOARD_KERNEL_PAGESIZE * 64)

BOARD_KERNEL_CMDLINE := androidboot.hardware=qcom ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1
BOARD_KERNEL_SEPARATED_DT := true
TARGET_CUSTOM_DTBTOOL := dtbToolBacon

BOARD_BOOTIMAGE_PARTITION_SIZE     := 16777216
BOARD_CACHEIMAGE_PARTITION_SIZE    := 536870912
BOARD_PERSISTIMAGE_PARTITION_SIZE  := 33554432
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 16777216
BOARD_SYSTEMIMAGE_PARTITION_SIZE   := 1388314624
BOARD_USERDATAIMAGE_PARTITION_SIZE := 13271448576

TARGET_OTA_ASSERT_DEVICE := bacon,A0001

TARGET_USES_ION := true

TARGET_HW_DISK_ENCRYPTION := false

# Workaround framework bluetooth dependency
BOARD_HAVE_BLUETOOTH := true
BOARD_HAVE_BLUETOOTH_QCOM := true
BLUETOOTH_HCI_USE_MCT := true
BOARD_BLUETOOTH_BDROID_BUILDCFG_INCLUDE_DIR := device/oneplus/bacon

# Wifi
BOARD_HAS_QCOM_WLAN := true
BOARD_WLAN_DEVICE := qcwcn
BOARD_HOSTAPD_DRIVER := NL80211
BOARD_HOSTAPD_PRIVATE_LIB := lib_driver_cmd_$(BOARD_WLAN_DEVICE)
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_$(BOARD_WLAN_DEVICE)
WIFI_DRIVER_FW_PATH_AP := "ap"
WIFI_DRIVER_FW_PATH_STA := "sta"
WPA_SUPPLICANT_VERSION := VER_0_8_X

USE_OPENGL_RENDERER := true
# Enable dex pre-opt to speed up initial boot
ifeq ($(HOST_OS),linux)
      WITH_DEXPREOPT := true
endif

TARGET_RECOVERY_FSTAB = device/oneplus/bacon/rootdir/etc/fstab.qcom

BOARD_NFC_CHIPSET := pn547

USE_CUSTOM_AUDIO_POLICY := 1

BOARD_SEPOLICY_DIRS += \
     device/oneplus/bacon/sepolicy

TARGET_GESTURES_NODE := "/proc/touchpanel/gesture_enable"
USE_OPENGL_RENDERER := true

FEATURE_QCRIL_UIM_SAP_SERVER_MODE := true

# Control flag between KM versions
TARGET_HW_KEYMASTER_V03 := true

BOARD_VENDOR_QCOM_LOC_PDK_FEATURE_SET := true

# Flashlight Fix
COMMON_GLOBAL_CPPFLAGS += -DLEGACY_FLASHLIGHT_FIX
