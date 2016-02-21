package com.qti.internal.telephony;

import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

public class QtiEmergencyCallHelper {
    private static final int INVALID = -1;
    private static final String LOG_TAG = "QtiEmergencyCallHelper";
    private static final int PRIMARY_STACK_MODEMID = 0;
    private static final int PROVISIONED = 1;
    private static QtiEmergencyCallHelper sInstance = null;

    public static int getPhoneIdForECall() {
        int phId;
        QtiSubscriptionController scontrol = QtiSubscriptionController.getInstance();
        int voicePhoneId = scontrol.getPhoneId(scontrol.getDefaultVoiceSubId());
        int phoneId = INVALID;
        TelephonyManager tm = TelephonyManager.getDefault();
        int phoneCount = tm.getPhoneCount();
        for (phId = PRIMARY_STACK_MODEMID; phId < phoneCount; phId += PROVISIONED) {
            Phone phone = PhoneFactory.getPhone(phId);
            if (phone.getServiceState().getState() == 0 || phone.getServiceState().isEmergencyOnly()) {
                phoneId = phId;
                if (phoneId == voicePhoneId) {
                    break;
                }
            }
        }
        Log.d(LOG_TAG, "Voice phoneId in service = " + phoneId);
        if (phoneId == INVALID) {
            phId = PRIMARY_STACK_MODEMID;
            while (phId < phoneCount) {
                UiccCardProvisioner uiccProvisioner = ExtTelephonyServiceImpl.getInstance().getUiccProvisionerInstance();
                if (tm.getSimState(phId) == 5 && uiccProvisioner.getCurrentUiccCardProvisioningStatus(phId) == PROVISIONED) {
                    phoneId = phId;
                    if (phoneId == voicePhoneId) {
                        break;
                    }
                }
                phId += PROVISIONED;
            }
            if (phoneId == INVALID) {
                phoneId = getPrimaryStackPhoneId();
            }
        }
        Log.d(LOG_TAG, "Voice phoneId in service = " + phoneId + " preferred phoneId =" + voicePhoneId);
        return phoneId;
    }

    private static int getPrimaryStackPhoneId() {
        int primayStackPhoneId = INVALID;
        for (int i = PRIMARY_STACK_MODEMID; i < TelephonyManager.getDefault().getPhoneCount(); i += PROVISIONED) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null) {
                Log.d(LOG_TAG, "Logical Modem id: " + phone.getModemUuId() + " phoneId: " + i);
                String modemUuId = phone.getModemUuId();
                if (modemUuId != null && modemUuId.length() > 0 && !modemUuId.isEmpty() && Integer.parseInt(modemUuId) == 0) {
                    primayStackPhoneId = i;
                    Log.d(LOG_TAG, "Primay Stack phone id: " + primayStackPhoneId + " selected");
                    break;
                }
            }
        }
        if (primayStackPhoneId != INVALID) {
            return primayStackPhoneId;
        }
        Log.d(LOG_TAG, "Returning default phone id");
        return PRIMARY_STACK_MODEMID;
    }
}
