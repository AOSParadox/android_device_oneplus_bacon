#
# Copyright (C) 2015 The AOSParadox Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# inherit CodeAurora MSM8974 Board Config
-include device/qcom/msm8974/BoardConfig.mk

# Include path
TARGET_SPECIFIC_HEADER_PATH := device/oneplus/bacon/include

# Bootloader
TARGET_NO_BOOTLOADER := true

# Platform
TARGET_BOARD_PLATFORM_GPU := qcom-adreno330

# Asserts
TARGET_BOARD_INFO_FILE ?= device/oneplus/bacon/board-info.txt
TARGET_OTA_ASSERT_DEVICE := bacon,A0001

# Init
TARGET_INIT_VENDOR_LIB := libinit_bacon

# Flags
COMMON_GLOBAL_CFLAGS += -DNO_SECURE_DISCARD -DUSE_RIL_VERSION_10
COMMON_GLOBAL_CPPFLAGS += -DNO_SECURE_DISCARD -DUSE_RIL_VERSION_10

# QCOM hardware
BOARD_USES_QCOM_HARDWARE := false

# Audio
BOARD_USES_ALSA_AUDIO := true
AUDIO_FEATURE_ENABLED_COMPRESS_CAPTURE := true
AUDIO_FEATURE_ENABLED_COMPRESS_VOIP := true
AUDIO_FEATURE_ENABLED_EXTN_FORMATS := true
AUDIO_FEATURE_ENABLED_EXTN_POST_PROC := true
AUDIO_FEATURE_ENABLED_FLUENCE := true
AUDIO_FEATURE_ENABLED_HFP := true
AUDIO_FEATURE_ENABLED_HWDEP_CAL := true
AUDIO_FEATURE_ENABLED_INCALL_MUSIC := true
AUDIO_FEATURE_ENABLED_MULTI_VOICE_SESSIONS := true
AUDIO_FEATURE_ENABLED_PROXY_DEVICE := true
AUDIO_FEATURE_PCM_IOCTL_ENABLED := true
AUDIO_FEATURE_ENABLED_PCM_OFFLOAD := true
TARGET_USES_QCOM_MM_AUDIO := true

# ANT+
BOARD_ANT_WIRELESS_DEVICE := "vfs-prerelease"

# Bluetooth
BOARD_BLUETOOTH_BDROID_BUILDCFG_INCLUDE_DIR := device/oneplus/bacon/bluetooth
BOARD_HAVE_BLUETOOTH_QCOM := true
QCOM_BT_USE_SMD_TTY := true
BLUETOOTH_HCI_USE_MCT := true

# Camera
USE_DEVICE_SPECIFIC_CAMERA := true

# Crypto
TARGET_HW_DISK_ENCRYPTION := true

# Graphics
BOARD_EGL_CFG := device/qcom/msm8974/egl.cfg
TARGET_CONTINUOUS_SPLASH_ENABLED := true
TARGET_USES_C2D_COMPOSITION := true

# Filesystem
TARGET_USERIMAGES_USE_F2FS := true
BOARD_USERDATAIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_BOOTIMAGE_PARTITION_SIZE     := 16777216
BOARD_CACHEIMAGE_PARTITION_SIZE    := 536870912
BOARD_PERSISTIMAGE_PARTITION_SIZE  := 33554432
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 16777216
BOARD_SYSTEMIMAGE_PARTITION_SIZE   := 1388314624
BOARD_USERDATAIMAGE_PARTITION_SIZE := 13271448576

# Kernel
TARGET_KERNEL_CONFIG := bacon_defconfig
KERNEL_DEFCONFIG := bacon_defconfig
BOARD_KERNEL_CMDLINE := console=ttyHSL0,115200,n8 androidboot.hardware=bacon user_debug=31 msm_rtb.filter=0x3F ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1
BOARD_KERNEL_TAGS_OFFSET := 0x01e00000
TARGET_USE_CM_RAMDISK := true

# Lights
TARGET_PROVIDES_LIBLIGHT := true

# NFC
BOARD_NFC_CHIPSET := pn547

# Wifi
BOARD_HAS_QCOM_WLAN              := true
BOARD_HAS_QCOM_WLAN_SDK          := true
BOARD_WLAN_DEVICE                := qcwcn
WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_qcwcn
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_qcwcn
WIFI_DRIVER_MODULE_PATH          := "/system/lib/modules/wlan.ko"
WIFI_DRIVER_MODULE_NAME          := "wlan"
TARGET_USES_WCNSS_CTRL           := true
TARGET_USES_QCOM_WCNSS_QMI       := true
TARGET_USES_WCNSS_MAC_ADDR_REV   := true
TARGET_WCNSS_MAC_PREFIX          := e8bba8
WLAN_SELECT                      += CONFIG_PRIMA_WLAN=y

# Recovery
TARGET_RECOVERY_FSTAB := device/oneplus/bacon/ramdisk/fstab.bacon
BOARD_VOLD_CRYPTFS_MIGRATE := true

# Sepolicy
-include device/qcom/sepolicy/sepolicy.mk

BOARD_SEPOLICY_DIRS += \
        device/oneplus/bacon/sepolicy
