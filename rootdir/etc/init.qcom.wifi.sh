#!/system/bin/sh
# Copyright (c) 2009-2013, The Linux Foundation. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of The Linux Foundation nor
#       the names of its contributors may be used to endorse or promote
#       products derived from this software without specific prior written
#       permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NON-INFRINGEMENT ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
# OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
# ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

# Workaround for conn_init not copying the updated firmware
rm /data/misc/wifi/WCNSS_qcom_cfg.ini 2> /dev/null
rm /data/misc/wifi/WCNSS_qcom_wlan_nv.bin 2> /dev/null

echo `getprop ro.serialno` > /sys/devices/platform/wcnss_wlan.0/serial_number
setprop wlan.driver.config /data/misc/wifi/WCNSS_qcom_cfg.ini

logwrapper /system/bin/conn_init

echo 1 > /dev/wcnss_wlan

# Wait for nvitems
if [ ! "$(ls /data/opponvitems)" ]; then
    tries=30
    while [ ! "$(ls /data/opponvitems)" ]; do
        tries=$((tries-1))
        if [ $tries -eq 0 ]; then
            log -t qcom-wifi -p e "$0: timeout waiting for NV"
            exit 1
        fi
        sleep 1
    done
    logwrapper /system/bin/conn_init
    echo 0 > /sys/module/wlan/parameters/con_mode
fi

start wcnss-service
