package com.qti.internal.telephony.dataconnection;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneProxy;
import com.android.internal.telephony.dataconnection.DcSwitchAsyncChannel.RequestInfo;
import com.android.internal.telephony.dataconnection.DctController;
import com.qti.internal.telephony.ExtTelephonyServiceImpl;
import com.qti.internal.telephony.QtiRilInterface;
import com.qualcomm.qcrilhook.QmiOemHookConstants;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes;

public class QtiDctController extends DctController {
    private static final int DCTCONTROLLER_EVENT_BASE = 200;
    private static final int EVENT_CONNECT_RESPONSE = 201;
    private final int ACTIVE = 1;
    private final int ALLOW_DATA_RETRY_DELAY = 30000;
    private final int DO_CONNECT_ON_PHONEID = 0;
    private final int DO_DISCONNECT_ON_ACTIVE_PHONEID = 2;
    private final int DO_DISCONNECT_ON_PHONEID = 1;
    private final int FALSE = 1;
    private final int INACTIVE = 0;
    private final int MANUAL_DDS_SWITCH_DSDA = 1;
    private final int MANUAL_DDS_SWITCH_DSDS = 0;
    private final int MAX_CONNECT_FAILURE_COUNT = 5;
    private final int MODEM_DATA_CAPABILITY_UNKNOWN = -1;
    private final int MODEM_DUAL_DATA_CAPABLE = 2;
    private final int MODEM_SINGLE_DATA_CAPABLE = 1;
    private final int NOP = 3;
    private final int ON_DEMAND_REQ_DSDA = 3;
    private final int ON_DEMAND_REQ_DSDS = 2;
    private final String OVERRIDE_MODEM_DUAL_DATA_CAP_PROP = "persist.radio.msim.data.cap";
    private final int TRUE = 0;
    private int[] mAllowDataFailure;
    private final int[][] mDcSwitchStateMachineActionTable = new int[][]{new int[]{0, 3}, new int[]{0, 1}};
    private final int[][] mDdsSwitchActionTable = new int[][]{new int[]{0, 0}, new int[]{0, 2}, new int[]{2, 0}};
    private int mPreviousPhoneId = -1;

    protected QtiDctController(PhoneProxy[] phones) {
        super(phones);
        LOG_TAG = "QtiDctController";
        this.mAllowDataFailure = new int[this.mPhoneNum];
    }

    public static DctController makeDctController(PhoneProxy[] phones) {
        if (sDctController == null) {
            logd("makeDctController: new QtiDctController phones.length=" + phones.length);
            sDctController = new QtiDctController(phones);
        }
        logd("makeDctController: X sDctController=" + sDctController);
        return sDctController;
    }

    protected void onSettingsChanged() {
        logd("onSettingsChanged");
        int ddsSubId = this.mSubController.getDefaultDataSubId();
        int ddsPhoneId = this.mSubController.getPhoneId(ddsSubId);
        if (this.mSubController.isActiveSubId(ddsSubId) && isUiccProvisioned(ddsPhoneId)) {
            informDdsToRil(ddsSubId);
            super.onSettingsChanged();
            return;
        }
        logd("ddsSubId = " + ddsSubId + " is invalid, ignore.");
    }

