/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2013 The OmniROM Project
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


#define LOG_NDEBUG 0
#define LOG_TAG "lights"

#include <cutils/log.h>
#include <cutils/properties.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <pthread.h>

#include <sys/ioctl.h>
#include <sys/types.h>

#include <hardware/lights.h>

#ifndef min
#define min(a,b) ((a)<(b)?(a):(b))
#endif
#ifndef max
#define max(a,b) ((a)<(b)?(b):(a))
#endif

/******************************************************************************/

static pthread_once_t g_init = PTHREAD_ONCE_INIT;
static pthread_mutex_t g_lock = PTHREAD_MUTEX_INITIALIZER;
static struct light_state_t g_notification;
static struct light_state_t g_battery;
static struct light_state_t g_attention;
static int g_is_find7s = 0;

char const*const RED_LED_FILE
        = "/sys/class/leds/red/brightness";

char const*const GREEN_LED_FILE
        = "/sys/class/leds/green/brightness";

char const*const BLUE_LED_FILE
        = "/sys/class/leds/blue/brightness";

char const*const QPNP_RED_LED_FILE
        = "/sys/class/leds/rgb_red/brightness";

char const*const QPNP_GREEN_LED_FILE
        = "/sys/class/leds/rgb_green/brightness";

char const*const QPNP_BLUE_LED_FILE
        = "/sys/class/leds/rgb_blue/brightness";

char const*const QPNP_RAMP_STEP_FILE
        = "/sys/class/leds/rgb_red/ramp_step_ms";

char const*const QPNP_DUTY_FILE
        = "/sys/class/leds/rgb_red/duty_pcts";

char const*const QPNP_BLINK_FILE
        = "/sys/class/leds/rgb_red/blink";

char const*const LCD_FILE
        = "/sys/class/leds/lcd-backlight/brightness";

char const*const BUTTONS_FILE
        = "/sys/class/leds/button-backlight/brightness";

char const*const RED_FREQ_FILE
        = "/sys/class/leds/red/device/grpfreq";

char const*const RED_PWM_FILE
        = "/sys/class/leds/red/device/grppwm";

char const*const RED_BLINK_FILE
        = "/sys/class/leds/red/device/blink";

//The maximum LUT size is 63 steps
#define QPNP_DUTY_STEPS 63

/**
 * device methods
 */

void init_globals(void)
{
    // init the mutex
    pthread_mutex_init(&g_lock, NULL);
}

static int is_find7s(void)
{
    char value[PROPERTY_VALUE_MAX] = {'\0'};

    if (property_get("ro.oppo.device", value, NULL)) {
        if (!strcmp(value, "find7s")) {
	    return 1;
	}
    }
    return 0;
}

static int
write_str(char const* path, const char *buf)
{
    int fd;
    static int already_warned = 0;

    fd = open(path, O_RDWR);
    if (fd >= 0) {
        int bytes = strlen(buf);
        int amt = write(fd, buf, bytes);
        close(fd);
        return amt == -1 ? -errno : 0;
    } else {
        if (already_warned == 0) {
            ALOGE("write_str failed to open %s\n", path);
            already_warned = 1;
        }
        return -errno;
    }
}

static int
write_int(char const* path, int value)
{
    char buffer[20];
    sprintf(buffer, "%d\n", value);
    return write_str(path, buffer);
}

static int
is_lit(struct light_state_t const* state)
{
    return state->color & 0x00ffffff;
}

static int
rgb_to_brightness(struct light_state_t const* state)
{
    int color = state->color & 0x00ffffff;
    return ((77*((color>>16)&0x00ff))
            + (150*((color>>8)&0x00ff)) + (29*(color&0x00ff))) >> 8;
}

static int
set_light_backlight(struct light_device_t* dev,
        struct light_state_t const* state)
{
    int err = 0;
    int brightness = rgb_to_brightness(state);

    pthread_mutex_lock(&g_lock);
    err = write_int(LCD_FILE, brightness);

    // TODO for now use same as screen
    // everything below 50 is not really visible
    if (brightness != 0) {
        brightness = max(50, brightness);
    }
    write_int(BUTTONS_FILE, brightness);
    pthread_mutex_unlock(&g_lock);

    return err;
}

static int
set_speaker_light_locked_shineled(struct light_device_t* dev,
        struct light_state_t const* state)
{

    int len;
    int alpha, red, green, blue;
    int blink, freq, pwm;
    int onMS, offMS;
    unsigned int colorRGB;

