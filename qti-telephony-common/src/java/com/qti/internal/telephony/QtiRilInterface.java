package com.qti.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.util.Log;
import com.qti.internal.telephony.dataconnection.QtiApnProfileOmh;
import com.qti.internal.telephony.dataconnection.QtiApnSetting;
import com.qti.internal.telephony.UiccCardProvisioner.UiccProvisionStatus;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qcrilhook.OemHookCallback;
import com.qualcomm.qcrilhook.QcRilHook;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class QtiRilInterface {
    private static final int BYTE_SIZE = 1;
    private static final int INT_SIZE = 4;
    private static final String LOG_TAG = "QtiRilInterface";
    private static boolean mIsServiceReady = false;
    private static QtiRilInterface sInstance = null;
    private String OMH_FAKE_QCRIL_HOOK_RESPONSE = "persist.test.omh.fakeprofile";
    private QcRilHook mQcRilHook;
    private QcRilHookCallback mQcrilHookCb = new QcRilHookCallback() {
        public void onQcRilHookReady() {
            QtiRilInterface.mIsServiceReady = true;
            QtiRilInterface.this.logd("Service ready, notifying registrants");
            QtiRilInterface.this.mServiceReadyRegistrantList.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(QtiRilInterface.mIsServiceReady), null));
        }

        public synchronized void onQcRilHookDisconnected() {
            QtiRilInterface.mIsServiceReady = false;
            QtiRilInterface.this.logd("Service disconnected, notifying registrants");
            QtiRilInterface.this.mServiceReadyRegistrantList.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(QtiRilInterface.mIsServiceReady), null));
        }
    };
    private RegistrantList mServiceReadyRegistrantList;

    private class OmhCallProfileCallback extends OemHookCallback {
        Message mAppMessage;
        int mModemApnType;

        public OmhCallProfileCallback(int modemApnType, Message msg) {
            super(msg);
            this.mAppMessage = msg;
            this.mModemApnType = modemApnType;
        }

        public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
            if (SystemProperties.getBoolean(QtiRilInterface.this.OMH_FAKE_QCRIL_HOOK_RESPONSE, false)) {
                QtiRilInterface.this.logi("Getting fake omh profiles");
                AsyncResult.forMessage(this.mAppMessage, QtiRilInterface.this.getFakeOmhProfiles(this.mModemApnType), null);
            } else if (response != null) {
                Log.d(QtiRilInterface.LOG_TAG, "getOmhCallProfile: onOemHookResponse = " + response.toString());
                AsyncResult.forMessage(this.mAppMessage, QtiRilInterface.this.parseOmhProfiles(response), null);
            } else {
                AsyncResult.forMessage(this.mAppMessage, null, new Exception("QCRIL_EVT_HOOK_GET_OMH_CALL_PROFILE failed"));
            }
            this.mAppMessage.sendToTarget();
        }
    }

    public static synchronized QtiRilInterface getInstance(Context context) {
        QtiRilInterface qtiRilInterface;
        synchronized (QtiRilInterface.class) {
            if (sInstance == null) {
                sInstance = new QtiRilInterface(context);
            } else {
                Log.wtf(LOG_TAG, "instance = " + sInstance);
            }
            qtiRilInterface = sInstance;
        }
        return qtiRilInterface;
    }

    private QtiRilInterface(Context context) {
        logd(" in constructor ");
        this.mServiceReadyRegistrantList = new RegistrantList();
        this.mQcRilHook = new QcRilHook(context, this.mQcrilHookCb);
    }

    public UiccProvisionStatus getUiccProvisionPreference(int phoneId) {
        UiccProvisionStatus provStatus = new UiccProvisionStatus();
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_GET_UICC_PROVISION_PREFERENCE, new byte[0], phoneId);
        if (ar.exception == null && ar.result != null) {
            ByteBuffer byteBuf = ByteBuffer.wrap((byte[]) ar.result);
            byteBuf.order(ByteOrder.nativeOrder());
            logd("Data received: " + byteBuf.toString());
            provStatus.setUserPreference(byteBuf.getInt());
            provStatus.setCurrentState(byteBuf.getInt());
        }
        logi("get pref, phoneId " + phoneId + " " + provStatus + " exception " + ar.exception);
        return provStatus;
    }

    private ArrayList<QtiApnSetting> parseOmhProfiles(byte[] buffer) {
        ArrayList<QtiApnSetting> profilesList = null;
        ByteBuffer byteBuf = ByteBuffer.wrap(buffer);
        if (byteBuf != null) {
            byteBuf.order(ByteOrder.nativeOrder());
            logi("Data received: " + byteBuf.toString());
            int nProfiles = byteBuf.getInt();
            for (int i = 0; i < nProfiles; i += BYTE_SIZE) {
                int profileId = byteBuf.getInt();
                int priority = byteBuf.getInt();
                QtiApnProfileOmh profile = new QtiApnProfileOmh(profileId, priority);
                logi("getOmhCallProfile " + profileId + ":" + priority);
                profilesList.add(profile);
            }
        }
        return profilesList;
    }

    private ArrayList<QtiApnSetting> getFakeOmhProfiles(int profileId) {
        int[] prioritySortedProfiles = new int[]{2, 32, 64, BYTE_SIZE};
        ArrayList<QtiApnSetting> profilesList = new ArrayList();
        for (int i = 0; i < prioritySortedProfiles.length; i += BYTE_SIZE) {
            if (prioritySortedProfiles[i] == profileId) {
                QtiApnProfileOmh profile = new QtiApnProfileOmh(prioritySortedProfiles[i], i);
                logi("profile(id=" + profileId + ") =" + profile);
                profilesList.add(profile);
                break;
            }
        }
        return profilesList;
    }

    public void getOmhCallProfile(int modemApnType, Message callbackMsg, int phoneId) {
        logi("getOmhCallProfile()");
        byte[] requestData = new byte[INT_SIZE];
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.createBufferWithNativeByteOrder(requestData).putInt(modemApnType);
        this.mQcRilHook.sendQcRilHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_GET_OMH_CALL_PROFILE, requestData, new OmhCallProfileCallback(modemApnType, callbackMsg), phoneId);
    }

    public String getUiccIccId(int phoneId) {
        String iccId = null;
        byte[] requestData = new byte[INT_SIZE];
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.createBufferWithNativeByteOrder(requestData).putInt(phoneId);
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_GET_UICC_ICCID, requestData, phoneId);
        if (ar.exception == null && ar.result != null) {
            iccId = new String((byte[]) ar.result);
        }
        logi("getUiccIccId iccId[" + phoneId + "] = " + iccId + " exception: " + ar.exception);
        return iccId;
    }

    public void sendPhoneStatus(int isReady, int phoneId) {
        byte[] requestData = new byte[BYTE_SIZE];
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.createBufferWithNativeByteOrder(requestData).put((byte) isReady);
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_ATEL_UI_STATUS, requestData, phoneId);
    }

    public boolean setUiccProvisionPreference(int userPref, int phoneId) {
        boolean retval = false;
        byte[] requestData = new byte[8];
        QcRilHook qcRilHook = this.mQcRilHook;
        ByteBuffer reqBuffer = QcRilHook.createBufferWithNativeByteOrder(requestData);
        reqBuffer.putInt(userPref);
        reqBuffer.putInt(phoneId);
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_UICC_PROVISION_PREFERENCE, requestData, phoneId);
        if (ar.exception == null) {
            retval = true;
        }
        logi("set provision userPref " + userPref + " phoneId " + phoneId + " exception: " + ar.exception);
        return retval;
    }

    public boolean isServiceReady() {
        return mIsServiceReady;
    }

    public void registerForUnsol(Handler handler, int event, Object obj) {
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.register(handler, event, obj);
    }

    public void registerForServiceReadyEvent(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mServiceReadyRegistrantList.add(r);
        if (isServiceReady()) {
            r.notifyRegistrant(new AsyncResult(null, Boolean.valueOf(mIsServiceReady), null));
        }
    }

    public void qcRilSendDDSInfo(int ddsPhoneId, int rilId) {
        this.mQcRilHook.qcRilSendDDSInfo(ddsPhoneId, rilId);
    }

    public void unRegisterForServiceReadyEvent(Handler h) {
        this.mServiceReadyRegistrantList.remove(h);
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