    public void handleMessage(Message msg) {
        logd("handleMessage msg=" + msg);
        switch (msg.what) {
            case EVENT_CONNECT_RESPONSE /*201*/:
                onConnectResponse(msg.arg1, (AsyncResult) msg.obj);
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    private void doConnect(int phoneId) {
        logd("doConnect phoneId = " + phoneId);
        if (isUiccProvisioned(phoneId)) {
            int ddsSubId = this.mSubController.getDefaultDataSubId();
            for (Object obj : this.mRequestInfos.keySet()) {
                RequestInfo requestInfo = (RequestInfo) this.mRequestInfos.get(obj);
                // OLD : if (getRequestPhoneId(requestInfo.request) == phoneId && !requestInfo.executedPhoneId) {
		if ( getRequestPhoneId(requestInfo.request) == phoneId &&
		   getRequestPhoneId(requestInfo.request) != requestInfo.executedPhoneId ) {
                    informDdsToRil(ddsSubId);
                    this.mDcSwitchAsyncChannel[phoneId].connect(requestInfo, obtainMessage(EVENT_CONNECT_RESPONSE, phoneId, 0));
                    Phone phone = this.mPhones[phoneId].getActivePhone();
                    if (phone.getPhoneType() == 2 && phone.getServiceState().getDataRegState() == 0) {
                        logd("Active phone is CDMA, fake ATTACH");
                        this.mDcSwitchAsyncChannel[phoneId].notifyDataAttached();
                    }
                }
            }
            return;
        }
        logd("doConnect: phoneId " + phoneId + " is not provisioned, bail out");
    }

    private int getNonActivePhoneId(int activePhoneId) {
        for (int i = 0; i < this.mPhoneNum; i++) {
            if (i != activePhoneId) {
                return i;
            }
        }
        return -1;
    }

    private void doDsdaCleanUp(int activePhoneId) {
        boolean anyRequestExecutingOnOtherSub = false;
        int nonActivePhoneId = getNonActivePhoneId(activePhoneId);
        for (Object obj : this.mRequestInfos.keySet()) {
            if (getRequestPhoneId(((RequestInfo) this.mRequestInfos.get(obj)).request) == nonActivePhoneId) {
                anyRequestExecutingOnOtherSub = true;
                break;
            }
        }
        if (nonActivePhoneId != -1 && !anyRequestExecutingOnOtherSub && !this.mDcSwitchAsyncChannel[nonActivePhoneId].isIdleOrDetachingSync()) {
            logd("doDsdaCleanup on phoneId = " + nonActivePhoneId);
            doDisconnectAll(nonActivePhoneId);
        }
    }

    private void doDisconnectAll(int phoneId) {
        logd("doDisconnectAll phoneId = " + phoneId);
        Phone phone = this.mPhones[phoneId].getActivePhone();
        this.mDcSwitchAsyncChannel[phoneId].disconnectAll();
        if (phone.getPhoneType() == 2) {
            logd("Active phone is CDMA, fake DETACH");
            this.mDcSwitchAsyncChannel[phoneId].notifyDataDetached();
        }
    }

    private int identifyUsecase(int phoneId, int activePhoneId, int ddsPhoneId, int maxDataCap) {
        logd("phoneId = " + phoneId);
        logd("activePhoneId = " + activePhoneId);
        logd("ddsPhoneId = " + ddsPhoneId);
        logd("maxDataCap = " + maxDataCap);
        if (activePhoneId == -1 || activePhoneId == phoneId) {
            if (maxDataCap == 2) {
                logd("Modem is DSDA-Data capable.");
                return 3;
            }
            logd("Modem is DSDS-Data capable.");
            return 2;
        } else if (maxDataCap == 2) {
            logd("Modem is DSDA-Data capable.");
            if (phoneId == ddsPhoneId) {
                logd("DDS switch request identified");
                this.mPreviousPhoneId = activePhoneId;
                return 1;
            }
            logd("Ondemand PS request on non-dds identified");
            return 3;
        } else {
            logd("Modem is DSDS-Data capable.");
            this.mPreviousPhoneId = activePhoneId;
            return 2;
        }
    }

    private void handleDdsSwitch(int phoneId, int activePhoneId) {
        switch (this.mDdsSwitchActionTable[activePhoneId + 1][phoneId]) {
            case 0:
                doConnect(phoneId);
                return;
            case QmiPrimitiveTypes.SIZE_OF_SHORT /*2*/:
                doDisconnectAll(activePhoneId);
                return;
            default:
                return;
        }
    }

    private int getDcSwitchStateMachineActivityState(int phoneId) {
        return this.mDcSwitchAsyncChannel[phoneId].isIdleOrDetachingSync() ? 0 : 1;
    }

    private int isReqPresentForPhoneId(int phoneId) {
        for (Object obj : this.mRequestInfos.keySet()) {
            RequestInfo requestInfo = (RequestInfo) this.mRequestInfos.get(obj);
            logd("selectExecPhone requestInfo = " + requestInfo);
            if (getRequestPhoneId(requestInfo.request) == phoneId) {
                return 0;
            }
        }
        return 1;
    }

    private void processAction(int phoneId) {
        int state = getDcSwitchStateMachineActivityState(phoneId);
        switch (this.mDcSwitchStateMachineActionTable[state][isReqPresentForPhoneId(phoneId)]) {
            case 0:
                doConnect(phoneId);
                return;
            case QmiPrimitiveTypes.SIZE_OF_BYTE /*1*/:
                doDisconnectAll(phoneId);
                return;
            default:
                return;
        }
    }

    private void handleSimultaneousReq(int phoneId) {
        int otherPhoneId = getNonActivePhoneId(phoneId);
        if (otherPhoneId != -1) {
            processAction(otherPhoneId);
        }
        processAction(phoneId);
    }

    protected void onProcessRequest() {
        int phoneId = getTopPriorityRequestPhoneId();
        int activePhoneId = -1;
        int defaultDds = this.mSubController.getDefaultDataSubId();
        int ddsPhoneId = this.mSubController.getPhoneId(defaultDds);
        int maxDataCap = SystemProperties.getInt("persist.radio.msim.data.cap", -1);
        if (this.mSubController.isActiveSubId(defaultDds)) {
            for (int i = 0; i < this.mDcSwitchAsyncChannel.length; i++) {
                if (!this.mDcSwitchAsyncChannel[i].isIdleSync()) {
                    activePhoneId = i;
                    break;
                }
            }
            int usecase = identifyUsecase(phoneId, activePhoneId, ddsPhoneId, maxDataCap);
            switch (usecase) {
                case 0:
                    logd("MANUAL_DDS_SWITCH_DSDS");
                    break;
                case QmiPrimitiveTypes.SIZE_OF_BYTE /*1*/:
                    break;
                case QmiPrimitiveTypes.SIZE_OF_SHORT /*2*/:
                    break;
                case QmiOemHookConstants.SUCCESS_STATUS /*3*/:
                    logd("ON_DEMAND_REQ_DSDA");
                    handleSimultaneousReq(phoneId);
                    return;
                default:
                    logd("Unhandled usecase = " + usecase);
                    return;
            }
            logd("MANUAL_DDS_SWITCH_DSDA");
            logd("ON_DEMAND_REQ_DSDS");
            handleDdsSwitch(phoneId, activePhoneId);
            return;
        }
        logd("onProcessRequest: ddsSubId = " + defaultDds + " is not valid");
    }

    private void resetConnectFailureCount(int phoneId) {
        this.mAllowDataFailure[phoneId] = 0;
    }

    private void incConnectFailureCount(int phoneId) {
        int[] iArr = this.mAllowDataFailure;
        iArr[phoneId] = iArr[phoneId] + 1;
    }

    private int getConnectFailureCount(int phoneId) {
        return this.mAllowDataFailure[phoneId];
    }

    private void enforceDds(int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds != null && subIds.length != 0) {
            logd("enforceDds: subId = " + subIds[0]);
            this.mSubController.setDefaultDataSubId(subIds[0]);
        }
    }

    private void handleConnectMaxFailure(int phoneId) {
        resetConnectFailureCount(phoneId);
        int ddsPhoneId = this.mSubController.getPhoneId(this.mSubController.getDefaultDataSubId());
        if (phoneId != ddsPhoneId) {
            logd("ALLOW_DATA retries exhausted on phoneId = " + phoneId + " enforce DDS setting");
            enforceDds(ddsPhoneId);
        } else if (this.mPreviousPhoneId != -1) {
            logd("DDS switch retries exhausted, reverting back DDS to phoneId= " + this.mPreviousPhoneId);
            enforceDds(this.mPreviousPhoneId);
        }
        this.mDcSwitchAsyncChannel[phoneId].reset();
    }

    private void onConnectResponse(final int phoneId, AsyncResult asyncResult) {
        if (asyncResult.exception != null) {
            incConnectFailureCount(phoneId);
            logd("Allow_data failed on phoneId = " + phoneId + ", failureCount = " + getConnectFailureCount(phoneId));
            if (getConnectFailureCount(phoneId) == 5) {
                handleConnectMaxFailure(phoneId);
                return;
            }
            logd("Scheduling retry connect/allow_data");
            postDelayed(new Runnable() {
                public void run() {
                    QtiDctController.logd("Running retry connect/allow_data");
                    if (phoneId == QtiDctController.this.getTopPriorityRequestPhoneId()) {
                        QtiDctController.this.mDcSwitchAsyncChannel[phoneId].retryConnect();
                    } else {
                        QtiDctController.this.processRequests();
                    }
                }
            }, 30000);
            return;
        }
        logd("Allow_data success on phoneId = " + phoneId);
        resetConnectFailureCount(phoneId);
    }

    private boolean isUiccProvisioned(int phoneId) {
        boolean status = ExtTelephonyServiceImpl.getInstance().getUiccProvisionerInstance().getCurrentUiccCardProvisioningStatus(phoneId) > 0;
        logd("isUiccProvisioned = " + status);
        return status;
    }

    public void informDdsToRil(int ddsSubId) {
        int ddsPhoneId = this.mSubController.getPhoneId(ddsSubId);
        if (ddsPhoneId < 0 || ddsPhoneId >= this.mPhoneNum) {
            logd("InformDdsToRil dds phoneId is invalid = " + ddsPhoneId);
            return;
        }
        QtiRilInterface qtiRilInterface = QtiRilInterface.getInstance(this.mContext);
        if (qtiRilInterface.isServiceReady()) {
            for (int i = 0; i < this.mPhoneNum; i++) {
                logd("InformDdsToRil rild= " + i + ", DDS phoneId=" + ddsPhoneId);
                qtiRilInterface.qcRilSendDDSInfo(ddsPhoneId, i);
            }
            return;
        }
        logd("Oem hook service is not ready yet");
    }
}
