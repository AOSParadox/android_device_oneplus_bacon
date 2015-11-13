/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <errno.h>
#include <fcntl.h>
#include <stdbool.h>
#include <string.h>

#define LOG_TAG "PowerHal"
#include <utils/Log.h>

#include <hardware/hardware.h>
#include <hardware/power.h>

#define CPU_PATH_MAX 	   "/sys/kernel/msm_thermal/user_maxfreq"
#define LOW_POWER_MAX_FREQ "960000"
#define NORMAL_MAX_FREQ    "2457600"

static bool low_power_mode;

static int sysfs_write(const char *path, char *s)
{
    char buf[80];
    int len;
    int fd = open(path, O_WRONLY);

    if (fd < 0) {
        strerror_r(errno, buf, sizeof(buf));
        ALOGE("Error opening %s: %s\n", path, buf);
        return -1;
    }

    len = write(fd, s, strlen(s));
    if (len < 0) {
        strerror_r(errno, buf, sizeof(buf));
        ALOGE("Error writing to %s: %s\n", path, buf);
        return -1;
    }

    close(fd);
    return 0;
}

static void power_init(__attribute__((unused)) struct power_module *module)
{
    ALOGI("%s", __func__);
}

static void power_set_interactive(__attribute__((unused)) struct power_module *module,
                      __attribute__((unused)) int on)
{
}

static void power_hint(__attribute__((unused)) struct power_module *module,
                      power_hint_t hint, __attribute__((unused)) void *data)
{
    if (hint != POWER_HINT_LOW_POWER)
        return;

    if (low_power_mode) {
        low_power_mode = false;
        sysfs_write(CPU_PATH_MAX, NORMAL_MAX_FREQ);
    } else {
        low_power_mode = true;
        sysfs_write(CPU_PATH_MAX, LOW_POWER_MAX_FREQ);
    }
}

static struct hw_module_methods_t power_module_methods = {
    .open = NULL,
};

void set_feature(__attribute__((unused)) struct power_module *module,
		feature_t feature, int state)
{
#ifdef TAP_TO_WAKE_NODE
    char tmp_str[64];

    if (feature == POWER_FEATURE_DOUBLE_TAP_TO_WAKE) {
        snprintf(tmp_str, 64, "%d", state);
        sysfs_write(TAP_TO_WAKE_NODE, tmp_str);
    }
#endif
}

struct power_module HAL_MODULE_INFO_SYM = {
    .common = {
        .tag = HARDWARE_MODULE_TAG,
        .module_api_version = POWER_MODULE_API_VERSION_0_3,
        .hal_api_version = HARDWARE_HAL_API_VERSION,
        .id = POWER_HARDWARE_MODULE_ID,
        .name = "Bacon Power HAL",
        .author = "The CyanogenMod Project",
        .methods = &power_module_methods,
    },
    .init = power_init,
    .powerHint = power_hint,
    .setInteractive = power_set_interactive,
    .setFeature = set_feature
};
