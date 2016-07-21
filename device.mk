
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

# CAF Branch
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    ro.par.branch=LA.BF.1.1.3-01310-8x74.0

# Ramdisk
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*,device/oneplus/bacon/ramdisk,root)

# Prebuilt
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*,device/oneplus/bacon/prebuilt/system,system)

# Overlays
DEVICE_PACKAGE_OVERLAYS += device/oneplus/bacon/overlay
PRODUCT_PACKAGE_OVERLAYS += device/oneplus/bacon/overlay

PRODUCT_AAPT_CONFIG += xxhdpi
PRODUCT_AAPT_PREF_CONFIG := xxhdpi

# CodeAurora MSM9874 Device Tree
$(call inherit-product, device/qcom/msm8974/msm8974.mk)

$(call inherit-product, device/oneplus/bacon/common.mk)
$(call inherit-product, vendor/oneplus/bacon/bacon-vendor.mk)

# Haters gonna hate ..
PRODUCT_CHARACTERISTICS := nosdcard

# WiFi
PRODUCT_PACKAGES += \
    wcnss_service

# NFC
PRODUCT_PACKAGES += \
    nfc_nci.pn54x.default

# USB
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp

# Power
PRODUCT_PACKAGES += \
    power.msm8974

# Doze mode
PRODUCT_PACKAGES += \
    OneplusDoze
