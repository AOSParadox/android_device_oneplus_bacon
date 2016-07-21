
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
