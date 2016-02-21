package com.qti.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.qualcomm.qcrilhook.IQcRilHook;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public class UiccCardProvisioner extends Handler {
    private static final String ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED = "org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED";
    private static final boolean DBG = true;
    private static final int EVENT_ICC_CHANGED = 1;
    private static final int EVENT_MANUAL_PROVISION_DONE = 3;
    private static final int EVENT_OEM_HOOK_SERVICE_READY = 4;
    private static final int EVENT_UNSOL_MANUAL_PROVISION_STATUS_CHANGED = 2;
    private static final String EXTRA_NEW_PROVISION_STATE = "newProvisionState";
    private static final int GENERIC_FAILURE = -1;
    private static final int INVALID_INPUT = -2;
    private static final String LOG_TAG = "UiccCardProvisioner";
    private static final int REQUEST_IN_PROGRESS = -3;
    private static final int SUCCESS = 0;
    private static final boolean VDBG = false;
    private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private static AtomicBoolean mRequestInProgress = new AtomicBoolean(false);
    private static UiccController mUiccController = null;
    private static Object sManualProvLock = new Object();
    private CardState[] mCardState = new CardState[mNumPhones];
    private Context mContext;
    private UiccProvisionStatus[] mProvisionStatus = new UiccProvisionStatus[mNumPhones];
    private QtiRilInterface mQtiRilInterface;
    private String[] mSimIccId = new String[mNumPhones];

    static class UiccProvisionStatus {
        static final int CARD_NOT_PRESENT = -2;
        static final int INVALID_STATE = -1;
        static final int NOT_PROVISIONED = 0;
        static final int PROVISIONED = 1;
        private int currentState = INVALID_STATE;
        private int userPreference = INVALID_STATE;

        UiccProvisionStatus() {
        }

        boolean equals(UiccProvisionStatus provisionStatus) {
            if (provisionStatus.getUserPreference() == getUserPreference() && provisionStatus.getCurrentState() == getCurrentState()) {
                return UiccCardProvisioner.DBG;
            }
            return false;
        }

        int getUserPreference() {
            return this.userPreference;
        }

        void setUserPreference(int pref) {
            this.userPreference = pref;
        }

        int getCurrentState() {
            return this.currentState;
        }

        void setCurrentState(int state) {
            this.currentState = state;
        }

        public String toString() {
            return "User pref " + this.userPreference + " Current pref " + this.currentState;
        }
    }

    UiccCardProvisioner(Context context) {
        logd(" Invoking constructor, no of phones = " + mNumPhones);
        this.mContext = context;
        for (int index = 0; index < mNumPhones; index += EVENT_ICC_CHANGED) {
            this.mSimIccId[index] = null;
            this.mProvisionStatus[index] = new UiccProvisionStatus();
            this.mCardState[index] = CardState.CARDSTATE_ABSENT;
        }
        mUiccController = UiccController.getInstance();
        mUiccController.registerForIccChanged(this, EVENT_ICC_CHANGED, null);
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
        this.mQtiRilInterface.registerForServiceReadyEvent(this, EVENT_OEM_HOOK_SERVICE_READY, null);
        this.mQtiRilInterface.registerForUnsol(this, EVENT_UNSOL_MANUAL_PROVISION_STATUS_CHANGED, null);
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case EVENT_ICC_CHANGED /*1*/:
                ar = (AsyncResult) msg.obj;
                if (ar.result != null) {
                    updateIccAvailability(((Integer) ar.result).intValue());
                    return;
                } else {
                    loge("Error: Invalid card index EVENT_ICC_CHANGED ");
                    return;
                }
            case EVENT_UNSOL_MANUAL_PROVISION_STATUS_CHANGED /*2*/:
                ar = (AsyncResult) msg.obj;
                if (ar.result != null) {
                    handleUnsolManualProvisionEvent((Message) ar.result);
                    return;
                } else {
                    loge("Error: empty result, UNSOL_MANUAL_PROVISION_STATUS_CHANGED");
                    return;
                }
            case EVENT_MANUAL_PROVISION_DONE /*3*/:
                logd(" MANUAL_PROVISION_STATUS_CHANGED, pref[" + msg.arg1 + "] = " + msg.arg2);
                int slotId = msg.arg1;
                if (getCurrentProvisioningStatus(slotId) != msg.arg2) {
                    QtiSubscriptionController.getInstance().updateUserPreferences();
                    broadcastManualProvisionStatusChanged(msg.arg1, msg.arg2);
                    return;
                }
                return;
            case EVENT_OEM_HOOK_SERVICE_READY /*4*/:
                ar = (AsyncResult) msg.obj;
                if (ar.result == null) {
                    loge("Error: empty result, EVENT_OEM_HOOK_SERVICE_READY");
                    return;
                } else if (((Boolean) ar.result).booleanValue()) {
                    queryAllUiccProvisionInfo();
                    return;
                } else {
                    return;
                }
            default:
                loge("Error: hit default case " + msg.what);
                return;
        }
    }

    private void handleUnsolManualProvisionEvent(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("Null data received in handleUnsolManualProvisionEvent");
            return;
        }
        ByteBuffer payload = ByteBuffer.wrap((byte[]) msg.obj);
        payload.order(ByteOrder.nativeOrder());
        int rspId = payload.getInt();
        int slotId = msg.arg1;
        if (isValidSlotId(slotId) && rspId == IQcRilHook.QCRILHOOK_UNSOL_UICC_PROVISION_STATUS_CHANGED) {
            logi(" Unsol: rspId " + rspId + " slotId " + msg.arg1);
            queryUiccProvisionInfo(slotId);
        }
    }

    private void queryAllUiccProvisionInfo() {
        for (int index = 0; index < mNumPhones; index += EVENT_ICC_CHANGED) {
            logd(" query  provision info, card state[" + index + "] = " + this.mCardState[index]);
            if (this.mCardState[index] == CardState.CARDSTATE_PRESENT) {
                queryUiccProvisionInfo(index);
            }
        }
    }

    private void queryUiccProvisionInfo(int phoneId) {
        if (this.mQtiRilInterface.isServiceReady()) {
            UiccProvisionStatus oldStatus = this.mProvisionStatus[phoneId];
            String oldIccId = this.mSimIccId[phoneId];
            UiccProvisionStatus subStatus = this.mQtiRilInterface.getUiccProvisionPreference(phoneId);
            if (!(subStatus.getCurrentState() == GENERIC_FAILURE || subStatus.getUserPreference() == GENERIC_FAILURE)) {
                synchronized (sManualProvLock) {
                    this.mProvisionStatus[phoneId] = subStatus;
                }
            }
            String iccId = this.mQtiRilInterface.getUiccIccId(phoneId);
            if (iccId != null) {
                if (this.mSimIccId[phoneId] == null) {
                    logi("add subInfo record, iccId[" + phoneId + "] = " + iccId);
                    QtiSubscriptionInfoUpdater.getInstance().addSubInfoRecord(phoneId, iccId);
                }
                this.mSimIccId[phoneId] = iccId;
            }
            logd(" queryUiccProvisionInfo, iccId[" + phoneId + "] = " + iccId + " " + this.mProvisionStatus[phoneId]);
            if (this.mSimIccId[phoneId] != oldIccId || !oldStatus.equals(this.mProvisionStatus[phoneId])) {
                if (this.mSimIccId[phoneId] != null) {
                    int[] subIds = QtiSubscriptionController.getInstance().getSubId(phoneId);
                    if (!(subIds == null || subIds.length == 0 || !QtiSubscriptionController.getInstance().isActiveSubId(subIds[0]))) {
                        QtiSubscriptionController.getInstance().updateUserPreferences();
                    }
                }
                logd(" broadcasting ProvisionInfo, phoneId = " + phoneId);
                broadcastManualProvisionStatusChanged(phoneId, getCurrentProvisioningStatus(phoneId));
                return;
            }
            return;
        }
        logi("Oem hook service is not ready yet " + phoneId);
    }

    private void updateIccAvailability(int slotId) {
        if (isValidSlotId(slotId)) {
            CardState newState = CardState.CARDSTATE_ABSENT;
            UiccCard newCard = mUiccController.getUiccCard(slotId);
            if (newCard != null) {
                newState = newCard.getCardState();
                logd("updateIccAvailability, card state[" + slotId + "] = " + newState);
                this.mCardState[slotId] = newState;
                int currentState = getCurrentProvisioningStatus(slotId);
                if (this.mCardState[slotId] == CardState.CARDSTATE_PRESENT && (this.mSimIccId[slotId] == null || currentState == GENERIC_FAILURE || currentState == INVALID_INPUT)) {
                    queryUiccProvisionInfo(slotId);
                    return;
                } else if (this.mCardState[slotId] == CardState.CARDSTATE_ABSENT) {
                    synchronized (sManualProvLock) {
                        this.mProvisionStatus[slotId].setUserPreference(INVALID_INPUT);
                        this.mProvisionStatus[slotId].setCurrentState(INVALID_INPUT);
                        this.mSimIccId[slotId] = null;
                    }
                    return;
                } else {
                    return;
                }
            }
            logd("updateIccAvailability, uicc card null, ignore " + slotId);
            return;
        }
        loge("Invalid slot Index!!! " + slotId);
    }

    private void broadcastManualProvisionStatusChanged(int phoneId, int newProvisionState) {
        Intent intent = new Intent(ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED);
        intent.putExtra("phone", phoneId);
        intent.putExtra(EXTRA_NEW_PROVISION_STATE, newProvisionState);
        this.mContext.sendBroadcast(intent);
    }

    private int getCurrentProvisioningStatus(int slotId) {
        int currentState;
        synchronized (sManualProvLock) {
            currentState = this.mProvisionStatus[slotId].getCurrentState();
        }
        return currentState;
    }

    public int getCurrentUiccCardProvisioningStatus(int slotId) {
        if (mNumPhones == EVENT_ICC_CHANGED && isValidSlotId(slotId)) {
            return EVENT_ICC_CHANGED;
        }
        if (canProcessRequest(slotId)) {
            return getCurrentProvisioningStatus(slotId);
        }
        return GENERIC_FAILURE;
    }

    public int getUiccCardProvisioningUserPreference(int slotId) {
        if (mNumPhones == EVENT_ICC_CHANGED && isValidSlotId(slotId)) {
            return EVENT_ICC_CHANGED;
        }
        if (!canProcessRequest(slotId)) {
            return GENERIC_FAILURE;
        }
        int userPref;
        synchronized (sManualProvLock) {
            userPref = this.mProvisionStatus[slotId].getUserPreference();
        }
        return userPref;
    }

    public int activateUiccCard(int slotId) {
        logd(" activateUiccCard: phoneId = " + slotId);
        enforceModifyPhoneState("activateUiccCard");
        int activateStatus = 0;
        if (!canProcessRequest(slotId)) {
            return INVALID_INPUT;
        }
        if (getCurrentProvisioningStatus(slotId) == EVENT_ICC_CHANGED) {
            logd(" Uicc card in slot[" + slotId + "] already activated ");
            return 0;
        } else if (isFlexMapInProgress() || !mRequestInProgress.compareAndSet(false, DBG)) {
            return REQUEST_IN_PROGRESS;
        } else {
            boolean retVal = this.mQtiRilInterface.setUiccProvisionPreference(EVENT_ICC_CHANGED, slotId);
            if (retVal) {
                synchronized (sManualProvLock) {
                    this.mProvisionStatus[slotId].setCurrentState(EVENT_ICC_CHANGED);
                }
                sendMessage(obtainMessage(EVENT_MANUAL_PROVISION_DONE, slotId, EVENT_ICC_CHANGED));
            } else {
                activateStatus = GENERIC_FAILURE;
            }
            logi(" activation result[" + slotId + "] = " + retVal);
            mRequestInProgress.set(false);
            return activateStatus;
        }
    }

    public int deactivateUiccCard(int slotId) {
        logd(" deactivateUiccCard: phoneId = " + slotId);
        enforceModifyPhoneState("deactivateUiccCard");
        int deactivateState = 0;
        if (!canProcessRequest(slotId)) {
            return INVALID_INPUT;
        }
        if (getCurrentProvisioningStatus(slotId) == 0) {
            logd(" Uicc card in slot[" + slotId + "] already in deactive state ");
            return 0;
        } else if (isFlexMapInProgress() || !mRequestInProgress.compareAndSet(false, DBG)) {
            return REQUEST_IN_PROGRESS;
        } else {
            boolean retVal = this.mQtiRilInterface.setUiccProvisionPreference(0, slotId);
            if (retVal) {
                synchronized (sManualProvLock) {
                    this.mProvisionStatus[slotId].setCurrentState(0);
                }
                sendMessage(obtainMessage(EVENT_MANUAL_PROVISION_DONE, slotId, 0));
            } else {
                deactivateState = GENERIC_FAILURE;
            }
            logi(" deactivation result[" + slotId + "] = " + retVal);
            mRequestInProgress.set(false);
            return deactivateState;
        }
    }

    private void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private boolean canProcessRequest(int slotId) {
        if (mNumPhones > EVENT_ICC_CHANGED && isValidSlotId(slotId)) {
            return DBG;
        }
        loge("Request can't be processed, slotId " + slotId + " numPhones " + mNumPhones);
        return false;
    }

    private boolean isValidSlotId(int slotId) {
        if (slotId < 0 || slotId >= mNumPhones) {
            return false;
        }
        return DBG;
    }

    private boolean isFlexMapInProgress() {
        QtiRadioCapabilityController rcController = QtiRadioCapabilityController.getInstance();
        if (rcController == null) {
            return false;
        }
        boolean retVal = rcController.isSetNWModeInProgress();
        logd("isFlexMapInProgress: = " + retVal);
        return retVal;
    }

    public boolean isAnyProvisionRequestInProgress() {
        return mRequestInProgress.get();
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void logi(String string) {
        Rlog.i(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }
}
