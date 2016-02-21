package com.qti.internal.telephony;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SubscriptionController;
import java.util.Iterator;
import java.util.List;

public class QtiSubscriptionController extends SubscriptionController {
    private static final String APM_SIM_NOT_PWDN_PROPERTY = "persist.radio.apm_sim_not_pwdn";
    private static final int DUMMY_SUB_ID_BASE = 2147483643;
    static final String LOG_TAG = "QtiSubscriptionController";
    private static final int NOT_PROVISIONED = 0;
    private static final int PROVISIONED = 1;
    private static CommandsInterface[] sCi = null;
    private static int sNumPhones;
    private boolean mIsShutDownInProgress = false;
    private TelecomManager mTelecomManager;
    private TelephonyManager mTelephonyManager;

    public static QtiSubscriptionController init(Context c, CommandsInterface[] ci) {
        QtiSubscriptionController qtiSubscriptionController;
        synchronized (QtiSubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new QtiSubscriptionController(c);
                sCi = ci;
                sNumPhones = TelephonyManager.getDefault().getPhoneCount();
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            qtiSubscriptionController = (QtiSubscriptionController) sInstance;
        }
        return qtiSubscriptionController;
    }

    public static QtiSubscriptionController getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return (QtiSubscriptionController) sInstance;
    }

    private QtiSubscriptionController(Context c) {
        super(c);
        logd(" init by Context");
        mDefaultPhoneId = NOT_PROVISIONED;
        mDefaultFallbackSubId = DUMMY_SUB_ID_BASE;
        this.mTelecomManager = TelecomManager.from(this.mContext);
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
    }

    private void clearVoiceSubId() {
        List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        logdl("[clearVoiceSubId] records: " + records);
        if (shouldDefaultBeCleared(records, getDefaultVoiceSubId())) {
            logdl("[clearVoiceSubId] clear voice sub id");
            setDefaultVoiceSubId(DUMMY_SUB_ID_BASE);
        }
    }

    public int getSlotId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("[getSlotId]- subId invalid");
            return -1;
        } else if (subId < DUMMY_SUB_ID_BASE) {
            return super.getSlotId(subId);
        } else {
            logd("getPhoneId, received dummy subId " + subId);
            return getPhoneIdFromDummySubId(subId);
        }
    }

    public int getPhoneId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
            logdl("[getPhoneId] asked for default subId=" + subId);
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logdl("[getPhoneId]- invalid subId return=-1");
            return -1;
        } else if (subId < DUMMY_SUB_ID_BASE) {
            return super.getPhoneId(subId);
        } else {
            logd("getPhoneId, received dummy subId " + subId);
            return getPhoneIdFromDummySubId(subId);
        }
    }

    private int getPhoneIdFromDummySubId(int subId) {
        return subId - DUMMY_SUB_ID_BASE;
    }

    protected int[] getDummySubIds(int slotIdx) {
        int numSubs = getActiveSubInfoCountMax();
        if (numSubs <= 0) {
            return null;
        }
        int[] iArr = new int[numSubs];
        for (int i = NOT_PROVISIONED; i < numSubs; i += PROVISIONED) {
            iArr[i] = DUMMY_SUB_ID_BASE + slotIdx;
        }
        return iArr;
    }

    public void clearDefaultsForInactiveSubIds() {
        enforceModifyPhoneState("clearDefaultsForInactiveSubIds");
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            logdl("[clearDefaultsForInactiveSubIds] records: " + records);
            if (shouldDefaultBeCleared(records, getDefaultDataSubId())) {
                logd("[clearDefaultsForInactiveSubIds] clearing default data sub id");
                setDefaultDataSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultSmsSubId())) {
                logdl("[clearDefaultsForInactiveSubIds] clearing default sms sub id");
                setDefaultSmsSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultVoiceSubId())) {
                logdl("[clearDefaultsForInactiveSubIds] clearing default voice sub id");
                setDefaultVoiceSubId(DUMMY_SUB_ID_BASE);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected boolean shouldDefaultBeCleared(List<SubscriptionInfo> records, int subId) {
        logdl("[shouldDefaultBeCleared: subId] " + subId);
        if (records == null) {
            logdl("[shouldDefaultBeCleared] return true no records subId=" + subId);
            return true;
        } else if (SubscriptionManager.isValidSubscriptionId(subId)) {
            for (SubscriptionInfo record : records) {
                int id = record.getSubscriptionId();
                logdl("[shouldDefaultBeCleared] Record.id: " + id);
                if (id == subId) {
                    logdl("[shouldDefaultBeCleared] return false subId is active, subId=" + subId);
                    return false;
                }
            }
            if (getUiccProvisionStatus(getSlotId(subId)) == PROVISIONED) {
                logdl("[shouldDefaultBeCleared] return false subId is provisioned, subId=" + subId);
                return false;
            }
            logdl("[shouldDefaultBeCleared] return true not active subId=" + subId);
            return true;
        } else {
            logdl("[shouldDefaultBeCleared] return false only one subId, subId=" + subId);
            return false;
        }
    }

    private boolean isRadioAvailableOnAllSubs() {
        int i = NOT_PROVISIONED;
        while (i < sNumPhones) {
            if (sCi != null && !sCi[i].getRadioState().isAvailable()) {
                return false;
            }
            i += PROVISIONED;
        }
        return true;
    }

    private boolean isUpdateUserPrefsRequired() {
        boolean isApmSimNotPwrDown;
        if (SystemProperties.getInt(APM_SIM_NOT_PWDN_PROPERTY, NOT_PROVISIONED) == PROVISIONED) {
            isApmSimNotPwrDown = true;
        } else {
            isApmSimNotPwrDown = false;
        }
        int isAPMOn = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", NOT_PROVISIONED);
        if (isAPMOn == PROVISIONED && !isApmSimNotPwrDown) {
            logd("isUpdateUserPrefsRequired, isApmSimNotPwrDown = " + isApmSimNotPwrDown + ", isAPMOn:" + isAPMOn);
            return false;
        } else if (!isRadioAvailableOnAllSubs()) {
            logd(" isUpdateUserPrefsRequired, radio not available");
            return false;
        } else if (!this.mIsShutDownInProgress) {
            return true;
        } else {
            logd(" mIsShutDownInProgress: " + this.mIsShutDownInProgress);
            return false;
        }
    }

    synchronized void updateUserPreferences() {
        SubscriptionInfo mNextActivatedSub = null;
        int activeCount = NOT_PROVISIONED;
        if (isUpdateUserPrefsRequired()) {
            List<SubscriptionInfo> sil = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (sil == null || sil.size() < PROVISIONED) {
                logi("updateUserPreferences: Subscription list is empty");
                clearVoiceSubId();
                mDefaultFallbackSubId = DUMMY_SUB_ID_BASE;
            } else if (SystemProperties.getBoolean("persist.radio.aosp_usr_pref_sel", false)) {
                logi("updateUserPreferences: AOSP user preference option enabled ");
            } else {
                for (SubscriptionInfo subInfo : sil) {
                    if (getUiccProvisionStatus(subInfo.getSimSlotIndex()) == PROVISIONED) {
                        activeCount += PROVISIONED;
                        if (mNextActivatedSub == null) {
                            mNextActivatedSub = subInfo;
                        }
                    }
                }
                logd("updateUserPreferences:: active sub count = " + activeCount + " dds = " + getDefaultDataSubId() + " voice = " + getDefaultVoiceSubId() + " sms = " + getDefaultSmsSubId());
                if (activeCount == PROVISIONED) {
                    setSMSPromptEnabled(false);
                }
                if (!(mNextActivatedSub == null || getActiveSubInfoCountMax() == PROVISIONED)) {
                    if (!isSubProvisioned(getDefaultDataSubId())) {
                        setDefaultDataSubId(mNextActivatedSub.getSubscriptionId());
                    }
                    if (!isSubProvisioned(getDefaultSmsSubId())) {
                        setDefaultSmsSubId(mNextActivatedSub.getSubscriptionId());
                    }
                    if (!isSubProvisioned(getDefaultVoiceSubId())) {
                        setDefaultVoiceSubId(mNextActivatedSub.getSubscriptionId());
                    }
                    if (!isNonSimAccountFound() && activeCount == PROVISIONED) {
                        int subId = mNextActivatedSub.getSubscriptionId();
                        PhoneAccountHandle phoneAccountHandle = subscriptionIdToPhoneAccountHandle(subId);
                        logi("set default phoneaccount to  " + subId);
                        this.mTelecomManager.setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
                    }
                    if (!isSubProvisioned(mDefaultFallbackSubId)) {
                        setDefaultFallbackSubId(mNextActivatedSub.getSubscriptionId());
                    }
                    logd("updateUserPreferences: after currentDds = " + getDefaultDataSubId() + " voice = " + getDefaultVoiceSubId() + " sms = " + getDefaultSmsSubId());
                }
            }
        } else {
            logd("Invalid use case, Ignore Updating User Preference!!!");
        }
    }

    private int getUiccProvisionStatus(int slotId) {
        if (ExtTelephonyServiceImpl.getInstance() != null) {
            return ExtTelephonyServiceImpl.getInstance().getCurrentUiccCardProvisioningStatus(slotId);
        }
        return NOT_PROVISIONED;
    }

    private boolean isSubProvisioned(int subId) {
        boolean isSubIdUsable = SubscriptionManager.isUsableSubIdValue(subId);
        if (!isSubIdUsable) {
            return isSubIdUsable;
        }
        int slotId = getSlotId(subId);
        if (!SubscriptionManager.isValidSlotId(slotId) || subId >= DUMMY_SUB_ID_BASE) {
            loge(" Invalid slotId " + slotId + " or subId = " + subId);
            return false;
        }
        if (getUiccProvisionStatus(slotId) != PROVISIONED) {
            isSubIdUsable = false;
        }
        loge("isSubProvisioned, state = " + isSubIdUsable + " subId = " + subId);
        return isSubIdUsable;
    }

    public boolean isSMSPromptEnabled() {
        int value = NOT_PROVISIONED;
        try {
            value = Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        return value != 0;
    }

    public void setSMSPromptEnabled(boolean enabled) {
        enforceModifyPhoneState("setSMSPromptEnabled");
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms_prompt", !enabled ? NOT_PROVISIONED : PROVISIONED);
        logi("setSMSPromptOption to " + enabled);
    }

    private boolean isNonSimAccountFound() {
        Iterator<PhoneAccountHandle> phoneAccounts = this.mTelecomManager.getCallCapablePhoneAccounts().listIterator();
        while (phoneAccounts.hasNext()) {
            if (this.mTelephonyManager.getSubIdForPhoneAccount(this.mTelecomManager.getPhoneAccount((PhoneAccountHandle) phoneAccounts.next())) == -1) {
                logi("Other than SIM account found. ");
                return true;
            }
        }
        logi("Other than SIM account not found ");
        return false;
    }

    private PhoneAccountHandle subscriptionIdToPhoneAccountHandle(int subId) {
        Iterator<PhoneAccountHandle> phoneAccounts = this.mTelecomManager.getCallCapablePhoneAccounts().listIterator();
        while (phoneAccounts.hasNext()) {
            PhoneAccountHandle phoneAccountHandle = (PhoneAccountHandle) phoneAccounts.next();
            if (subId == this.mTelephonyManager.getSubIdForPhoneAccount(this.mTelecomManager.getPhoneAccount(phoneAccountHandle))) {
                return phoneAccountHandle;
            }
        }
        return null;
    }

    public void shutdownRequestReceived() {
        this.mIsShutDownInProgress = true;
        logi("ShutDown in Progress. ");
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