    if(state == NULL) {
        red = 0;
        green = 0;
        blue = 0;
        onMS = 0;
        onMS = 0;
        blink = 0;
        freq = 0;
        pwm = 0;
    } else {
        switch (state->flashMode) {
            case LIGHT_FLASH_TIMED:
                onMS = state->flashOnMS;
                offMS = state->flashOffMS;
                break;
            case LIGHT_FLASH_NONE:
            default:
                onMS = 0;
                offMS = 0;
                break;
        }

        colorRGB = state->color;

#if 0
        ALOGD("set_speaker_light_locked mode %d, colorRGB=%08X, onMS=%d, offMS=%d\n",
                state->flashMode, colorRGB, onMS, offMS);
#endif

        red = (colorRGB >> 16) & 0xFF;
        green = (colorRGB >> 8) & 0xFF;
        blue = colorRGB & 0xFF;

        if (onMS > 0 && offMS > 0) {
            int totalMS = onMS + offMS;

            // the LED appears to blink about once per second if freq is 20
            // 1000ms / 20 = 50
            freq = totalMS / 50;
            // pwm specifies the ratio of ON versus OFF
            // pwm = 0 -> always off
            // pwm = 255 => always on
            pwm = (onMS * 255) / totalMS;

            // the low 4 bits are ignored, so round up if necessary
            if (pwm > 0 && pwm < 16)
                pwm = 16;

            blink = 1;
        } else {
            blink = 0;
            freq = 0;
            pwm = 0;
        }
    }

    write_int(RED_LED_FILE, red);
    write_int(GREEN_LED_FILE, green);
    write_int(BLUE_LED_FILE, blue);

    if (blink) {
        write_int(RED_FREQ_FILE, freq);
        write_int(RED_PWM_FILE, pwm);
    }
    write_int(RED_BLINK_FILE, blink);

    return 0;
}

static int
set_speaker_light_locked_qpnp(struct light_device_t* dev,
        struct light_state_t const* state)
{

    int len;
    int red, green, blue;
    int onMS, offMS;
    unsigned int colorRGB;

    if(state == NULL) {
        len = 0;
        red = 0;
        green = 0;
        blue = 0;
        onMS = 0;
        offMS = 0;
    } else {
        switch (state->flashMode) {
            case LIGHT_FLASH_TIMED:
                onMS = state->flashOnMS;
                offMS = state->flashOffMS;
                break;
            case LIGHT_FLASH_NONE:
            default:
                onMS = 0;
                offMS = 0;
                break;
        }

        colorRGB = state->color;

        red = (colorRGB >> 16) & 0xFF;
        green = (colorRGB >> 8) & 0xFF;
        blue = colorRGB & 0xFF;

#if 0
        ALOGD("set_speaker_light_locked_qpnp mode %d, colorRGB=%08X, onMS=%d, offMS=%d\n",
                state->flashMode, colorRGB, onMS, offMS);
#endif

        if (onMS > 0 && offMS > 0) {
	    char dutystr[4*(QPNP_DUTY_STEPS+1)];
            char* p = dutystr;
            int totalMS = onMS + offMS;
            int stepMS = totalMS/QPNP_DUTY_STEPS;
	    int onSteps = onMS/stepMS;
            int i;

	    //FIXME - This math makes my head hurt and it's been a long week
            p += sprintf(p, "0");
            for(i = 1; i <= onSteps/2; i++) {
              p += sprintf(p, ",%d", min(((100*i)/(onSteps/2)), 100));
            }
            for(; i <= onSteps; i++) {
	      p += sprintf(p, ",%d", min(((100*(onSteps-i))/(onSteps/2)), 100));
            }
            for(; i < QPNP_DUTY_STEPS - 1; i++) {
              p += sprintf(p, ",0");
            }
            sprintf(p,"\n");
#if 0
            ALOGD("set_speaker_light_locked_qpnp stepMS = %d, onSteps = %d, dutystr \"%s\"\n",
		  stepMS, onSteps, dutystr);
#endif
            write_int(QPNP_RED_LED_FILE, 0);
            write_int(QPNP_GREEN_LED_FILE, 0);
            write_int(QPNP_BLUE_LED_FILE, 0);
            write_str(QPNP_DUTY_FILE, dutystr);
            write_int(QPNP_RAMP_STEP_FILE, stepMS);
            write_int(QPNP_BLINK_FILE, 1);
        } else {
#if 0
            ALOGD("set_speaker_light_locked_qpnp red = %d\n",
		  red);
#endif
            write_int(QPNP_BLINK_FILE, 0);
            write_int(QPNP_RED_LED_FILE, red);
            write_int(QPNP_GREEN_LED_FILE, green);
            write_int(QPNP_BLUE_LED_FILE, blue);
        }
    }

    return 0;
}

