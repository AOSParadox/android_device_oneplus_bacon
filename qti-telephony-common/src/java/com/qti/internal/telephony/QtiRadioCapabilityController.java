package com.qti.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneProxy;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RadioCapability;
import com.qti.internal.telephony.dataconnection.QtiDctController;
import java.util.HashMap;

public class QtiRadioCapabilityController extends Handler {
    static final String ALLOW_FLEX_MAPPING_ON_INACTIVE_SUB_PROPERTY = "persist.radio.flex_map_inactive";
    private static final boolean DBG = true;
    private static final int EVENT_GET_RADIO_CAPS_DONE = 3;
    private static final int EVENT_RADIO_AVAILABLE = 1;
    private static final int EVENT_RADIO_CAPS_AVAILABLE = 4;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 2;
    private static final int EVENT_UPDATE_BINDING_DONE = 5;
    private static final int FAILURE = 0;
    private static final String LOG_TAG = "QtiRadioCapabilityController";
    private static final int SUCCESS = 1;
    private static final boolean VDBG = false;
    private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private static QtiRadioCapabilityController sInstance;
    private static Object sSetNwModeLock = new Object();
    private CommandsInterface[] mCi;
    private Context mContext;
    private int[] mCurrentStackId = new int[mNumPhones];
    private boolean mIsSetPrefNwModeInProgress = false;
    private boolean mModemRatCapabilitiesAvailable = false;
    private Phone[] mPhone;
    private int[] mPrefNwMode = new int[mNumPhones];
    private int[] mPreferredStackId = new int[mNumPhones];
    private QtiSubscriptionController mQtiSubscriptionController = null;
    private int[] mRadioAccessFamily = new int[mNumPhones];
    private RadioCapability[] mRadioCapability = new RadioCapability[mNumPhones];
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Rlog.d(QtiRadioCapabilityController.LOG_TAG, "mReceiver: action " + action);
            if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE")) {
                QtiRadioCapabilityController.this.sendMessage(QtiRadioCapabilityController.this.obtainMessage(QtiRadioCapabilityController.EVENT_UPDATE_BINDING_DONE, QtiRadioCapabilityController.SUCCESS, -1));
            } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED")) {
                QtiRadioCapabilityController.this.sendMessage(QtiRadioCapabilityController.this.obtainMessage(QtiRadioCapabilityController.EVENT_UPDATE_BINDING_DONE, QtiRadioCapabilityController.FAILURE, -1));
            }
        }
    };
    private HashMap<Integer, Message> mStoredResponse = new HashMap();

    public static QtiRadioCapabilityController make(Context context, Phone[] phoneProxy, CommandsInterface[] ci) {
        Rlog.d(LOG_TAG, "getInstance");
        if (sInstance == null) {
            sInstance = new QtiRadioCapabilityController(context, phoneProxy, ci);
        } else {
            Log.wtf(LOG_TAG, "QtiRadioCapabilityController.make() should be called once");
        }
        return sInstance;
    }

    public static QtiRadioCapabilityController getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "QtiRadioCapabilityController.getInstance called before make");
        }
        return sInstance;
    }

    private QtiRadioCapabilityController(Context context, Phone[] phoneProxy, CommandsInterface[] ci) {
        this.mCi = ci;
        this.mContext = context;
        this.mPhone = phoneProxy;
        this.mQtiSubscriptionController = QtiSubscriptionController.getInstance();
        for (int i = FAILURE; i < this.mCi.length; i += SUCCESS) {
            this.mCi[i].registerForAvailable(this, SUCCESS, new Integer(i));
            this.mCi[i].registerForNotAvailable(this, EVENT_RADIO_NOT_AVAILABLE, new Integer(i));
            this.mStoredResponse.put(Integer.valueOf(i), null);
        }
        IntentFilter filter = new IntentFilter("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        context.registerReceiver(this.mReceiver, filter);
        logd("Constructor - Exit");
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        Integer phoneId;
        switch (msg.what) {
            case SUCCESS /*1*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logv("EVENT_RADIO_AVAILABLE, phoneId = " + phoneId);
                processRadioAvailable(ar, phoneId.intValue());
                return;
            case EVENT_RADIO_NOT_AVAILABLE /*2*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logd("EVENT_RADIO_NOT_AVAILABLE, phoneId = " + phoneId);
                processRadioNotAvailable(ar, phoneId.intValue());
                return;
            case EVENT_GET_RADIO_CAPS_DONE /*3*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logv("EVENT_GET_RADIO_CAPS_DONE, phoneId = " + phoneId);
                onGetRadioCapabilityDone(ar, (RadioCapability) ar.result, phoneId.intValue());
                return;
            case EVENT_RADIO_CAPS_AVAILABLE /*4*/:
                handleRadioCapsAvailable();
                return;
            case EVENT_UPDATE_BINDING_DONE /*5*/:
                logv(" EVENT_UPDATE_BINDING_DONE ");
                handleUpdateBindingDone(msg.arg1);
                return;
            default:
                return;
        }
    }

    private void processRadioAvailable(AsyncResult ar, int phoneId) {
        logd("processRadioAvailable on phoneId = " + phoneId);
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            this.mCi[phoneId].getRadioCapability(Message.obtain(this, EVENT_GET_RADIO_CAPS_DONE, new Integer(phoneId)));
            return;
        }
        loge("processRadioAvailable: Invalid phoneId = " + phoneId);
    }

    private void onGetRadioCapabilityDone(AsyncResult ar, RadioCapability rc, int phoneId) {
        if (rc == null && (ar.exception instanceof CommandException)) {
            loge("onGetRadioCapabilityDone: EXIT!, result null or Exception = " + ar.exception + " phoneId = " + phoneId);
            return;
        }
        logd("onGetRadioCapabilityDone on phoneId[" + phoneId + "] rc = " + rc);
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            this.mPhone[phoneId].radioCapabilityUpdated(rc);
            logd(" Updating in phone phoneId[" + phoneId + "] rc = " + rc);
            if (areAllModemCapInfoReceived()) {
                sendMessage(obtainMessage(EVENT_RADIO_CAPS_AVAILABLE));
                return;
            }
            return;
        }
        loge("onGetRadioCapabilityDone: Invalid phoneId = " + phoneId);
    }

    private boolean areAllModemCapInfoReceived() {
        for (int i = FAILURE; i < mNumPhones; i += SUCCESS) {
            if (this.mRadioCapability[i] == null) {
                return false;
            }
        }
        return DBG;
    }

    private boolean isFlexMappingAllowedOnInactiveSub() {
        return SystemProperties.getBoolean(ALLOW_FLEX_MAPPING_ON_INACTIVE_SUB_PROPERTY, false);
    }

    private void handleUpdateBindingDone(int result) {
        int i;
        boolean z = false;
        if (result == SUCCESS) {
            updateNewNwModeToDB();
        }
        for (i = FAILURE; i < mNumPhones; i += SUCCESS) {
            sendSubscriptionSettings(i);
        }
        setNWModeInProgressFlag(false);
        if (result == SUCCESS) {
            z = DBG;
        }
        notifyRadioCapsUpdated(z);
        for (i = FAILURE; i < mNumPhones; i += SUCCESS) {
            int errorCode = FAILURE;
            Message resp = (Message) this.mStoredResponse.get(Integer.valueOf(i));
            if (resp != null) {
                if (result != SUCCESS) {
                    errorCode = EVENT_RADIO_NOT_AVAILABLE;
                }
                sendResponseToTarget(resp, errorCode);
                this.mStoredResponse.put(Integer.valueOf(i), null);
            }
        }
    }

    private void handleRadioCapsAvailable() {
        this.mModemRatCapabilitiesAvailable = DBG;
        logd("handleRadioCapsAvailable... ");
        if (updateStackBindingIfRequired(false)) {
            setNWModeInProgressFlag(DBG);
        } else {
            notifyRadioCapsUpdated(false);
        }
    }

    private void processRadioNotAvailable(AsyncResult ar, int phoneId) {
        logd("processRadioNotAvailable on phoneId = " + phoneId);
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            this.mRadioCapability[phoneId] = null;
        } else {
            loge("Invalid Index!!!");
        }
        if (this.mModemRatCapabilitiesAvailable) {
            this.mModemRatCapabilitiesAvailable = false;
        }
    }

    private void syncCurrentStackInfo() {
        for (int i = FAILURE; i < mNumPhones; i += SUCCESS) {
            int i2;
            this.mCurrentStackId[i] = Integer.valueOf(this.mRadioCapability[i].getLogicalModemUuid()).intValue();
            this.mRadioAccessFamily[this.mCurrentStackId[i]] = this.mRadioCapability[i].getRadioAccessFamily();
            int[] iArr = this.mPreferredStackId;
            if (this.mCurrentStackId[i] >= 0) {
                i2 = this.mCurrentStackId[i];
            } else {
                i2 = i;
            }
            iArr[i] = i2;
            logv("syncCurrentStackInfo, current stackId[" + i + "] = " + this.mCurrentStackId[i] + " raf = " + this.mRadioAccessFamily[this.mCurrentStackId[i]]);
        }
    }

    private synchronized boolean updateStackBindingIfRequired(boolean isNwModeRequest) {
        int response;
        boolean isUpdateStackBindingRequired = false;
        boolean response2 = false;
        boolean callInProgress = isAnyCallsInProgress();
        boolean isInEcmState = isAnyPhoneInEcmState();
        String flexMapSupportType = SystemProperties.get("persist.radio.flexmap_type", "nw_mode");
        logd("updateStackBindingIfRequired");
        if (mNumPhones == SUCCESS || !flexMapSupportType.equals("nw_mode")) {
            loge("No need to update Stack Bindingm prop = " + flexMapSupportType + " ph count = " + mNumPhones);
            response = FAILURE;
        } else {
            if (!(callInProgress || isInEcmState)) {
                if (this.mModemRatCapabilitiesAvailable) {
                    int i;
                    updatePreferredStackIds(isNwModeRequest);
                    for (i = 0; i < mNumPhones; i += 1) {
                        logv(" pref stack[" + i + "] = " + this.mPreferredStackId[i] + " current stack[" + i + "] = " + this.mCurrentStackId[i]);
                        if (this.mPreferredStackId[i] != this.mCurrentStackId[i]) {
                            isUpdateStackBindingRequired = DBG;
                            break;
                        }
                    }
                    logd(" updateStackBindingIfRequired, required =  " + isUpdateStackBindingRequired);
                    if (isUpdateStackBindingRequired) {
                        RadioAccessFamily[] rafs = new RadioAccessFamily[mNumPhones];
                        for (i = FAILURE; i < mNumPhones; i += SUCCESS) {
                            rafs[i] = new RadioAccessFamily(i, this.mRadioAccessFamily[this.mPreferredStackId[i]]);
                        }
                        response2 = ProxyController.getInstance().setRadioCapability(rafs);
                    }
                    boolean response3 = response2;
                }
            }
            loge("Error: Call state = " + callInProgress + ", ecm state = " + isInEcmState + " rat cap available = " + this.mModemRatCapabilitiesAvailable);
            response = FAILURE;
        }
        return response == SUCCESS;
    }

    private void updatePreferredStackIds(boolean isNwModeRequest) {
        if (this.mModemRatCapabilitiesAvailable && areAllModemCapInfoReceived()) {
            if (!isNwModeRequest) {
                syncPreferredNwModeFromDB();
            }
            syncCurrentStackInfo();
            int curPhoneId = FAILURE;
            while (curPhoneId < mNumPhones) {
                if (isNwModeSupportedOnStack(this.mPrefNwMode[curPhoneId], this.mCurrentStackId[curPhoneId])) {
                    logd("updatePreferredStackIds: current stack[" + this.mCurrentStackId[curPhoneId] + "]supports NwMode[" + this.mPrefNwMode[curPhoneId] + "] on phoneId[" + curPhoneId + "]");
                } else {
                    logd("updatePreferredStackIds:  current stack[" + this.mCurrentStackId[curPhoneId] + "],  NwMode[" + this.mPrefNwMode[curPhoneId] + "] on phoneId[" + curPhoneId + "]");
                    int otherPhoneId = FAILURE;
                    while (otherPhoneId < mNumPhones) {
                        if (otherPhoneId != curPhoneId) {
                            logd("updatePreferredStackIds:  other stack[" + this.mCurrentStackId[otherPhoneId] + "],  NwMode[" + this.mPrefNwMode[curPhoneId] + "] on phoneId[" + curPhoneId + "]");
                            if (isNwModeSupportedOnStack(this.mPrefNwMode[curPhoneId], this.mCurrentStackId[otherPhoneId]) && isNwModeSupportedOnStack(this.mPrefNwMode[otherPhoneId], this.mCurrentStackId[curPhoneId])) {
                                logd("updatePreferredStackIds: Cross Binding is possible between phoneId[" + curPhoneId + "] and phoneId[" + otherPhoneId + "]");
                                this.mPreferredStackId[curPhoneId] = this.mCurrentStackId[otherPhoneId];
                                this.mPreferredStackId[otherPhoneId] = this.mCurrentStackId[curPhoneId];
                            }
                        }
                        otherPhoneId += SUCCESS;
                    }
                }
                curPhoneId += SUCCESS;
            }
            return;
        }
        loge("updatePreferredStackIds: Modem Caps not Available " + this.mModemRatCapabilitiesAvailable);
    }

    private boolean isNwModeSupportedOnStack(int nwMode, int stackId) {
        int[] numRafSupported = new int[mNumPhones];
        int maxNumRafSupported = FAILURE;
        boolean isSupported = false;
        for (int i = FAILURE; i < mNumPhones; i += SUCCESS) {
            numRafSupported[i] = getNumOfRafSupportedForNwMode(nwMode, this.mRadioAccessFamily[i]);
            if (maxNumRafSupported < numRafSupported[i]) {
                maxNumRafSupported = numRafSupported[i];
            }
        }
        if (numRafSupported[stackId] == maxNumRafSupported) {
            isSupported = DBG;
        }
        logd("nwMode:" + nwMode + ", on stack:" + stackId + " is " + (isSupported ? "Supported" : "Not Supported"));
        return isSupported;
    }

    private void syncPreferredNwModeFromDB() {
        for (int i = FAILURE; i < mNumPhones; i += SUCCESS) {
            this.mPrefNwMode[i] = getNetworkModeFromDB(i);
        }
    }

    private int getNetworkModeFromDB(int phoneId) {
        int[] subId = this.mQtiSubscriptionController.getSubId(phoneId);
        boolean isSubActive = this.mQtiSubscriptionController.isActiveSubId(subId[0]);
        int networkMode = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode" + subId[0], Phone.PREFERRED_NT_MODE);
        if (this.mQtiSubscriptionController.isActiveSubId(subId[0])) {
            logv(" get sub based N/W mode, val[" + phoneId + "] = " + networkMode);
            return networkMode;
        }
        try {
            networkMode = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", phoneId);
        } catch (SettingNotFoundException e) {
            loge("getPreferredNetworkMode: Could not find PREFERRED_NETWORK_MODE!!!");
            networkMode = Phone.PREFERRED_NT_MODE;
        }
        logv(" get slot based N/W mode, val[" + phoneId + "] = " + networkMode);
        return networkMode;
    }

    private void updateNewNwModeToDB() {
        for (int i = FAILURE; i < mNumPhones; i += SUCCESS) {
            int nwModeFromDB = getNetworkModeFromDB(i);
            if (this.mPrefNwMode[i] != nwModeFromDB) {
                int[] subId = this.mQtiSubscriptionController.getSubId(i);
                logi("updateNewNwModeToDB: subId[" + i + "] = " + subId + " new Nw mode = " + this.mPrefNwMode[i] + " old n/w mode = " + nwModeFromDB);
                if (this.mQtiSubscriptionController.isActiveSubId(subId[FAILURE])) {
                    Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode" + subId[FAILURE], this.mPrefNwMode[i]);
                }
                TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i, this.mPrefNwMode[i]);
            }
        }
    }

    public synchronized void setPreferredNetworkType(int phoneId, int networkType, Message response) {
        if (isSetNWModeInProgress() || isUiccProvisionInProgress()) {
            loge("setPreferredNetworkType: In Progress, nwmode[" + phoneId + "] = " + networkType);
            sendResponseToTarget(response, EVENT_RADIO_NOT_AVAILABLE);
        } else {
            boolean isSubActive = this.mQtiSubscriptionController.isActiveSubId(this.mQtiSubscriptionController.getSubId(phoneId)[FAILURE]);
            logd("setPreferredNetworkType: nwMode[" + phoneId + "] = " + networkType + " isActive = " + isSubActive);
            setNWModeInProgressFlag(DBG);
            syncPreferredNwModeFromDB();
            this.mPrefNwMode[phoneId] = networkType;
            if ((isFlexMappingAllowedOnInactiveSub() || isSubActive) && updateStackBindingIfRequired(DBG)) {
                logv("setPreferredNetworkType: store msg, nwMode[" + phoneId + "] = " + networkType);
                this.mStoredResponse.put(Integer.valueOf(phoneId), response);
            } else {
                logv("setPreferredNetworkType: sending nwMode[" + phoneId + "] = " + networkType);
                this.mCi[phoneId].setPreferredNetworkType(networkType, response);
                setNWModeInProgressFlag(false);
            }
        }
    }

    private int getNumOfRafSupportedForNwMode(int nwMode, int radioAccessFamily) {
        if (radioAccessFamily == SUCCESS) {
            loge(" Modem Capabilites are null. Return!!, N/W mode " + nwMode);
            return FAILURE;
        }
        int nwModeRaf = RadioAccessFamily.getRafFromNetworkType(nwMode);
        int supportedRafMaskForNwMode = radioAccessFamily & nwModeRaf;
        logv("getNumOfRATsSupportedForNwMode: nwMode[" + nwMode + " nwModeRaf = " + nwModeRaf + "] raf = " + radioAccessFamily + " supportedRafMaskForNwMode:" + supportedRafMaskForNwMode);
        return Integer.bitCount(supportedRafMaskForNwMode);
    }

    private void sendSubscriptionSettings(int phoneId) {
        int type = getNetworkModeFromDB(phoneId);
        this.mCi[phoneId].setPreferredNetworkType(type, null);
        Phone phone = ((PhoneProxy) this.mPhone[phoneId]).getActivePhone();
        ((PhoneBase) phone).restoreSavedNetworkSelection(null);
        int ddsSubId = this.mQtiSubscriptionController.getDefaultDataSubId();
        int ddsPhoneId = this.mQtiSubscriptionController.getPhoneId(ddsSubId);
        if (phoneId == ddsPhoneId) {
            QtiDctController dctCntrl = (QtiDctController) QtiDctController.getInstance();
            if (dctCntrl != null) {
                dctCntrl.informDdsToRil(ddsSubId);
            }
            this.mCi[phoneId].setDataAllowed(DBG, null);
        }
        if (this.mQtiSubscriptionController.isActiveSubId(this.mQtiSubscriptionController.getSubId(phoneId)[FAILURE])) {
            ((PhoneBase) phone).mDcTracker.setDataEnabled(((PhoneBase) phone).mDcTracker.getDataEnabled());
        }
        logi("sendSubscriptionSettings: nwMode[" + phoneId + "] = " + type + " dds subId[" + ddsPhoneId + "] = " + ddsSubId);
    }

    private void notifyRadioCapsUpdated(boolean isCrossMapDone) {
        logd("notifyRadioCapsUpdated: radio caps updated " + isCrossMapDone);
        if (isCrossMapDone) {
            for (int i = FAILURE; i < mNumPhones; i += SUCCESS) {
                this.mCurrentStackId[i] = this.mPreferredStackId[i];
            }
        }
        this.mContext.sendStickyBroadcastAsUser(new Intent("org.codeaurora.intent.action.ACTION_RADIO_CAPABILITY_UPDATED"), UserHandle.ALL);
    }

    private void sendResponseToTarget(Message response, int responseCode) {
        if (response != null) {
            AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
            response.sendToTarget();
        }
    }

    private boolean isAnyCallsInProgress() {
        for (int i = FAILURE; i < mNumPhones; i += SUCCESS) {
            if (this.mPhone[i].getState() != State.IDLE) {
                return DBG;
            }
        }
        return false;
    }

    private boolean isAnyPhoneInEcmState() {
        for (int i = FAILURE; i < mNumPhones; i += SUCCESS) {
            if (this.mPhone[i].isInEcm()) {
                return DBG;
            }
        }
        return false;
    }

    private boolean isUiccProvisionInProgress() {
        UiccCardProvisioner uiccProvisioner = ExtTelephonyServiceImpl.getInstance().getUiccProvisionerInstance();
        if (uiccProvisioner == null) {
            return false;
        }
        boolean retVal = uiccProvisioner.isAnyProvisionRequestInProgress();
        logd("isUiccProvisionInProgress: retVal =  " + retVal);
        return retVal;
    }

    private void setNWModeInProgressFlag(boolean newStatus) {
        synchronized (sSetNwModeLock) {
            this.mIsSetPrefNwModeInProgress = newStatus;
        }
    }

    public boolean isSetNWModeInProgress() {
        boolean retVal;
        synchronized (sSetNwModeLock) {
            retVal = this.mIsSetPrefNwModeInProgress;
        }
        return retVal;
    }

    public void radioCapabilityUpdated(int phoneId, RadioCapability rc) {
        this.mRadioCapability[phoneId] = rc;
        logd(" radioCapabilityUpdated phoneId[" + phoneId + "] rc = " + rc);
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

    private void logv(String string) {
    }
}
