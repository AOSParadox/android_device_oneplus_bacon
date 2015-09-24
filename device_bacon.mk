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

ifneq ($(QCPATH),)
$(call inherit-product-if-exists, $(QCPATH)/common/config/device-vendor.mk)
endif

# call dalvik heap config
$(call inherit-product-if-exists, frameworks/native/build/phone-xxhdpi-2048-dalvik-heap.mk)

# call hwui memory config
$(call inherit-product-if-exists, frameworks/native/build/phone-xxhdpi-2048-hwui-memory.mk)

# call the proprietary setup
$(call inherit-product-if-exists, vendor/oneplus/bacon/bacon-vendor.mk)

ifneq ($(QCPATH),)
$(call inherit-product-if-exists, $(QCPATH)/prebuilt_HY11/target/product/msm8974/prebuilt.mk)
endif

PRODUCT_COPY_FILES += \
    device/qcom/msm8974/WCNSS_qcom_wlan_nv.bin:system/etc/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin

# CAF branch
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    ro.par.branch=LA.BF.1.1.1.c4

# Ramdisk
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*,device/oneplus/bacon/cm_ramdisk,root)

# Prebuilt
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*,device/oneplus/bacon/prebuilt/system,system)

# Inherit CodeAurora MSM9874 Device Tree
$(call inherit-product, device/qcom/msm8974/msm8974.mk)

LOCAL_PATH := device/oneplus/bacon

# Device uses high-density artwork where available
PRODUCT_AAPT_CONFIG := normal hdpi xhdpi xxhdpi
PRODUCT_AAPT_PREF_CONFIG := xxhdpi

# Overlays
DEVICE_PACKAGE_OVERLAYS += device/oneplus/bacon/overlay
DEVICE_PACKAGE_OVERLAYS += device/oneplus/bacon/overlay vendor/extra/overlays/phone-1080p
PRODUCT_PACKAGE_OVERLAYS += device/oneplus/bacon/overlay
PRODUCT_PACKAGE_OVERLAYS += device/oneplus/bacon/overlay vendor/extra/overlays/phone-1080p

# Haters gonna hate..
PRODUCT_CHARACTERISTICS := nosdcard

# ANT+
PRODUCT_PACKAGES += \
    com.dsi.ant.antradio_library

#Audio
PRODUCT_PACKAGES += \
    libqcompostprocbundle \
    libqcomvisualizer \
    libqcomvoiceprocessing

# GPS
PRODUCT_PACKAGES += \
    gps.msm8974

# Lights
PRODUCT_PACKAGES += \
    lights.msm8974

# NFC
PRODUCT_PACKAGES += \
    nfc_nci.pn54x.default

# Power
PRODUCT_PACKAGES += \
    power.msm8974

# Keystore
PRODUCT_PACKAGES += \
    keystore.msm8974 \
    keystore.qcom

# Set default USB interface
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp

# USB
PRODUCT_PACKAGES += \
    com.android.future.usb.accessory

# Wi-Fi
PRODUCT_PACKAGES += \
    wcnss_service

# Permissions
PRODUCT_COPY_FILES += \
    external/ant-wireless/antradio-library/com.dsi.ant.antradio_library.xml:system/etc/permissions/com.dsi.ant.antradio_library.xml

# call the proprietary setup
$(call inherit-product-if-exists, vendor/oneplus/bacon/bacon-vendor-blobs.mk)
