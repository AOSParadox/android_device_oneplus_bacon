#!/system/bin/sh

#Copyright (C), 2008-2012, OPPO Mobile Comm Corp., Ltd
#VENDOR_EDIT
#file: init.oneplus.rf.sh
#Description: to enter powersave mode in rf mode for 13077.
#Date: 2013-12-17
#Author: zhanglong@drv

        echo 2 > /sys/module/lpm_resources/enable_low_power/l2
        echo 1 > /sys/module/lpm_resources/enable_low_power/pxo
        echo 1 > /sys/module/lpm_resources/enable_low_power/vdd_dig
        echo 1 > /sys/module/lpm_resources/enable_low_power/vdd_mem
        echo 1 > /sys/module/pm_8x60/modes/cpu0/power_collapse/suspend_enabled
        echo 1 > /sys/module/pm_8x60/modes/cpu0/power_collapse/idle_enabled
        echo 1 > /sys/module/pm_8x60/modes/cpu0/standalone_power_collapse/suspend_enabled
        echo 1 > /sys/module/pm_8x60/modes/cpu0/standalone_power_collapse/idle_enabled
        echo 1 > /sys/module/pm_8x60/modes/cpu0/retention/idle_enabled
        echo 0 > /sys/module/msm_thermal/core_control/enabled
        echo "ondemand" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor
        echo 50000 > /sys/devices/system/cpu/cpufreq/ondemand/sampling_rate
        echo 90 > /sys/devices/system/cpu/cpufreq/ondemand/up_threshold
        echo 1 > /sys/devices/system/cpu/cpufreq/ondemand/io_is_busy
        echo 2 > /sys/devices/system/cpu/cpufreq/ondemand/sampling_down_factor
        echo 10 > /sys/devices/system/cpu/cpufreq/ondemand/down_differential
        echo 70 > /sys/devices/system/cpu/cpufreq/ondemand/up_threshold_multi_core
        echo 3 > /sys/devices/system/cpu/cpufreq/ondemand/down_differential_multi_core
        echo 960000 > /sys/devices/system/cpu/cpufreq/ondemand/optimal_freq
        echo 960000 > /sys/devices/system/cpu/cpufreq/ondemand/sync_freq
        echo 1190400 > /sys/devices/system/cpu/cpufreq/ondemand/input_boost
        echo 80 > /sys/devices/system/cpu/cpufreq/ondemand/up_threshold_any_cpu_load
        echo 300000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq
        chown system /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq
        chown system /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq
        echo 1 > /sys/module/msm_thermal/core_control/enabled
        chown root.system /sys/devices/system/cpu/mfreq
        chmod 220 /sys/devices/system/cpu/mfreq
        echo 1 > /dev/cpuctl/apps/cpu.notify_on_migrate

