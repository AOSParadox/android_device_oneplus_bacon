package com.qti.internal.telephony;

import android.content.Context;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.PhoneProxy;
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.TelephonyPluginBase;
import com.android.internal.telephony.TelephonyPluginInterface;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.DctController;
import com.qti.internal.telephony.cdma.QtiCDMALTEPhone;
import com.qti.internal.telephony.dataconnection.QtiDcTracker;
import com.qti.internal.telephony.dataconnection.QtiDctController;
import com.qti.internal.telephony.gsm.QtiGSMPhone;

public class QtiTelephonyPlugin extends TelephonyPluginBase implements TelephonyPluginInterface {
    private String TAG = "QtiTelephonyPlugin";
    QtiRadioCapabilityController mQtiRadioCapabilityController = null;

    public void makeDefaultPhones(Context context) {
        super.makeDefaultPhones(context);
    }

    public DctController makeDctController(PhoneProxy[] phones) {
        return QtiDctController.makeDctController(phones);
    }

    public void initSubscriptionController(Context context, CommandsInterface[] commandsInterfaces) {
        QtiSubscriptionController.init(context, commandsInterfaces);
    }

    public SubscriptionInfoUpdater makeSubscriptionInfoUpdater(Context context, Phone[] phoneProxy, CommandsInterface[] commandsInterfaces) {
        return QtiSubscriptionInfoUpdater.init(context, phoneProxy, commandsInterfaces);
    }

    public PhoneBase makeGSMPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId) {
        return new QtiGSMPhone(context, ci, notifier, phoneId);
    }

    public PhoneProxy makePhoneProxy(PhoneBase phone) {
        return new QtiPhoneProxy(phone);
    }

    public PhoneBase makeCDMALTEPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId) {
        return new QtiCDMALTEPhone(context, ci, notifier, phoneId);
    }

    public void initExtTelephonyClasses(Context context, Phone[] phoneProxy, CommandsInterface[] commandsInterfaces) {
        Rlog.d(this.TAG, " Init ExtTelephonyServiceImpl ");
        ExtTelephonyServiceImpl.init(context);
        this.mQtiRadioCapabilityController = QtiRadioCapabilityController.make(context, phoneProxy, commandsInterfaces);
    }

    public DcTracker makeDcTracker(PhoneBase p) {
        return new QtiDcTracker(p);
    }
}
