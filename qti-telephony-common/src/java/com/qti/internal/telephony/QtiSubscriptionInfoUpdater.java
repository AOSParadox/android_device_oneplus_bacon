package com.qti.internal.telephony;

import android.content.Context;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionInfoUpdater;

public class QtiSubscriptionInfoUpdater extends SubscriptionInfoUpdater {
    private static final int EVENT_ADD_SUBINFO_RECORD = 100;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String LOG_TAG = "QtiSubscriptionInfoUpdater";
    private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private static QtiSubscriptionInfoUpdater sInstance = null;
    private boolean[] mIsRecordUpdateRequired = new boolean[mNumPhones];

    static QtiSubscriptionInfoUpdater init(Context context, Phone[] phoneProxy, CommandsInterface[] ci) {
        QtiSubscriptionInfoUpdater qtiSubscriptionInfoUpdater;
        synchronized (QtiSubscriptionInfoUpdater.class) {
            if (sInstance == null) {
                sInstance = new QtiSubscriptionInfoUpdater(context, phoneProxy, ci);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            qtiSubscriptionInfoUpdater = sInstance;
        }
        return qtiSubscriptionInfoUpdater;
    }

    static QtiSubscriptionInfoUpdater getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    private QtiSubscriptionInfoUpdater(Context context, Phone[] phoneProxy, CommandsInterface[] ci) {
        super(context, phoneProxy, ci);
        for (int index = 0; index < mNumPhones; index++) {
            this.mIsRecordUpdateRequired[index] = true;
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_ADD_SUBINFO_RECORD /*100*/:
                handleAddSubInfoRecordEvent(msg.arg1, (String) msg.obj);
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    void addSubInfoRecord(int slotId, String iccId) {
        if (iccId == null || slotId < 0 || slotId >= mNumPhones) {
            Rlog.e(LOG_TAG, "addSubInfoRecord, invalid input IccId[" + slotId + "] = " + iccId);
        } else {
            sendMessage(obtainMessage(EVENT_ADD_SUBINFO_RECORD, slotId, -1, iccId));
        }
    }

    private synchronized void handleAddSubInfoRecordEvent(int slotId, String iccId) {
        if (mIccId[slotId] == null || mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
            mIccId[slotId] = iccId;
            if (isAllIccIdQueryDone()) {
                updateSubscriptionInfoByIccId();
            }
            this.mIsRecordUpdateRequired[slotId] = false;
        } else {
            Rlog.d(LOG_TAG, "Record already exists ignore duplicate update, existing IccId = " + mIccId[slotId] + " recvd iccId[" + slotId + "] = " + iccId);
        }
    }

    protected void handleSimAbsent(int slotId) {
        this.mIsRecordUpdateRequired[slotId] = true;
        super.handleSimAbsent(slotId);
    }

    protected synchronized void updateSubscriptionInfoByIccId() {
        boolean isUpdateRequired = false;
        for (int index = 0; index < mNumPhones; index++) {
            if (this.mIsRecordUpdateRequired[index]) {
                isUpdateRequired = true;
                break;
            }
        }
        if (isUpdateRequired) {
            super.updateSubscriptionInfoByIccId();
            Rlog.d(LOG_TAG, "SIM state changed, Updating user preference ");
            QtiSubscriptionController.getInstance().updateUserPreferences();
        } else {
            Rlog.d(LOG_TAG, "Ignoring subscription update event");
        }
    }
}
