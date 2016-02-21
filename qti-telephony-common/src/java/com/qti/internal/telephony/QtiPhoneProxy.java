package com.qti.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneProxy;
import com.qualcomm.qcrilhook.QmiOemHookConstants;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes;

public class QtiPhoneProxy extends PhoneProxy {
    private static final String LOG_TAG = "QtiPhoneProxy";
    private static int NOT_READY = 0;
    private static int READY = 1;
    private boolean mIsPhoneReadySent = false;
    private QtiRilInterface mQtiRilInterface;

    public QtiPhoneProxy(PhoneBase phone) {
        super(phone);
        this.mQtiRilInterface = QtiRilInterface.getInstance(phone.getContext());
    }

    public void handleMessage(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        switch (msg.what) {
            case QmiPrimitiveTypes.SIZE_OF_BYTE /*1*/:
            case QmiOemHookConstants.SUCCESS_STATUS /*3*/:
                String what = msg.what == 1 ? "EVENT_VOICE_RADIO_TECH_CHANGED" : "EVENT_REQUEST_VOICE_RADIO_TECH_DONE";
                if (ar.exception != null) {
                    loge(what + ": exception=" + ar.exception);
                    return;
                } else if (ar.result == null || ((int[]) ar.result).length == 0) {
                    loge(what + ": has no tech!");
                    return;
                } else {
                    int newVoiceTech = ((int[]) ar.result)[0];
                    logd(what + ": newVoiceTech=" + newVoiceTech);
                    phoneObjectUpdater(newVoiceTech);
                    updatePhoneReady(this.mPhoneId);
                    return;
                }
            case QmiPrimitiveTypes.SIZE_OF_INT /*4*/:
                this.mIsPhoneReadySent = false;
                break;
            case QmiPrimitiveTypes.SIZE_OF_LONG /*8*/:
                this.mIsPhoneReadySent = false;
                updatePhoneReady(this.mPhoneId);
                return;
        }
        super.handleMessage(msg);
    }

    private void updatePhoneReady(int phoneId) {
        if (!this.mIsPhoneReadySent && SystemProperties.getInt("persist.radio.poweron_opt", 0) == 1) {
            logd("Sending Phone Ready to RIL.");
            this.mQtiRilInterface.sendPhoneStatus(READY, phoneId);
            this.mIsPhoneReadySent = true;
        }
    }

    protected void logd(String msg) {
        Rlog.d(LOG_TAG, "[QtiPhoneProxy] " + msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[QtiPhoneProxy] " + msg);
    }
}
