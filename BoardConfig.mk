#
# Copyright (C) 2016 The AOSParadox Project
# Copyright (C) 2016 Paranoid Android
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

# Assert
TARGET_OTA_ASSERT_DEVICE := bacon,A0001

# Firmware
ADD_RADIO_FILES ?= true
TARGET_NO_BOOTLOADER := true
TARGET_RELEASETOOLS_EXTENSIONS := device/oneplus/bacon

# Bluetooth
BLUETOOTH_HCI_USE_MCT := true
BOARD_BLUETOOTH_BDROID_BUILDCFG_INCLUDE_DIR := device/oneplus/bacon
BOARD_HAVE_BLUETOOTH_QCOM := true

# Dex-opt
# Enable dex pre-opt to speed up initial boot
ifeq ($(HOST_OS),linux)
      WITH_DEXPREOPT := true
endif

# GPS
BOARD_VENDOR_QCOM_LOC_PDK_FEATURE_SET := true

# Kernel
BOARD_KERNEL_CMDLINE := androidboot.hardware=qcom ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1
BOARD_KERNEL_SEPARATED_DT := true
TARGET_CUSTOM_DTBTOOL := dtbToolBacon
TARGET_INIT_VENDOR_LIB := libinit_msm
KERNEL_DEFCONFIG := baconx_defconfig
KERNEL_DIR := kernel/oneplus/msm8974
TARGET_RECOVERY_FSTAB = device/oneplus/bacon/rootdir/etc/fstab.qcom
TARGET_USE_CM_RAMDISK := true

# NFC
BOARD_NFC_CHIPSET := pn547

# Gestures
TARGET_GESTURES_NODE := "/proc/touchpanel/gesture_enable"

# Display
USE_OPENGL_RENDERER := true

# Audio
USE_CUSTOM_AUDIO_POLICY := 1

# Paritions
BOARD_BOOTIMAGE_PARTITION_SIZE     := 16777216
BOARD_CACHEIMAGE_PARTITION_SIZE    := 536870912
BOARD_PERSISTIMAGE_PARTITION_SIZE  := 33554432
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 16777216
BOARD_SYSTEMIMAGE_PARTITION_SIZE   := 1388314624
BOARD_USERDATAIMAGE_PARTITION_SIZE := 13271448576
TARGET_USERIMAGES_USE_F2FS := true

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

# Flashlight Fix
COMMON_GLOBAL_CPPFLAGS += -DLEGACY_FLASHLIGHT_FIX

# Sepolicy
BOARD_SEPOLICY_DIRS += \
     device/oneplus/bacon/sepolicy
