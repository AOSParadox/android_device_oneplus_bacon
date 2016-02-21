package com.qti.internal.telephony;

import android.content.Context;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.util.Log;
import com.android.internal.telephony.IExtTelephony.Stub;

public class ExtTelephonyServiceImpl extends Stub {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "ExtTelephonyServiceImpl";
    private static final String TELEPHONY_SERVICE_NAME = "extphone";
    private static ExtTelephonyServiceImpl sInstance = null;
    private UiccCardProvisioner mCardProvisioner = null;
    private Context mContext;

    public static ExtTelephonyServiceImpl init(Context c) {
        ExtTelephonyServiceImpl extTelephonyServiceImpl;
        synchronized (ExtTelephonyServiceImpl.class) {
            if (sInstance == null) {
                sInstance = new ExtTelephonyServiceImpl(c);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            extTelephonyServiceImpl = sInstance;
        }
        return extTelephonyServiceImpl;
    }

    public static ExtTelephonyServiceImpl getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    private ExtTelephonyServiceImpl(Context c) {
        logd("init constructor ");
        this.mContext = c;
        this.mCardProvisioner = new UiccCardProvisioner(c);
        if (ServiceManager.getService(TELEPHONY_SERVICE_NAME) == null) {
            ServiceManager.addService(TELEPHONY_SERVICE_NAME, this);
        }
    }

    public int getCurrentUiccCardProvisioningStatus(int slotId) {
        return this.mCardProvisioner.getCurrentUiccCardProvisioningStatus(slotId);
    }

    public int getUiccCardProvisioningUserPreference(int slotId) {
        return this.mCardProvisioner.getUiccCardProvisioningUserPreference(slotId);
    }

    public int activateUiccCard(int slotId) {
        return this.mCardProvisioner.activateUiccCard(slotId);
    }

    public int deactivateUiccCard(int slotId) {
        return this.mCardProvisioner.deactivateUiccCard(slotId);
    }

    public boolean isSMSPromptEnabled() {
        if (QtiSubscriptionController.getInstance() == null) {
            Log.wtf(LOG_TAG, "QtiSubscriptionController getInstance is null");
        }
        return QtiSubscriptionController.getInstance().isSMSPromptEnabled();
    }

    public void setSMSPromptEnabled(boolean enabled) {
        if (QtiSubscriptionController.getInstance() == null) {
            Log.wtf(LOG_TAG, "QtiSubscriptionController getInstance is null");
        }
        QtiSubscriptionController.getInstance().setSMSPromptEnabled(enabled);
    }

    public UiccCardProvisioner getUiccProvisionerInstance() {
        return this.mCardProvisioner;
    }

    public int getPhoneIdForECall() {
        return QtiEmergencyCallHelper.getPhoneIdForECall();
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }
}
