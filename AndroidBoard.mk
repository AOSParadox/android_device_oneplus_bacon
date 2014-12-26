LOCAL_PATH := $(call my-dir)

#
# Compile Linux Kernel
#
KERNEL_DEFCONFIG := cyanogenmod_bacon_defconfig

include device/qcom/msm8974/AndroidBoard.mk

#Create symbolic links
$(shell mkdir -p $(TARGET_OUT_ETC)/firmware/wlan/prima; \
	cp device/oneplus/bacon/wifi/WCNSS_qcom_wlan_nv.bin $(TARGET_OUT_ETC)/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin \
	ln -sf /persist/WCNSS_qcom_wlan_nv.bin \
	$(TARGET_OUT_ETC)/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin; \
	cp device/oneplus/bacon/wifi/WCNSS_qcom_cfg.ini $(TARGET_OUT_ETC)/firmware/wlan/prima/WCNSS_qcom_cfg.ini \
	ln -sf /data/misc/wifi/WCNSS_qcom_cfg.ini \
	$(TARGET_OUT_ETC)/firmware/wlan/prima/WCNSS_qcom_cfg.ini)


