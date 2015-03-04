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

LOCAL_PATH := $(call my-dir)

# inherit CodeAurora MSM8974 Board Config
-include device/qcom/msm8974/BoardConfig.mk

# Include path
TARGET_SPECIFIC_HEADER_PATH := device/oneplus/bacon/include

# Bootloader
TARGET_BOOTLOADER_BOARD_NAME := MSM8974
TARGET_NO_BOOTLOADER := true
TARGET_NO_RADIOIMAGE := true

# Platform
TARGET_BOARD_PLATFORM_GPU := qcom-adreno330

# Assertions
TARGET_BOARD_INFO_FILE ?= device/oneplus/bacon/board-info.txt

# Kernel
TARGET_KERNEL_CONFIG := cyanogenmod_bacon_defconfig
TARGET_KERNEL_SOURCE := kernel
KERNEL_DEFCONFIG := cyanogenmod_bacon_defconfig
BOARD_KERNEL_CMDLINE := console=ttyHSL0,115200,n8 androidboot.hardware=bacon user_debug=31 msm_rtb.filter=0x3F ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1
BOARD_KERNEL_BASE := 0x00000000
BOARD_KERNEL_PAGESIZE := 2048
BOARD_KERNEL_SEPARATED_DT := true
TARGET_KERNEL_ARCH := arm

# BACON Init
TARGET_INIT_VENDOR_LIB := libinit_bacon

# Flags
COMMON_GLOBAL_CFLAGS += -DNO_SECURE_DISCARD

# QCOM hardware
BOARD_USES_QCOM_HARDWARE := true

# Audio
BOARD_USES_ALSA_AUDIO := true
BOARD_USES_ALSA_AUDIO := true
AUDIO_FEATURE_ENABLED_COMPRESS_CAPTURE := true
AUDIO_FEATURE_ENABLED_COMPRESS_VOIP := true
AUDIO_FEATURE_ENABLED_EXTN_FORMATS := true
AUDIO_FEATURE_ENABLED_EXTN_POST_PROC := true
AUDIO_FEATURE_ENABLED_FLUENCE := true
AUDIO_FEATURE_ENABLED_HFP := true
AUDIO_FEATURE_ENABLED_INCALL_MUSIC := true
AUDIO_FEATURE_ENABLED_MULTI_VOICE_SESSIONS := true
AUDIO_FEATURE_ENABLED_PCM_OFFLOAD := true
AUDIO_FEATURE_ENABLED_PCM_OFFLOAD_24 := true
AUDIO_FEATURE_ENABLED_PROXY_DEVICE := true
AUDIO_FEATURE_DISABLED_DS1_DOLBY_DDP := true
AUDIO_FEATURE_DISABLED_FM := true
AUDIO_FEATURE_ENABLED_HWDEP_CAL := true

# Bluetooth
BOARD_BLUETOOTH_BDROID_BUILDCFG_INCLUDE_DIR := device/oneplus/bacon/bluetooth
BOARD_HAVE_BLUETOOTH := true
BOARD_HAVE_BLUETOOTH_QCOM := true
QCOM_BT_USE_SMD_TTY := true
BLUETOOTH_HCI_USE_MCT := true

# Camera
USE_DEVICE_SPECIFIC_CAMERA := true
COMMON_GLOBAL_CFLAGS += -DOPPO_CAMERA_HARDWARE

# Charger
BOARD_CHARGER_DISABLE_INIT_BLANK := true

# Graphics
BOARD_EGL_CFG := device/qcom/msm8974/egl.cfg
USE_OPENGL_RENDERER := true
TARGET_CONTINUOUS_SPLASH_ENABLED := true
TARGET_USES_C2D_COMPOSITION := trueI
OVERRIDE_RS_DRIVER := libRSDriver_adreno.so
HAVE_ADRENO_SOURCE:= false
VSYNC_EVENT_PHASE_OFFSET_NS := 7500000
SF_VSYNC_EVENT_PHASE_OFFSET_NS := 5000000

# Filesystem
BOARD_BOOTIMAGE_PARTITION_SIZE     := 16777216
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 16777216

# Lights
TARGET_PROVIDES_LIBLIGHT := true

# Wifi
BOARD_HAS_QCOM_WLAN              := true
BOARD_HAS_QCOM_WLAN_SDK          := true
BOARD_WLAN_DEVICE                := qcwcn
WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_qcwcn
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_qcwcn
WIFI_DRIVER_FW_PATH_STA          := "sta"
WIFI_DRIVER_FW_PATH_AP           := "ap"
TARGET_USES_WCNSS_CTRL           := true
TARGET_USES_QCOM_WCNSS_QMI       := true
TARGET_USES_WCNSS_MAC_ADDR_REV   := true

# No old RPC for prop
TARGET_NO_RPC := true

# GPS HAL lives here
BOARD_VENDOR_QCOM_GPS_LOC_API_HARDWARE := false
TARGET_GPS_HAL_PATH := device/oneplus/bacon/gps
TARGET_PROVIDES_GPS_LOC_API := true

# Use HW crypto for ODE
TARGET_HW_DISK_ENCRYPTION := true

# Added to indicate that protobuf-c is supported in this build
PROTOBUF_SUPPORTED := true

# ANT+
BOARD_ANT_WIRELESS_DEVICE := "vfs-prerelease"

# Recovery
TARGET_RECOVERY_FSTAB := device/oneplus/bacon/rootdir/etc/fstab.bacon

TARGET_OTA_ASSERT_DEVICE := bacon,A0001

TARGET_WCNSS_MAC_PREFIX := e8bba8

# Workaround for factory issue
BOARD_VOLD_CRYPTFS_MIGRATE := true

BOARD_NFC_CHIPSET := pn547

AUDIO_FEATURE_LOW_LATENCY_PRIMARY := true
AUDIO_FEATURE_ENABLED_LOW_LATENCY_CAPTURE := true

# Include an expanded selection of fonts
EXTENDED_FONT_FOOTPRINT := true

# inherit from the proprietary version
ifneq ($(QCPATH),)
-include $(QCPATH)/common/msm8974/BoardConfigVendor.mk
endif

# SELinux policies
BOARD_SEPOLICY_DIRS += \
        device/oneplus/bacon/sepolicy

# inherit from the proprietary version
-include vendor/oppo/msm8974-common/BoardConfigVendor.mk
-include vendor/oneplus/bacon/BoardConfigVendor.mk
