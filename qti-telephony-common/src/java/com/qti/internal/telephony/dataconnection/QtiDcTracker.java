package com.qti.internal.telephony.dataconnection;

import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Telephony.Carriers;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public final class QtiDcTracker extends DcTracker {
    private static final int EVENT_3GPP_RECORDS_LOADED = 100;
    private final int EVENT_MODEM_DATA_PROFILE_READY = 1001;
    private String LOG_TAG = "QtiDCT";
    private String OMH_FEATURE_ENABLE_OVERRIDE = "persist.radio.omh.enable";
    private final int QTI_DCT_EVENTS_BASE = 1000;
    private QtiCdmaApnProfileTracker mOmhApt;
    protected AtomicReference<IccRecords> mSimRecords = new AtomicReference();
    Handler mSimRecordsLoadedHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (!QtiDcTracker.this.mPhone.mIsTheCurrentActivePhone || QtiDcTracker.this.mIsDisposed) {
                QtiDcTracker.this.loge("Sim handler handleMessage: Ignore msgs since phone is inactive");
                return;
            }
            switch (msg.what) {
                case QtiDcTracker.EVENT_3GPP_RECORDS_LOADED /*100*/:
                    QtiDcTracker.this.log("EVENT_3GPP_RECORDS_LOADED");
                    QtiDcTracker.this.setInitialAttachApn();
                    return;
                default:
                    return;
            }
        }
    };

    public QtiDcTracker(PhoneBase p) {
        super(p);
        if (p.getPhoneType() == 1) {
            this.LOG_TAG = "QtiGsmDCT";
        } else if (p.getPhoneType() == 2) {
            this.LOG_TAG = "QtiCdmaDCT";
        } else {
            this.LOG_TAG = "DCT";
            loge("unexpected phone type [" + p.getPhoneType() + "]");
        }
        log(this.LOG_TAG + ".constructor");
        if (p.getPhoneType() == 2) {
            boolean fetchApnFromOmhCard = p.getContext().getResources().getBoolean(17957037);
            log(this.LOG_TAG + " fetchApnFromOmhCard: " + fetchApnFromOmhCard);
            boolean featureOverride = SystemProperties.getBoolean(this.OMH_FEATURE_ENABLE_OVERRIDE, false);
            if (featureOverride) {
                log(this.LOG_TAG + "OMH: feature-config override enabled");
                fetchApnFromOmhCard = featureOverride;
            }
            if (fetchApnFromOmhCard) {
                this.mOmhApt = new QtiCdmaApnProfileTracker((CDMAPhone) p);
                this.mOmhApt.registerForModemProfileReady(this, 1001, null);
            }
        }
    }

    public void dispose() {
        super.dispose();
        if (this.mOmhApt != null) {
            this.mOmhApt.unregisterForModemProfileReady(this);
        }
        IccRecords r = (IccRecords) this.mSimRecords.get();
        if (r != null) {
            r.unregisterForRecordsLoaded(this.mSimRecordsLoadedHandler);
            this.mSimRecords.set(null);
        }
    }

    protected void cleanUpConnection(boolean tearDown, ApnContext apnContext) {
        super.cleanUpConnection(tearDown, apnContext);
        if (this.mOmhApt != null) {
            this.mOmhApt.clearActiveApnProfile();
        }
    }

    private void onModemApnProfileReady() {
        if (this.mState == State.FAILED) {
            cleanUpAllConnections(false, "psRestrictEnabled");
        }
        log("OMH: onModemApnProfileReady(): Setting up data call");
        tryRestartDataConnections("apnChanged");
    }

    protected void onRecordsLoaded() {
        log("onRecordsLoaded: createAllApnList");
        this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17957010);
        if (this.mOmhApt != null) {
            log("OMH: onRecordsLoaded(): calling loadProfiles()");
            this.mOmhApt.loadProfiles();
            if (this.mPhone.mCi.getRadioState().isOn()) {
                log("OMH: onRecordsLoaded: notifying data availability");
                notifyOffApnsOfAvailability("simLoaded");
                return;
            }
            return;
        }
        createAllApnList();
        if (this.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoaded: notifying data availability");
            notifyOffApnsOfAvailability("simLoaded");
        }
        setupDataOnConnectableApns("simLoaded");
    }

    protected void createAllApnList() {
        this.mAllApnSettings = new ArrayList();
        String operator = getOperatorNumeric();
        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        if (!(this.mOmhApt == null || !ServiceState.isCdma(radioTech) || 13 == radioTech)) {
            ArrayList<QtiApnSetting> mOmhApnsList = new ArrayList();
            mOmhApnsList = this.mOmhApt.getOmhApnProfilesList();
            if (!mOmhApnsList.isEmpty()) {
                log("createAllApnList: Copy Omh profiles");
                this.mAllApnSettings.addAll(mOmhApnsList);
            }
        }
        if (!(!this.mAllApnSettings.isEmpty() || operator == null || operator.isEmpty())) {
            String selection = "numeric = '" + operator + "'";
            log("createAllApnList: selection=" + selection);
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, "_id");
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    this.mAllApnSettings = createApnList(cursor, (IccRecords) this.mIccRecords.get());
                }
                cursor.close();
            }
        }
        addEmergencyApnSetting();
        dedupeApnSettings();
        if (this.mAllApnSettings.isEmpty() && isDummyProfileNeeded()) {
            addDummyApnSettings(operator);
        }
        if (this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: No APN found for carrier: " + operator);
            this.mPreferredApn = null;
        } else {
            this.mPreferredApn = getPreferredApn(this.mAllApnSettings);
            if (!(this.mPreferredApn == null || this.mPreferredApn.numeric.equals(operator))) {
                this.mPreferredApn = null;
                setPreferredApn(-1);
            }
            log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
        }
        log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
        setDataProfilesAsNeeded();
    }

    public void handleMessage(Message msg) {
        log("QtiDcTracker handleMessage msg=" + msg);
        if (!this.mPhone.mIsTheCurrentActivePhone || this.mIsDisposed) {
            loge("handleMessage: Ignore GSM msgs since GSM phone is inactive");
            return;
        }
        switch (msg.what) {
            case 1001:
                onModemApnProfileReady();
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    protected boolean onUpdateIcc() {
        updateSimRecords();
        return super.onUpdateIcc();
    }

    @Override
    protected void setInitialAttachApn() {
        ArrayList<ApnSetting> apnList = create3gppApnsList();
        setInitialAttachApn(apnList, getPreferredApn(apnList));
    }

    private void updateSimRecords() {
        if (this.mUiccController != null && this.mSimRecords != null) {
            IccRecords newSimRecords = getUiccRecords(1);
            log("updateSimRecords: newSimRecords = " + newSimRecords);
            IccRecords r = (IccRecords) this.mSimRecords.get();
            if (r != newSimRecords) {
                if (r != null) {
                    log("Removing stale sim objects.");
                    r.unregisterForRecordsLoaded(this.mSimRecordsLoadedHandler);
                    this.mSimRecords.set(null);
                }
                if (newSimRecords != null) {
                    log("New sim records found");
                    this.mSimRecords.set(newSimRecords);
                    newSimRecords.registerForRecordsLoaded(this.mSimRecordsLoadedHandler, EVENT_3GPP_RECORDS_LOADED, null);
                }
            }
        }
    }

    private ArrayList<ApnSetting> create3gppApnsList() {
        ArrayList<ApnSetting> apnsList = null;
        IccRecords r = (IccRecords) this.mSimRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (!TextUtils.isEmpty(operator)) {
            String selection = ("numeric = '" + operator + "'") + " and carrier_enabled = 1";
            log("create3gppApnList: selection=" + selection);
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    apnsList = createApnList(cursor, r);
                }
                cursor.close();
            }
        }
        return apnsList;
    }

    protected void log(String s) {
        Rlog.d(this.LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }
}
