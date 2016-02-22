/*
 * Copyright (C) 2016 The AOSParadox Project
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

/* Camera: Add support for manual 3A.
 *
 * Add manual white balance mode.
 * user can set the specific cct to lock the white balance.Just as other
 * white balance mode, it will lock the white balance once it's set, the
 * only difference it that the cct value is set from app.
 * 
 * Add manual focus mode
 * allow app to set the focus distance with DAC value or actuator
 * step value. Once the value is set, the focus distance is locked
 * unless app switch it back to automatically mode
 * 
 * Change-Id: I0c08ad0cea27284645e9e710c26844ca24a5c477
 * Commit-Id: https://codeaurora.org/cgit/quic/la/platform/frameworks/av/commit/?h=LA.BF.1.1.1&id=0b8cd4917f2535c59c1fdc79bcd13188b2cd3c99
*/


#define CAMERA_PARAMETERS_EXTRA_C \
const char CameraParameters::WHITE_BALANCE_MANUAL_CCT[] = "manual-cct"; \
const char CameraParameters::FOCUS_MODE_MANUAL_POSITION[] = "manual"; \
const char CameraParameters::KEY_APP_MASK[] = "app-mask";


#define CAMERA_PARAMETERS_EXTRA_H \
    static const char WHITE_BALANCE_MANUAL_CCT[]; \
    static const char FOCUS_MODE_MANUAL_POSITION[]; \
    static const char KEY_APP_MASK[];


