package com.qti.internal.telephony.dataconnection;

import android.text.TextUtils;
import com.qti.internal.telephony.dataconnection.QtiApnSetting.ApnProfileType;
import java.util.ArrayList;

public class QtiApnProfileOmh extends QtiApnSetting {
    private static final int DATA_PROFILE_OMH_PRIORITY_HIGHEST = 0;
    private static final int DATA_PROFILE_OMH_PRIORITY_LOWEST = 255;
    private ApnProfileTypeModem mApnProfileModem;
    private int mPriority = DATA_PROFILE_OMH_PRIORITY_HIGHEST;
    private int mServiceTypeMasks = DATA_PROFILE_OMH_PRIORITY_HIGHEST;

    enum ApnProfileTypeModem {
        PROFILE_TYPE_UNSPECIFIED(1, "default"),
        PROFILE_TYPE_MMS(2, "mms"),
        PROFILE_TYPE_LBS(32, "supl"),
        PROFILE_TYPE_TETHERED(64, "dun");
        
        int id;
        String serviceType;

        private ApnProfileTypeModem(int i, String serviceType) {
            this.id = i;
            this.serviceType = serviceType;
        }

        public int getid() {
            return this.id;
        }

        public String getDataServiceType() {
            return this.serviceType;
        }

        public static ApnProfileTypeModem getApnProfileTypeModem(String serviceType) {
            if (TextUtils.equals(serviceType, "default")) {
                return PROFILE_TYPE_UNSPECIFIED;
            }
            if (TextUtils.equals(serviceType, "mms")) {
                return PROFILE_TYPE_MMS;
            }
            if (TextUtils.equals(serviceType, "supl")) {
                return PROFILE_TYPE_LBS;
            }
            if (TextUtils.equals(serviceType, "dun")) {
                return PROFILE_TYPE_TETHERED;
            }
            return PROFILE_TYPE_UNSPECIFIED;
        }
    }

    public QtiApnProfileOmh(int profileId, int priority) {
        super(DATA_PROFILE_OMH_PRIORITY_HIGHEST, "", null, "", null, null, null, null, null, null, null, 3, new String[DATA_PROFILE_OMH_PRIORITY_HIGHEST], "IP", "IP", true, DATA_PROFILE_OMH_PRIORITY_HIGHEST, DATA_PROFILE_OMH_PRIORITY_HIGHEST, profileId, false, DATA_PROFILE_OMH_PRIORITY_HIGHEST, DATA_PROFILE_OMH_PRIORITY_HIGHEST, DATA_PROFILE_OMH_PRIORITY_HIGHEST, DATA_PROFILE_OMH_PRIORITY_HIGHEST, "", "");
        this.mPriority = priority;
    }

    public boolean canHandleType(String serviceType) {
        return (this.mServiceTypeMasks & ApnProfileTypeModem.getApnProfileTypeModem(serviceType).getid()) != 0;
    }

    public ApnProfileType getApnProfileType() {
        return ApnProfileType.PROFILE_TYPE_OMH;
    }

    public String toShortString() {
        return "ApnProfile OMH";
    }

    public String toHash() {
        return toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(this.profileId).append(", ").append(this.mPriority);
        sb.append("]");
        return sb.toString();
    }

    public void setApnProfileTypeModem(ApnProfileTypeModem modemProfile) {
        this.mApnProfileModem = modemProfile;
    }

    public ApnProfileTypeModem getApnProfileTypeModem() {
        return this.mApnProfileModem;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
    }

    public boolean isPriorityHigher(int priority) {
        return isValidPriority(priority) && this.mPriority < priority;
    }

    public boolean isPriorityLower(int priority) {
        return isValidPriority(priority) && this.mPriority > priority;
    }

    public boolean isValidPriority() {
        return isValidPriority(this.mPriority);
    }

    private boolean isValidPriority(int priority) {
        return priority >= 0 && priority <= DATA_PROFILE_OMH_PRIORITY_LOWEST;
    }

    public int getProfileId() {
        return this.profileId;
    }

    public int getPriority() {
        return this.mPriority;
    }

    public void addServiceType(ApnProfileTypeModem modemProfile) {
        this.mServiceTypeMasks |= modemProfile.getid();
        ArrayList<String> serviceTypes = new ArrayList();
        ApnProfileTypeModem[] arr$ = ApnProfileTypeModem.values();
        int len$ = arr$.length;
        for (int i$ = DATA_PROFILE_OMH_PRIORITY_HIGHEST; i$ < len$; i$++) {
            ApnProfileTypeModem apt = arr$[i$];
            if ((this.mServiceTypeMasks & apt.getid()) != 0) {
                serviceTypes.add(apt.getDataServiceType());
            }
        }
        this.types = (String[]) serviceTypes.toArray(new String[DATA_PROFILE_OMH_PRIORITY_HIGHEST]);
    }
}