static int
set_speaker_light_locked(struct light_device_t* dev,
        struct light_state_t const* state)
{
    if(g_is_find7s)
        return set_speaker_light_locked_qpnp(dev, state);
    return set_speaker_light_locked_shineled(dev, state);        
}

static void
handle_speaker_battery_locked(struct light_device_t* dev,
    struct light_state_t const* state, int state_type)
{
    if(is_lit(&g_attention)) {
        set_speaker_light_locked(dev, NULL);
        set_speaker_light_locked(dev, &g_attention);
    } else {
        if(is_lit(&g_battery) && is_lit(&g_notification)) {
            set_speaker_light_locked(dev, NULL);
            set_speaker_light_locked(dev, &g_notification);
        } else if(is_lit(&g_battery)) {
            set_speaker_light_locked(dev, NULL);
            set_speaker_light_locked(dev, &g_battery);
        } else {
            set_speaker_light_locked(dev, &g_notification);
        }
    }

}

static int
set_light_battery(struct light_device_t* dev,
        struct light_state_t const* state)
{
    pthread_mutex_lock(&g_lock);
    g_battery = *state;
    handle_speaker_battery_locked(dev, state, 0);
    pthread_mutex_unlock(&g_lock);
    return 0;
}

static int
set_light_notifications(struct light_device_t* dev,
        struct light_state_t const* state)
{
    pthread_mutex_lock(&g_lock);
    g_notification = *state;
    handle_speaker_battery_locked(dev, state, 1);
    pthread_mutex_unlock(&g_lock);
    return 0;
}

static int
set_light_attention(struct light_device_t* dev,
        struct light_state_t const* state)
{
    pthread_mutex_lock(&g_lock);
    g_attention = *state;
    /*
     * attention logic tweaks from:
     * https://github.com/CyanogenMod/android_device_samsung_d2-common/commit/6886bdbbc2417dd605f9818af2537c7b58491150
    */
    if (state->flashMode == LIGHT_FLASH_HARDWARE) {
        if (g_attention.flashOnMS > 0 && g_attention.flashOffMS == 0) {
            g_attention.flashMode = LIGHT_FLASH_NONE;
        }
    } else if (state->flashMode == LIGHT_FLASH_NONE) {
        g_attention.color = 0;
    }
    handle_speaker_battery_locked(dev, state, 2);
    pthread_mutex_unlock(&g_lock);
    return 0;
}

static int
set_light_touchkeys(struct light_device_t* dev,
        struct light_state_t const* state)
{
    int err = 0;
    int brightness = rgb_to_brightness(state);

    pthread_mutex_lock(&g_lock);
    write_int(BUTTONS_FILE, brightness);
    pthread_mutex_unlock(&g_lock);
    return err;
}


/** Close the lights device */
static int
close_lights(struct light_device_t *dev)
{
    if (dev) {
        free(dev);
    }
    return 0;
}


/******************************************************************************/

/**
 * module methods
 */

/** Open a new instance of a lights device using name */
static int open_lights(const struct hw_module_t* module, char const* name,
        struct hw_device_t** device)
{
    int (*set_light)(struct light_device_t* dev,
            struct light_state_t const* state);

    if (0 == strcmp(LIGHT_ID_BACKLIGHT, name))
        set_light = set_light_backlight;
    else if (0 == strcmp(LIGHT_ID_NOTIFICATIONS, name))
        set_light = set_light_notifications;
    else if (0 == strcmp(LIGHT_ID_BATTERY, name))
        set_light = set_light_battery;
    else if (0 == strcmp(LIGHT_ID_ATTENTION, name))
        set_light = set_light_attention;
    else if (0 == strcmp(LIGHT_ID_BUTTONS, name))
        set_light = set_light_touchkeys;
    else
        return -EINVAL;

    pthread_once(&g_init, init_globals);

    struct light_device_t *dev = malloc(sizeof(struct light_device_t));
    memset(dev, 0, sizeof(*dev));

    dev->common.tag = HARDWARE_DEVICE_TAG;
    dev->common.version = 0;
    dev->common.module = (struct hw_module_t*)module;
    dev->common.close = (int (*)(struct hw_device_t*))close_lights;
    dev->set_light = set_light;

    *device = (struct hw_device_t*)dev;

    g_is_find7s = is_find7s();

    return 0;
}

static struct hw_module_methods_t lights_module_methods = {
    .open =  open_lights,
};

/*
 * The lights Module
 */
struct hw_module_t HAL_MODULE_INFO_SYM = {
    .tag = HARDWARE_MODULE_TAG,
    .version_major = 1,
    .version_minor = 0,
    .id = LIGHTS_HARDWARE_MODULE_ID,
    .name = "N1 lights module",
    .author = "Google, Inc., OmniROM",
    .methods = &lights_module_methods,
};
