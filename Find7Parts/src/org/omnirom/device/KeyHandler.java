/*
 * Copyright (C) 2015 The Omnirom Project
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

package org.omnirom.device;

import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.IAudioService;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.android.internal.os.DeviceKeyHandler;
import com.android.internal.util.ArrayUtils;

public class KeyHandler implements DeviceKeyHandler {

    private static final String TAG = KeyHandler.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final int GESTURE_REQUEST = 1;
    private static final int GESTURE_WAKELOCK_DURATION = 3000;

    // Supported scancodes
    private static final int GESTURE_CIRCLE_SCANCODE = 250;
    private static final int GESTURE_V_SCANCODE = 252;
    private static final int KEY_DOUBLE_TAP = 255;

    private static final int[] sSupportedGestures = new int[]{
        GESTURE_CIRCLE_SCANCODE,
        GESTURE_V_SCANCODE,
        KEY_DOUBLE_TAP
    };

    private final Context mContext;
    private final PowerManager mPowerManager;
    private EventHandler mEventHandler;
    private WakeLock mGestureWakeLock;
    private KeyguardManager mKeyguardManager;

    public KeyHandler(Context context) {
        mContext = context;
        mEventHandler = new EventHandler();
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mGestureWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "GestureWakeLock");
    }

    private void ensureKeyguardManager() {
        if (mKeyguardManager == null) {
            mKeyguardManager =
                    (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        }
    }

    private class EventHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            KeyEvent event = (KeyEvent) msg.obj;
            switch(event.getScanCode()) {
            case GESTURE_CIRCLE_SCANCODE:
                if (DEBUG) Log.i(TAG, "GESTURE_CIRCLE_SCANCODE");
                ensureKeyguardManager();
                String action = null;
                mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
                if (mKeyguardManager.isKeyguardSecure() && mKeyguardManager.isKeyguardLocked()) {
                    action = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE;
                } else {
                    try {
                        WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
                    } catch (RemoteException e) {
                        // Ignore
                    }
                    action = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA;
                }
                mPowerManager.wakeUp(SystemClock.uptimeMillis());
                Intent intent = new Intent(action, null);
                startActivitySafely(intent);
                break;
            case GESTURE_V_SCANCODE:
                if (DEBUG) Log.i(TAG, "GESTURE_V_SCANCODE");
                mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
                Intent torchIntent = new Intent("com.android.systemui.TOGGLE_FLASHLIGHT");
                torchIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                UserHandle user = new UserHandle(UserHandle.USER_CURRENT);
                mContext.sendBroadcastAsUser(torchIntent, user);
                break;
            }
        }
    }

    public boolean handleKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        if (DEBUG) Log.i(TAG, "scanCode=" + event.getScanCode());
        boolean isKeySupported = ArrayUtils.contains(sSupportedGestures, event.getScanCode());
        if (isKeySupported && !mEventHandler.hasMessages(GESTURE_REQUEST)) {
            if (event.getScanCode() == KEY_DOUBLE_TAP && !mPowerManager.isScreenOn()) {
                if (DEBUG) Log.i(TAG, "KEY_DOUBLE_TAP");
                mPowerManager.wakeUp(SystemClock.uptimeMillis());
                return true;
            }
            Message msg = getMessageForKeyEvent(event);
            mEventHandler.sendMessage(msg);
        }
        return isKeySupported;
    }

    private Message getMessageForKeyEvent(KeyEvent keyEvent) {
        Message msg = mEventHandler.obtainMessage(GESTURE_REQUEST);
        msg.obj = keyEvent;
        return msg;
    }

    private void startActivitySafely(Intent intent) {
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            UserHandle user = new UserHandle(UserHandle.USER_CURRENT);
            mContext.startActivityAsUser(intent, null, user);
        } catch (ActivityNotFoundException e) {
            // Ignore
        }
    }
}

