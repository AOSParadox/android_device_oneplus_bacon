package com.qti.internal.telephony.cdma;

import android.content.Context;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.RadioCapability;
import com.android.internal.telephony.cdma.CDMALTEPhone;
import com.qti.internal.telephony.QtiRadioCapabilityController;
import com.qti.internal.telephony.QtiSubscriptionController;

public class QtiCDMALTEPhone extends CDMALTEPhone {
    private static final String LOG_TAG = "QtiCDMALTEPhone";

    public QtiCDMALTEPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId) {
        super(context, ci, notifier, phoneId);
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        QtiRadioCapabilityController radioCapController = QtiRadioCapabilityController.getInstance();
        if (radioCapController != null) {
            radioCapController.setPreferredNetworkType(getPhoneId(), networkType, response);
        } else {
            Rlog.e(LOG_TAG, " Error: Received null QtiRadioCapabilityController instante ");
        }
    }

    public void radioCapabilityUpdated(RadioCapability rc) {
        this.mRadioCapability.set(rc);
        QtiRadioCapabilityController radioCapController = QtiRadioCapabilityController.getInstance();
        if (radioCapController != null) {
            radioCapController.radioCapabilityUpdated(getPhoneId(), rc);
        }
    }

    public void shutdownRadio() {
        super.shutdownRadio();
        if (QtiSubscriptionController.getInstance() != null) {
            Rlog.i(LOG_TAG, "Shutdown Radio request");
            QtiSubscriptionController.getInstance().shutdownRequestReceived();
            return;
        }
        Rlog.w(LOG_TAG, "QtiSubscriptionController instance is null");
    }
}
