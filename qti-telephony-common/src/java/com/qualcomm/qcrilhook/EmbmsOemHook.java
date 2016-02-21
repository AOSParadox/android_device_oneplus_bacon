package com.qualcomm.qcrilhook;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.util.Log;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiItemType;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiStructType;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiArray;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiByte;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiInteger;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiLong;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiString;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class EmbmsOemHook extends Handler {
    private static final int DEFAULT_PHONE = 0;
    private static final short EMBMSHOOK_MSG_ID_ACTDEACT = (short) 17;
    private static final short EMBMSHOOK_MSG_ID_ACTIVATE = (short) 2;
    private static final short EMBMSHOOK_MSG_ID_CONTENT_DESCRIPTION = (short) 29;
    private static final short EMBMSHOOK_MSG_ID_DEACTIVATE = (short) 3;
    private static final short EMBMSHOOK_MSG_ID_DELIVER_LOG_PACKET = (short) 22;
    private static final short EMBMSHOOK_MSG_ID_DISABLE = (short) 1;
    private static final short EMBMSHOOK_MSG_ID_ENABLE = (short) 0;
    private static final short EMBMSHOOK_MSG_ID_GET_ACTIVE = (short) 5;
    private static final short EMBMSHOOK_MSG_ID_GET_ACTIVE_LOG_PACKET_IDS = (short) 21;
    private static final short EMBMSHOOK_MSG_ID_GET_AVAILABLE = (short) 4;
    private static final short EMBMSHOOK_MSG_ID_GET_COVERAGE = (short) 8;
    private static final short EMBMSHOOK_MSG_ID_GET_E911_STATE = (short) 27;
    private static final short EMBMSHOOK_MSG_ID_GET_EMBMS_STATUS = (short) 33;
    private static final short EMBMSHOOK_MSG_ID_GET_INTERESTED_TMGI_LIST_RESP = (short) 35;
    private static final short EMBMSHOOK_MSG_ID_GET_PLMN_LIST = (short) 31;
    private static final short EMBMSHOOK_MSG_ID_GET_SIB16_COVERAGE = (short) 24;
    private static final short EMBMSHOOK_MSG_ID_GET_SIG_STRENGTH = (short) 9;
    private static final short EMBMSHOOK_MSG_ID_GET_TIME = (short) 26;
    private static final short EMBMSHOOK_MSG_ID_SET_TIME = (short) 23;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_ACTIVE_TMGI_LIST = (short) 12;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_AVAILABLE_TMGI_LIST = (short) 15;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_CELL_ID = (short) 18;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL = (short) 30;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_COVERAGE_STATE = (short) 13;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_E911_STATE = (short) 28;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_EMBMS_STATUS = (short) 32;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_GET_INTERESTED_TMGI_LIST = (short) 34;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_OOS_STATE = (short) 16;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_RADIO_STATE = (short) 19;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_SAI_LIST = (short) 20;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_SIB16 = (short) 25;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_STATE_CHANGE = (short) 11;
    private static final short EMBMS_SERVICE_ID = (short) 2;
    private static final int FAILURE = -1;
    private static String LOG_TAG = "EmbmsOemHook";
    private static final int OEM_HOOK_RESPONSE = 1;
    private static final short ONE_BYTE = (short) 1;
    private static final int QCRILHOOK_READY_CALLBACK = 2;
    private static final short SIZE_OF_EACH_PLMN_IN_BYTES = (short) 6;
    private static final int SIZE_OF_TMGI = 6;
    private static final int SUCCESS = 0;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_ACT_TMGI = (byte) 3;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_DEACT_TMGI = (byte) 4;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_EARFCN_LIST = (byte) 6;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_PRIORITY = (byte) 5;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_SAI_LIST = (byte) 16;
    private static final byte TLV_TYPE_ACTDEACTIVATE_RESP_ACTTMGI = (byte) 17;
    private static final byte TLV_TYPE_ACTDEACTIVATE_RESP_ACT_CODE = (byte) 2;
    private static final byte TLV_TYPE_ACTDEACTIVATE_RESP_DEACTTMGI = (byte) 18;
    private static final byte TLV_TYPE_ACTDEACTIVATE_RESP_DEACT_CODE = (byte) 3;
    private static final byte TLV_TYPE_ACTIVATE_REQ_EARFCN_LIST = (byte) 5;
    private static final byte TLV_TYPE_ACTIVATE_REQ_PRIORITY = (byte) 4;
    private static final byte TLV_TYPE_ACTIVATE_REQ_SAI_LIST = (byte) 16;
    private static final byte TLV_TYPE_ACTIVATE_REQ_TMGI = (byte) 3;
    private static final byte TLV_TYPE_ACTIVATE_RESP_TMGI = (byte) 17;
    private static final short TLV_TYPE_ACTIVELOGPACKETID_REQ_PACKET_ID_LIST = (short) 2;
    private static final short TLV_TYPE_ACTIVELOGPACKETID_RESP_PACKET_ID_LIST = (short) 2;
    private static final byte TLV_TYPE_COMMON_REQ_CALL_ID = (byte) 2;
    private static final byte TLV_TYPE_COMMON_REQ_TRACE_ID = (byte) 1;
    private static final byte TLV_TYPE_COMMON_RESP_CALL_ID = (byte) 16;
    private static final byte TLV_TYPE_COMMON_RESP_CODE = (byte) 2;
    private static final byte TLV_TYPE_COMMON_RESP_TRACE_ID = (byte) 1;
    private static final byte TLV_TYPE_CONTENT_DESCRIPTION_REQ_PARAMETER_ARRAY = (byte) 16;
    private static final byte TLV_TYPE_CONTENT_DESCRIPTION_REQ_TMGI = (byte) 3;
    private static final byte TLV_TYPE_DEACTIVATE_REQ_TMGI = (byte) 3;
    private static final byte TLV_TYPE_DEACTIVATE_RESP_TMGI = (byte) 17;
    private static final short TLV_TYPE_DELIVERLOGPACKET_REQ_LOG_PACKET = (short) 3;
    private static final short TLV_TYPE_DELIVERLOGPACKET_REQ_PACKET_ID = (short) 2;
    private static final byte TLV_TYPE_ENABLE_RESP_IFNAME = (byte) 17;
    private static final byte TLV_TYPE_ENABLE_RESP_IF_INDEX = (byte) 18;
    private static final byte TLV_TYPE_GET_ACTIVE_RESP_TMGI_ARRAY = (byte) 16;
    private static final byte TLV_TYPE_GET_AVAILABLE_RESP_TMGI_ARRAY = (byte) 16;
    private static final byte TLV_TYPE_GET_COVERAGE_STATE_RESP_STATE = (byte) 16;
    private static final short TLV_TYPE_GET_E911_RESP_STATE = (short) 16;
    private static final short TLV_TYPE_GET_EMBMS_STATUS_RESP = (short) 2;
    private static final byte TLV_TYPE_GET_INTERESTED_TMGI_LIST_RESP_TMGI = (byte) 3;
    private static final byte TLV_TYPE_GET_PLMN_LIST_RESP_PLMN_LIST = (byte) 2;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_ACTIVE_TMGI_LIST = (byte) 20;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_EXCESS_SNR = (byte) 18;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_MBSFN_AREA_ID = (byte) 16;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_NUMBER_OF_TMGI_PER_MBSFN = (byte) 19;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_SNR = (byte) 17;
    private static final byte TLV_TYPE_GET_TIME_RESP_DAY_LIGHT_SAVING = (byte) 16;
    private static final byte TLV_TYPE_GET_TIME_RESP_LEAP_SECONDS = (byte) 17;
    private static final byte TLV_TYPE_GET_TIME_RESP_LOCAL_TIME_OFFSET = (byte) 18;
    private static final byte TLV_TYPE_GET_TIME_RESP_TIME_MSECONDS = (byte) 3;
    private static final byte TLV_TYPE_SET_TIME_REQ_SNTP_SUCCESS = (byte) 1;
    private static final byte TLV_TYPE_SET_TIME_REQ_TIME_MSECONDS = (byte) 16;
    private static final byte TLV_TYPE_SET_TIME_REQ_TIME_STAMP = (byte) 17;
    private static final short TLV_TYPE_UNSOL_ACTIVE_IND_TMGI_ARRAY = (short) 2;
    private static final short TLV_TYPE_UNSOL_AVAILABLE_IND_TMGI_ARRAY_OR_RESPONSE_CODE = (short) 2;
    private static final short TLV_TYPE_UNSOL_CELL_ID_IND_CID = (short) 4;
    private static final short TLV_TYPE_UNSOL_CELL_ID_IND_MCC = (short) 2;
    private static final short TLV_TYPE_UNSOL_CELL_ID_IND_MNC = (short) 3;
    private static final short TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_CONTENT_CONTROL = (short) 16;
    private static final short TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_STATUS_CONTROL = (short) 17;
    private static final short TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_TMGI = (short) 2;
    private static final short TLV_TYPE_UNSOL_COVERAGE_IND_STATE_OR_RESPONSE_CODE = (short) 2;
    private static final short TLV_TYPE_UNSOL_E911_STATE_OR_RESPONSE_CODE = (short) 2;
    private static final short TLV_TYPE_UNSOL_EMBMS_STATUS = (short) 1;
    private static final short TLV_TYPE_UNSOL_OOS_IND_STATE = (short) 2;
    private static final short TLV_TYPE_UNSOL_OOS_IND_TMGI_ARRAY = (short) 3;
    private static final short TLV_TYPE_UNSOL_RADIO_STATE = (short) 2;
    private static final short TLV_TYPE_UNSOL_SAI_IND_AVAILABLE_SAI_LIST = (short) 4;
    private static final short TLV_TYPE_UNSOL_SAI_IND_CAMPED_SAI_LIST = (short) 2;
    private static final short TLV_TYPE_UNSOL_SAI_IND_SAI_PER_GROUP_LIST = (short) 3;
    private static final short TLV_TYPE_UNSOL_SIB16 = (short) 1;
    private static final short TLV_TYPE_UNSOL_STATE_IND_IF_INDEX = (short) 3;
    private static final short TLV_TYPE_UNSOL_STATE_IND_IP_ADDRESS = (short) 2;
    private static final short TLV_TYPE_UNSOL_STATE_IND_STATE = (short) 1;
    private static final short TWO_BYTES = (short) 2;
    private static final int UNSOL_BASE_QCRILHOOK = 4096;
    public static final int UNSOL_TYPE_ACTIVE_TMGI_LIST = 2;
    public static final int UNSOL_TYPE_AVAILABLE_TMGI_LIST = 4;
    public static final int UNSOL_TYPE_BROADCAST_COVERAGE = 3;
    public static final int UNSOL_TYPE_CELL_ID = 6;
    public static final int UNSOL_TYPE_CONTENT_DESC_PER_OBJ_CONTROL = 11;
    public static final int UNSOL_TYPE_E911_STATE = 10;
    public static final int UNSOL_TYPE_EMBMSOEMHOOK_READY_CALLBACK = 4097;
    public static final int UNSOL_TYPE_EMBMS_STATUS = 12;
    public static final int UNSOL_TYPE_GET_INTERESTED_TMGI_LIST = 13;
    public static final int UNSOL_TYPE_OOS_STATE = 5;
    public static final int UNSOL_TYPE_RADIO_STATE = 7;
    public static final int UNSOL_TYPE_SAI_LIST = 8;
    public static final int UNSOL_TYPE_SIB16_COVERAGE = 9;
    public static final int UNSOL_TYPE_STATE_CHANGE = 1;
    private static int mRefCount = SUCCESS;
    private static EmbmsOemHook sInstance;
    private QmiOemHook mQmiOemHook;
    private RegistrantList mRegistrants = new RegistrantList();

    public class ActDeactRequest extends BaseQmiStructType {
        public QmiArray<QmiByte> actTmgi;
        public QmiByte callId;
        public QmiArray<QmiByte> deActTmgi;
        public QmiArray<QmiInteger> earfcnList;
        public QmiInteger priority;
        public QmiArray<QmiInteger> saiList;
        public QmiInteger traceId;

        public ActDeactRequest(int trace, byte callId, byte[] actTmgi, byte[] deActTmgi, int priority, int[] saiList, int[] earfcnList) {
            this.traceId = new QmiInteger((long) trace);
            this.callId = new QmiByte(callId);
            this.priority = new QmiInteger((long) priority);
            this.actTmgi = EmbmsOemHook.this.byteArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, actTmgi);
            this.deActTmgi = EmbmsOemHook.this.byteArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, deActTmgi);
            this.saiList = EmbmsOemHook.this.intArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, saiList);
            this.earfcnList = EmbmsOemHook.this.intArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, earfcnList);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_RADIO_STATE];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.callId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST] = this.actTmgi;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE] = this.deActTmgi;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_AVAILABLE_TMGI_LIST] = this.priority;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_OOS_STATE] = this.saiList;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_CELL_ID] = this.earfcnList;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TWO_BYTES, EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_IF_INDEX, EmbmsOemHook.TLV_TYPE_UNSOL_SAI_IND_AVAILABLE_SAI_LIST, EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_ACTIVE, EmbmsOemHook.TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_CONTENT_CONTROL, EmbmsOemHook.SIZE_OF_EACH_PLMN_IN_BYTES};
        }
    }

    public class ActDeactResponse {
        public short actCode = EmbmsOemHook.EMBMSHOOK_MSG_ID_ENABLE;
        public byte[] actTmgi = null;
        public short deactCode = EmbmsOemHook.EMBMSHOOK_MSG_ID_ENABLE;
        public byte[] deactTmgi = null;
        public int status;
        public int traceId = EmbmsOemHook.SUCCESS;

        public ActDeactResponse(int status, ByteBuffer buf) {
            this.status = status;
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                byte tmgiLength;
                byte[] tmgi;
                byte i;
                switch (type) {
                    case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                        this.actCode = buf.getShort();
                        Log.i(EmbmsOemHook.LOG_TAG, "Act code = " + this.actCode);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE /*3*/:
                        this.deactCode = buf.getShort();
                        Log.i(EmbmsOemHook.LOG_TAG, "Deact code = " + this.deactCode);
                        break;
                    case 16:
                        Log.i(EmbmsOemHook.LOG_TAG, "callid = " + buf.get());
                        break;
                    case 17:
                        tmgiLength = buf.get();
                        tmgi = new byte[tmgiLength];
                        for (i = (byte) 0; i < tmgiLength; i += EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE) {
                            tmgi[i] = buf.get();
                        }
                        this.actTmgi = tmgi;
                        Log.i(EmbmsOemHook.LOG_TAG, "Act tmgi = " + EmbmsOemHook.bytesToHexString(this.actTmgi));
                        break;
                    case 18:
                        tmgiLength = buf.get();
                        tmgi = new byte[tmgiLength];
                        for (i = (byte) 0; i < tmgiLength; i += EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE) {
                            tmgi[i] = buf.get();
                        }
                        this.deactTmgi = tmgi;
                        Log.i(EmbmsOemHook.LOG_TAG, "Deact tmgi = " + EmbmsOemHook.bytesToHexString(this.deactTmgi));
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "TmgiResponse: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class ActiveLogPacketIDsRequest extends BaseQmiStructType {
        public QmiArray<QmiInteger> supportedLogPacketIdList;
        public QmiInteger traceId;

        public ActiveLogPacketIDsRequest(int trace, int[] supportedLogPacketIdList) {
            this.traceId = new QmiInteger((long) trace);
            this.supportedLogPacketIdList = EmbmsOemHook.this.intArrayToQmiArray(EmbmsOemHook.TWO_BYTES, supportedLogPacketIdList);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.supportedLogPacketIdList;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TWO_BYTES};
        }
    }

    public class ActiveLogPacketIDsResponse {
        public int[] activePacketIdList = null;
        public int status;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ActiveLogPacketIDsResponse(int r12, java.nio.ByteBuffer r13) {
            /*
            r10 = this;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r11;
            r10.<init>();
            r7 = 0;
            r10.traceId = r7;
            r7 = 0;
            r10.activePacketIdList = r7;
            r10.status = r12;
        L_0x000d:
            r7 = r13.hasRemaining();
            if (r7 == 0) goto L_0x00a6;
        L_0x0013:
            r7 = r13.get();	 Catch:{ BufferUnderflowException -> 0x0042 }
            r6 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r7);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r7 = r13.getShort();	 Catch:{ BufferUnderflowException -> 0x0042 }
            r3 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r7);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r5 = 0;
            switch(r6) {
                case 1: goto L_0x0082;
                case 2: goto L_0x004d;
                default: goto L_0x0027;
            };	 Catch:{ BufferUnderflowException -> 0x0042 }
        L_0x0027:
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8.<init>();	 Catch:{ BufferUnderflowException -> 0x0042 }
            r9 = "ActiveLogPacketIDsResponse: Unexpected Type ";
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = r8.append(r6);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = r8.toString();	 Catch:{ BufferUnderflowException -> 0x0042 }
            android.util.Log.e(r7, r8);	 Catch:{ BufferUnderflowException -> 0x0042 }
            goto L_0x000d;
        L_0x0042:
            r1 = move-exception;
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r8 = "Invalid format of byte buffer received in ActiveLogPacketIDsResponse";
            android.util.Log.e(r7, r8);
            goto L_0x000d;
        L_0x004d:
            r4 = r13.getShort();	 Catch:{ BufferUnderflowException -> 0x0042 }
            r0 = new int[r4];	 Catch:{ BufferUnderflowException -> 0x0042 }
            r2 = 0;
        L_0x0054:
            if (r2 >= r4) goto L_0x005f;
        L_0x0056:
            r7 = r13.getInt();	 Catch:{ BufferUnderflowException -> 0x0042 }
            r0[r2] = r7;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r2 = r2 + 1;
            goto L_0x0054;
        L_0x005f:
            r10.activePacketIdList = r0;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8.<init>();	 Catch:{ BufferUnderflowException -> 0x0042 }
            r9 = "Active log packet Id's = ";
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r9 = r10.activePacketIdList;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r9 = java.util.Arrays.toString(r9);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = r8.toString();	 Catch:{ BufferUnderflowException -> 0x0042 }
            android.util.Log.i(r7, r8);	 Catch:{ BufferUnderflowException -> 0x0042 }
            goto L_0x000d;
        L_0x0082:
            r7 = r13.getInt();	 Catch:{ BufferUnderflowException -> 0x0042 }
            r10.traceId = r7;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8.<init>();	 Catch:{ BufferUnderflowException -> 0x0042 }
            r9 = "traceId = ";
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r9 = r10.traceId;	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x0042 }
            r8 = r8.toString();	 Catch:{ BufferUnderflowException -> 0x0042 }
            android.util.Log.i(r7, r8);	 Catch:{ BufferUnderflowException -> 0x0042 }
            goto L_0x000d;
        L_0x00a6:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.ActiveLogPacketIDsResponse.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, int, java.nio.ByteBuffer):void");
        }
    }

    public class BasicRequest extends BaseQmiStructType {
        public QmiInteger traceId;

        public BasicRequest(int trace) {
            this.traceId = new QmiInteger((long) trace);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            short[] sArr = new short[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE];
            sArr[EmbmsOemHook.SUCCESS] = EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE;
            return sArr;
        }
    }

    public class CellIdIndication {
        public String id = null;
        public String mcc = null;
        public String mnc = null;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public CellIdIndication(java.nio.ByteBuffer r11) {
            /*
            r9 = this;
            r6 = 0;
            r5 = 0;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r10;
            r9.<init>();
            r9.mcc = r5;
            r9.mnc = r5;
            r9.id = r5;
            r9.traceId = r6;
        L_0x000f:
            r5 = r11.hasRemaining();
            if (r5 == 0) goto L_0x011b;
        L_0x0015:
            r5 = r11.get();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r4 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r5);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = r11.getShort();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r2 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r5);	 Catch:{ BufferUnderflowException -> 0x0043 }
            switch(r4) {
                case 1: goto L_0x00f7;
                case 2: goto L_0x004e;
                case 3: goto L_0x0084;
                case 4: goto L_0x00bb;
                default: goto L_0x0028;
            };	 Catch:{ BufferUnderflowException -> 0x0043 }
        L_0x0028:
            r5 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6.<init>();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = "CellIdIndication: Unexpected Type ";
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.append(r4);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.toString();	 Catch:{ BufferUnderflowException -> 0x0043 }
            android.util.Log.e(r5, r6);	 Catch:{ BufferUnderflowException -> 0x0043 }
            goto L_0x000f;
        L_0x0043:
            r0 = move-exception;
            r5 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r6 = "Unexpected buffer format when parsing for CellIdIndication";
            android.util.Log.e(r5, r6);
            goto L_0x000f;
        L_0x004e:
            r3 = new byte[r2];	 Catch:{ BufferUnderflowException -> 0x0043 }
            r1 = 0;
        L_0x0051:
            if (r1 >= r2) goto L_0x005c;
        L_0x0053:
            r5 = r11.get();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r3[r1] = r5;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r1 = r1 + 1;
            goto L_0x0051;
        L_0x005c:
            r5 = new com.qualcomm.qcrilhook.QmiPrimitiveTypes$QmiString;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5.<init>(r3);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = r5.toStringValue();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r9.mcc = r5;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6.<init>();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = "MCC = ";
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = r9.mcc;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.toString();	 Catch:{ BufferUnderflowException -> 0x0043 }
            android.util.Log.i(r5, r6);	 Catch:{ BufferUnderflowException -> 0x0043 }
            goto L_0x000f;
        L_0x0084:
            r3 = new byte[r2];	 Catch:{ BufferUnderflowException -> 0x0043 }
            r1 = 0;
        L_0x0087:
            if (r1 >= r2) goto L_0x0092;
        L_0x0089:
            r5 = r11.get();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r3[r1] = r5;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r1 = r1 + 1;
            goto L_0x0087;
        L_0x0092:
            r5 = new com.qualcomm.qcrilhook.QmiPrimitiveTypes$QmiString;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5.<init>(r3);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = r5.toStringValue();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r9.mnc = r5;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6.<init>();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = "MNC = ";
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = r9.mnc;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.toString();	 Catch:{ BufferUnderflowException -> 0x0043 }
            android.util.Log.i(r5, r6);	 Catch:{ BufferUnderflowException -> 0x0043 }
            goto L_0x000f;
        L_0x00bb:
            r5 = "%7s";
            r6 = 1;
            r6 = new java.lang.Object[r6];	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = 0;
            r8 = r11.getInt();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r8 = java.lang.Integer.toHexString(r8);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6[r7] = r8;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = java.lang.String.format(r5, r6);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = 32;
            r7 = 48;
            r5 = r5.replace(r6, r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r9.id = r5;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6.<init>();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = "CellId = ";
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = r9.id;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.toString();	 Catch:{ BufferUnderflowException -> 0x0043 }
            android.util.Log.i(r5, r6);	 Catch:{ BufferUnderflowException -> 0x0043 }
            goto L_0x000f;
        L_0x00f7:
            r5 = r11.getInt();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r9.traceId = r5;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6.<init>();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = "traceId = ";
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r7 = r9.traceId;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.append(r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r6 = r6.toString();	 Catch:{ BufferUnderflowException -> 0x0043 }
            android.util.Log.i(r5, r6);	 Catch:{ BufferUnderflowException -> 0x0043 }
            goto L_0x000f;
        L_0x011b:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.CellIdIndication.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, java.nio.ByteBuffer):void");
        }
    }

    public class ContentDescPerObjectControlIndication {
        public int perObjectContentControl;
        public int perObjectStatusControl;
        public byte[] tmgi = null;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ContentDescPerObjectControlIndication(java.nio.ByteBuffer r11) {
            /*
            r9 = this;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r10;
            r9.<init>();
            r6 = 0;
            r9.traceId = r6;
            r6 = 0;
            r9.tmgi = r6;
        L_0x000b:
            r6 = r11.hasRemaining();
            if (r6 == 0) goto L_0x00eb;
        L_0x0011:
            r6 = r11.get();	 Catch:{ BufferUnderflowException -> 0x003f }
            r5 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r6);	 Catch:{ BufferUnderflowException -> 0x003f }
            r6 = r11.getShort();	 Catch:{ BufferUnderflowException -> 0x003f }
            r2 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r6);	 Catch:{ BufferUnderflowException -> 0x003f }
            switch(r5) {
                case 1: goto L_0x00c7;
                case 2: goto L_0x004a;
                case 16: goto L_0x007f;
                case 17: goto L_0x00a3;
                default: goto L_0x0024;
            };	 Catch:{ BufferUnderflowException -> 0x003f }
        L_0x0024:
            r6 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7.<init>();	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = "ContentDescPerObjectControl: Unexpected Type ";
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.append(r5);	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.toString();	 Catch:{ BufferUnderflowException -> 0x003f }
            android.util.Log.e(r6, r7);	 Catch:{ BufferUnderflowException -> 0x003f }
            goto L_0x000b;
        L_0x003f:
            r0 = move-exception;
            r6 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r7 = "Unexpected buffer format when parsing forContentDescPerObjectControl Notification";
            android.util.Log.e(r6, r7);
            goto L_0x000b;
        L_0x004a:
            r4 = r11.get();	 Catch:{ BufferUnderflowException -> 0x003f }
            r3 = new byte[r4];	 Catch:{ BufferUnderflowException -> 0x003f }
            r1 = 0;
        L_0x0051:
            if (r1 >= r4) goto L_0x005c;
        L_0x0053:
            r6 = r11.get();	 Catch:{ BufferUnderflowException -> 0x003f }
            r3[r1] = r6;	 Catch:{ BufferUnderflowException -> 0x003f }
            r1 = r1 + 1;
            goto L_0x0051;
        L_0x005c:
            r9.tmgi = r3;	 Catch:{ BufferUnderflowException -> 0x003f }
            r6 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7.<init>();	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = "tmgi = ";
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = r9.tmgi;	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = com.qualcomm.qcrilhook.EmbmsOemHook.bytesToHexString(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.toString();	 Catch:{ BufferUnderflowException -> 0x003f }
            android.util.Log.i(r6, r7);	 Catch:{ BufferUnderflowException -> 0x003f }
            goto L_0x000b;
        L_0x007f:
            r6 = r11.getInt();	 Catch:{ BufferUnderflowException -> 0x003f }
            r9.perObjectContentControl = r6;	 Catch:{ BufferUnderflowException -> 0x003f }
            r6 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7.<init>();	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = "perObjectContentControl = ";
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = r9.perObjectContentControl;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.toString();	 Catch:{ BufferUnderflowException -> 0x003f }
            android.util.Log.i(r6, r7);	 Catch:{ BufferUnderflowException -> 0x003f }
            goto L_0x000b;
        L_0x00a3:
            r6 = r11.getInt();	 Catch:{ BufferUnderflowException -> 0x003f }
            r9.perObjectStatusControl = r6;	 Catch:{ BufferUnderflowException -> 0x003f }
            r6 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7.<init>();	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = "perObjectStatusControl = ";
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = r9.perObjectStatusControl;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.toString();	 Catch:{ BufferUnderflowException -> 0x003f }
            android.util.Log.i(r6, r7);	 Catch:{ BufferUnderflowException -> 0x003f }
            goto L_0x000b;
        L_0x00c7:
            r6 = r11.getInt();	 Catch:{ BufferUnderflowException -> 0x003f }
            r9.traceId = r6;	 Catch:{ BufferUnderflowException -> 0x003f }
            r6 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7.<init>();	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = "traceId = ";
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r8 = r9.traceId;	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.append(r8);	 Catch:{ BufferUnderflowException -> 0x003f }
            r7 = r7.toString();	 Catch:{ BufferUnderflowException -> 0x003f }
            android.util.Log.i(r6, r7);	 Catch:{ BufferUnderflowException -> 0x003f }
            goto L_0x000b;
        L_0x00eb:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.ContentDescPerObjectControlIndication.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, java.nio.ByteBuffer):void");
        }
    }

    public class ContentDescriptionReq extends BaseQmiStructType {
        public QmiByte callId;
        public QmiArray<QmiInteger> parameterArray;
        public QmiArray<QmiByte> tmgi;
        public QmiInteger traceId;

        public ContentDescriptionReq(int trace, byte callId, byte[] tmgi, int[] parameterArray) {
            this.traceId = new QmiInteger((long) trace);
            this.callId = new QmiByte(callId);
            this.tmgi = EmbmsOemHook.this.byteArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, tmgi);
            this.parameterArray = EmbmsOemHook.this.intArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, parameterArray, EmbmsOemHook.TWO_BYTES);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_AVAILABLE_TMGI_LIST];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.callId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST] = this.tmgi;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE] = this.parameterArray;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TWO_BYTES, EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_IF_INDEX, EmbmsOemHook.TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_CONTENT_CONTROL};
        }
    }

    public class CoverageState {
        public int code = EmbmsOemHook.SUCCESS;
        public int state;
        public int status;
        public int traceId = EmbmsOemHook.SUCCESS;

        public CoverageState(ByteBuffer buf, short msgId) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            continue;
                        case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                            if (msgId == EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_COVERAGE) {
                                this.code = buf.getInt();
                                Log.i(EmbmsOemHook.LOG_TAG, "response code = " + this.code);
                                continue;
                            }
                            break;
                        case 16:
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "CoverageState: Unexpected Type " + type);
                            continue;
                    }
                    this.state = buf.getInt();
                    Log.i(EmbmsOemHook.LOG_TAG, "Coverage State = " + this.state);
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Invalid format of byte buffer received in CoverageState");
                }
            }
        }
    }

    public class DeliverLogPacketRequest extends BaseQmiStructType {
        public QmiArray<QmiByte> logPacket;
        public QmiInteger logPacketId;
        public QmiInteger traceId;

        public DeliverLogPacketRequest(int trace, int logPacketId, byte[] logPacket) {
            this.traceId = new QmiInteger((long) trace);
            this.logPacketId = new QmiInteger((long) logPacketId);
            this.logPacket = EmbmsOemHook.this.byteArrayToQmiArray(EmbmsOemHook.TWO_BYTES, logPacket);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.logPacketId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST] = this.logPacket;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TWO_BYTES, EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_IF_INDEX};
        }
    }

    public class DisableResponse {
        public byte callId = (byte) 0;
        public int code = EmbmsOemHook.SUCCESS;
        public int status;
        public int traceId;

        public DisableResponse(int error, ByteBuffer buf) {
            this.status = error;
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                        this.code = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "code = " + this.code);
                        break;
                    case 16:
                        this.callId = buf.get();
                        Log.i(EmbmsOemHook.LOG_TAG, "callid = " + this.callId);
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "DisableResponse: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class E911StateIndication {
        public int code;
        public int state;
        public int traceId = EmbmsOemHook.SUCCESS;

        public E911StateIndication(ByteBuffer buf, short msgId) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            continue;
                        case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                            if (msgId == EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_E911_STATE) {
                                this.code = buf.getInt();
                                Log.i(EmbmsOemHook.LOG_TAG, "response code = " + this.code);
                                continue;
                            }
                            break;
                        case 16:
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "E911 State: Unexpected Type " + type);
                            continue;
                    }
                    this.state = buf.getInt();
                    Log.i(EmbmsOemHook.LOG_TAG, "E911 State = " + this.state);
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing for E911 Notification");
                }
            }
        }
    }

    public class EmbmsStatus {
        private static final int TYPE_EMBMS_STATUS = 1000;
        public boolean embmsStatus = false;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public EmbmsStatus(java.nio.ByteBuffer r10, int r11) {
            /*
            r8 = this;
            r4 = 0;
            r7 = 1;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r9;
            r8.<init>();
            r8.embmsStatus = r4;
            r8.traceId = r4;
        L_0x000b:
            r4 = r10.hasRemaining();
            if (r4 == 0) goto L_0x00b7;
        L_0x0011:
            r4 = r10.get();	 Catch:{ BufferUnderflowException -> 0x0047 }
            r3 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r4);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r4 = r10.getShort();	 Catch:{ BufferUnderflowException -> 0x0047 }
            r1 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r4);	 Catch:{ BufferUnderflowException -> 0x0047 }
            if (r3 != r7) goto L_0x0029;
        L_0x0023:
            r4 = 32;
            if (r11 != r4) goto L_0x0029;
        L_0x0027:
            r3 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        L_0x0029:
            switch(r3) {
                case 1: goto L_0x0093;
                case 2: goto L_0x0052;
                case 1000: goto L_0x0052;
                default: goto L_0x002c;
            };	 Catch:{ BufferUnderflowException -> 0x0047 }
        L_0x002c:
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x0047 }
            r6 = "embmsStatus: Unexpected Type ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = r5.append(r3);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x0047 }
            android.util.Log.e(r4, r5);	 Catch:{ BufferUnderflowException -> 0x0047 }
            goto L_0x000b;
        L_0x0047:
            r0 = move-exception;
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r5 = "Unexpected buffer format when parsing for embmsStatus";
            android.util.Log.e(r4, r5);
            goto L_0x000b;
        L_0x0052:
            r2 = r10.get();	 Catch:{ BufferUnderflowException -> 0x0047 }
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x0047 }
            r6 = "Unsol embmsStatus received = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = r5.append(r2);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x0047 }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x0047 }
            if (r2 != r7) goto L_0x0075;
        L_0x0072:
            r4 = 1;
            r8.embmsStatus = r4;	 Catch:{ BufferUnderflowException -> 0x0047 }
        L_0x0075:
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x0047 }
            r6 = "Unsol embmsStatus = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r6 = r8.embmsStatus;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x0047 }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x0047 }
            goto L_0x000b;
        L_0x0093:
            r4 = r10.getInt();	 Catch:{ BufferUnderflowException -> 0x0047 }
            r8.traceId = r4;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x0047 }
            r6 = "traceId = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r6 = r8.traceId;	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x0047 }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x0047 }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x0047 }
            goto L_0x000b;
        L_0x00b7:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.EmbmsStatus.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, java.nio.ByteBuffer, int):void");
        }
    }

    public class EnableResponse {
        public byte callId = (byte) 0;
        public int code = EmbmsOemHook.SUCCESS;
        public int ifIndex = EmbmsOemHook.SUCCESS;
        public String interfaceName = null;
        public int status;
        public int traceId;

        public EnableResponse(int error, ByteBuffer buf) {
            this.status = error;
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                        this.code = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "code = " + this.code);
                        break;
                    case 16:
                        this.callId = buf.get();
                        Log.i(EmbmsOemHook.LOG_TAG, "callid = " + this.callId);
                        break;
                    case 17:
                        byte[] name = new byte[length];
                        for (int i = EmbmsOemHook.SUCCESS; i < length; i += EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE) {
                            name[i] = buf.get();
                        }
                        this.interfaceName = new QmiString(name).toStringValue();
                        Log.i(EmbmsOemHook.LOG_TAG, "ifName = " + this.interfaceName);
                        break;
                    case 18:
                        this.ifIndex = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "ifIndex = " + this.ifIndex);
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "EnableResponse: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class GenericRequest extends BaseQmiStructType {
        public QmiByte callId;
        public QmiInteger traceId;

        public GenericRequest(int trace, byte callId) {
            this.traceId = new QmiInteger((long) trace);
            this.callId = new QmiByte(callId);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.callId;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TWO_BYTES};
        }
    }

    public class GetInterestedTmgiResponse extends BaseQmiStructType {
        public QmiByte callId;
        public QmiArray<QmiByte> tmgiList;
        public QmiInteger traceId;

        public GetInterestedTmgiResponse(int traceId, byte callId, byte[] tmgiList) {
            this.traceId = new QmiInteger((long) traceId);
            this.callId = new QmiByte(callId);
            this.tmgiList = EmbmsOemHook.this.tmgiListArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, tmgiList);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.callId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST] = this.tmgiList;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TWO_BYTES, EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_IF_INDEX};
        }
    }

    public class GetPLMNListResponse {
        public byte[] plmnList = null;
        public int status;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public GetPLMNListResponse(int r16, java.nio.ByteBuffer r17) {
            /*
            r14 = this;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r15;
            r14.<init>();
            r11 = 0;
            r14.traceId = r11;
            r11 = 0;
            r14.plmnList = r11;
            r0 = r16;
            r14.status = r0;
        L_0x000f:
            r11 = r17.hasRemaining();
            if (r11 == 0) goto L_0x00c9;
        L_0x0015:
            r11 = r17.get();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r10 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r11);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r11 = r17.getShort();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r5 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r11);	 Catch:{ BufferUnderflowException -> 0x0043 }
            switch(r10) {
                case 1: goto L_0x00a5;
                case 2: goto L_0x004e;
                default: goto L_0x0028;
            };	 Catch:{ BufferUnderflowException -> 0x0043 }
        L_0x0028:
            r11 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12.<init>();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r13 = "GetPLMNListResponse: Unexpected Type ";
            r12 = r12.append(r13);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = r12.append(r10);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = r12.toString();	 Catch:{ BufferUnderflowException -> 0x0043 }
            android.util.Log.e(r11, r12);	 Catch:{ BufferUnderflowException -> 0x0043 }
            goto L_0x000f;
        L_0x0043:
            r1 = move-exception;
            r11 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r12 = "Invalid format of byte buffer received in GetPLMNListResponse";
            android.util.Log.e(r11, r12);
            goto L_0x000f;
        L_0x004e:
            r8 = r17.get();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r9 = 32;
            r11 = r8 * 6;
            r11 = new byte[r11];	 Catch:{ BufferUnderflowException -> 0x0043 }
            r14.plmnList = r11;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r3 = 0;
            r2 = 0;
        L_0x005c:
            if (r2 >= r8) goto L_0x0083;
        L_0x005e:
            r6 = r17.get();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r11 = r14.plmnList;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r0 = r17;
            r0.get(r11, r3, r6);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r3 = r3 + r6;
            r7 = r17.get();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r11 = r14.plmnList;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r0 = r17;
            r0.get(r11, r3, r7);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r3 = r3 + r7;
            r11 = 2;
            if (r7 != r11) goto L_0x0080;
        L_0x0079:
            r11 = r14.plmnList;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r4 = r3 + 1;
            r11[r3] = r9;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r3 = r4;
        L_0x0080:
            r2 = r2 + 1;
            goto L_0x005c;
        L_0x0083:
            r11 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12.<init>();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r13 = "plmnList = ";
            r12 = r12.append(r13);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r13 = r14.plmnList;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r13 = com.qualcomm.qcrilhook.EmbmsOemHook.bytesToHexString(r13);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = r12.append(r13);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = r12.toString();	 Catch:{ BufferUnderflowException -> 0x0043 }
            android.util.Log.i(r11, r12);	 Catch:{ BufferUnderflowException -> 0x0043 }
            goto L_0x000f;
        L_0x00a5:
            r11 = r17.getInt();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r14.traceId = r11;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r11 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12.<init>();	 Catch:{ BufferUnderflowException -> 0x0043 }
            r13 = "traceId = ";
            r12 = r12.append(r13);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r13 = r14.traceId;	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = r12.append(r13);	 Catch:{ BufferUnderflowException -> 0x0043 }
            r12 = r12.toString();	 Catch:{ BufferUnderflowException -> 0x0043 }
            android.util.Log.i(r11, r12);	 Catch:{ BufferUnderflowException -> 0x0043 }
            goto L_0x000f;
        L_0x00c9:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.GetPLMNListResponse.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, int, java.nio.ByteBuffer):void");
        }
    }

    public class OosState {
        public byte[] list = null;
        public int state;
        public int traceId = EmbmsOemHook.SUCCESS;

        public OosState(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                        this.state = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "OOs State = " + this.state);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE /*3*/:
                        this.list = EmbmsOemHook.this.parseTmgi(buf);
                        Log.i(EmbmsOemHook.LOG_TAG, "tmgiArray = " + EmbmsOemHook.bytesToHexString(this.list));
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "OosState: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class RadioStateIndication {
        public int state = EmbmsOemHook.SUCCESS;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public RadioStateIndication(java.nio.ByteBuffer r8) {
            /*
            r6 = this;
            r3 = 0;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r7;
            r6.<init>();
            r6.state = r3;
            r6.traceId = r3;
        L_0x000a:
            r3 = r8.hasRemaining();
            if (r3 == 0) goto L_0x0090;
        L_0x0010:
            r3 = r8.get();	 Catch:{ BufferUnderflowException -> 0x003e }
            r2 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r3);	 Catch:{ BufferUnderflowException -> 0x003e }
            r3 = r8.getShort();	 Catch:{ BufferUnderflowException -> 0x003e }
            r1 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r3);	 Catch:{ BufferUnderflowException -> 0x003e }
            switch(r2) {
                case 1: goto L_0x006c;
                case 2: goto L_0x0049;
                default: goto L_0x0023;
            };	 Catch:{ BufferUnderflowException -> 0x003e }
        L_0x0023:
            r3 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003e }
            r4.<init>();	 Catch:{ BufferUnderflowException -> 0x003e }
            r5 = "RadioStateIndication: Unexpected Type ";
            r4 = r4.append(r5);	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = r4.append(r2);	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = r4.toString();	 Catch:{ BufferUnderflowException -> 0x003e }
            android.util.Log.e(r3, r4);	 Catch:{ BufferUnderflowException -> 0x003e }
            goto L_0x000a;
        L_0x003e:
            r0 = move-exception;
            r3 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r4 = "Unexpected buffer format when parsing for RadioStateIndication";
            android.util.Log.e(r3, r4);
            goto L_0x000a;
        L_0x0049:
            r3 = r8.getInt();	 Catch:{ BufferUnderflowException -> 0x003e }
            r6.state = r3;	 Catch:{ BufferUnderflowException -> 0x003e }
            r3 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003e }
            r4.<init>();	 Catch:{ BufferUnderflowException -> 0x003e }
            r5 = "radio = ";
            r4 = r4.append(r5);	 Catch:{ BufferUnderflowException -> 0x003e }
            r5 = r6.state;	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = r4.append(r5);	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = r4.toString();	 Catch:{ BufferUnderflowException -> 0x003e }
            android.util.Log.i(r3, r4);	 Catch:{ BufferUnderflowException -> 0x003e }
            goto L_0x000a;
        L_0x006c:
            r3 = r8.getInt();	 Catch:{ BufferUnderflowException -> 0x003e }
            r6.traceId = r3;	 Catch:{ BufferUnderflowException -> 0x003e }
            r3 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003e }
            r4.<init>();	 Catch:{ BufferUnderflowException -> 0x003e }
            r5 = "traceId = ";
            r4 = r4.append(r5);	 Catch:{ BufferUnderflowException -> 0x003e }
            r5 = r6.traceId;	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = r4.append(r5);	 Catch:{ BufferUnderflowException -> 0x003e }
            r4 = r4.toString();	 Catch:{ BufferUnderflowException -> 0x003e }
            android.util.Log.i(r3, r4);	 Catch:{ BufferUnderflowException -> 0x003e }
            goto L_0x000a;
        L_0x0090:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.RadioStateIndication.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, java.nio.ByteBuffer):void");
        }
    }

    public class RequestIndication {
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public RequestIndication(java.nio.ByteBuffer r8) {
            /*
            r6 = this;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r7;
            r6.<init>();
            r3 = 0;
            r6.traceId = r3;
        L_0x0008:
            r3 = r8.hasRemaining();
            if (r3 == 0) goto L_0x006a;
        L_0x000e:
            r3 = r8.get();	 Catch:{ BufferUnderflowException -> 0x003c }
            r2 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r3);	 Catch:{ BufferUnderflowException -> 0x003c }
            r3 = r8.getShort();	 Catch:{ BufferUnderflowException -> 0x003c }
            r1 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r3);	 Catch:{ BufferUnderflowException -> 0x003c }
            switch(r2) {
                case 1: goto L_0x0047;
                default: goto L_0x0021;
            };	 Catch:{ BufferUnderflowException -> 0x003c }
        L_0x0021:
            r3 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003c }
            r4.<init>();	 Catch:{ BufferUnderflowException -> 0x003c }
            r5 = "RequestIndication: Unexpected Type ";
            r4 = r4.append(r5);	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = r4.append(r2);	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = r4.toString();	 Catch:{ BufferUnderflowException -> 0x003c }
            android.util.Log.e(r3, r4);	 Catch:{ BufferUnderflowException -> 0x003c }
            goto L_0x0008;
        L_0x003c:
            r0 = move-exception;
            r3 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r4 = "Unexpected buffer format when parsing for RequestIndication";
            android.util.Log.e(r3, r4);
            goto L_0x0008;
        L_0x0047:
            r3 = r8.getInt();	 Catch:{ BufferUnderflowException -> 0x003c }
            r6.traceId = r3;	 Catch:{ BufferUnderflowException -> 0x003c }
            r3 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003c }
            r4.<init>();	 Catch:{ BufferUnderflowException -> 0x003c }
            r5 = "traceId = ";
            r4 = r4.append(r5);	 Catch:{ BufferUnderflowException -> 0x003c }
            r5 = r6.traceId;	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = r4.append(r5);	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = r4.toString();	 Catch:{ BufferUnderflowException -> 0x003c }
            android.util.Log.i(r3, r4);	 Catch:{ BufferUnderflowException -> 0x003c }
            goto L_0x0008;
        L_0x006a:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.RequestIndication.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, java.nio.ByteBuffer):void");
        }
    }

    public class SaiIndication {
        public int[] availableSaiList = null;
        public int[] campedSaiList = null;
        public int[] numSaiPerGroupList = null;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public SaiIndication(java.nio.ByteBuffer r12) {
            /*
            r10 = this;
            r7 = 0;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r11;
            r10.<init>();
            r10.campedSaiList = r7;
            r10.numSaiPerGroupList = r7;
            r10.availableSaiList = r7;
            r7 = 0;
            r10.traceId = r7;
        L_0x000f:
            r7 = r12.hasRemaining();
            if (r7 == 0) goto L_0x010c;
        L_0x0015:
            r6 = r12.get();	 Catch:{ BufferUnderflowException -> 0x003c }
            r3 = r12.getShort();	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = 0;
            switch(r6) {
                case 1: goto L_0x00e8;
                case 2: goto L_0x0047;
                case 3: goto L_0x007c;
                case 4: goto L_0x00b2;
                default: goto L_0x0021;
            };	 Catch:{ BufferUnderflowException -> 0x003c }
        L_0x0021:
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8.<init>();	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = "SaiIndication: Unexpected Type ";
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.append(r6);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.toString();	 Catch:{ BufferUnderflowException -> 0x003c }
            android.util.Log.e(r7, r8);	 Catch:{ BufferUnderflowException -> 0x003c }
            goto L_0x000f;
        L_0x003c:
            r1 = move-exception;
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r8 = "Unexpected buffer format when parsing for SaiIndication";
            android.util.Log.e(r7, r8);
            goto L_0x000f;
        L_0x0047:
            r5 = r12.get();	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = new int[r5];	 Catch:{ BufferUnderflowException -> 0x003c }
            r2 = 0;
        L_0x004e:
            if (r2 >= r5) goto L_0x0059;
        L_0x0050:
            r7 = r12.getInt();	 Catch:{ BufferUnderflowException -> 0x003c }
            r4[r2] = r7;	 Catch:{ BufferUnderflowException -> 0x003c }
            r2 = r2 + 1;
            goto L_0x004e;
        L_0x0059:
            r10.campedSaiList = r4;	 Catch:{ BufferUnderflowException -> 0x003c }
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8.<init>();	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = "Camped list = ";
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = r10.campedSaiList;	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = java.util.Arrays.toString(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.toString();	 Catch:{ BufferUnderflowException -> 0x003c }
            android.util.Log.i(r7, r8);	 Catch:{ BufferUnderflowException -> 0x003c }
            goto L_0x000f;
        L_0x007c:
            r5 = r12.get();	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = new int[r5];	 Catch:{ BufferUnderflowException -> 0x003c }
            r2 = 0;
        L_0x0083:
            if (r2 >= r5) goto L_0x008e;
        L_0x0085:
            r7 = r12.getInt();	 Catch:{ BufferUnderflowException -> 0x003c }
            r4[r2] = r7;	 Catch:{ BufferUnderflowException -> 0x003c }
            r2 = r2 + 1;
            goto L_0x0083;
        L_0x008e:
            r10.numSaiPerGroupList = r4;	 Catch:{ BufferUnderflowException -> 0x003c }
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8.<init>();	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = "Number of SAI per group list = ";
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = r10.numSaiPerGroupList;	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = java.util.Arrays.toString(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.toString();	 Catch:{ BufferUnderflowException -> 0x003c }
            android.util.Log.i(r7, r8);	 Catch:{ BufferUnderflowException -> 0x003c }
            goto L_0x000f;
        L_0x00b2:
            r0 = r12.getShort();	 Catch:{ BufferUnderflowException -> 0x003c }
            r4 = new int[r0];	 Catch:{ BufferUnderflowException -> 0x003c }
            r2 = 0;
        L_0x00b9:
            if (r2 >= r0) goto L_0x00c4;
        L_0x00bb:
            r7 = r12.getInt();	 Catch:{ BufferUnderflowException -> 0x003c }
            r4[r2] = r7;	 Catch:{ BufferUnderflowException -> 0x003c }
            r2 = r2 + 1;
            goto L_0x00b9;
        L_0x00c4:
            r10.availableSaiList = r4;	 Catch:{ BufferUnderflowException -> 0x003c }
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8.<init>();	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = "Available SAI list = ";
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = r10.availableSaiList;	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = java.util.Arrays.toString(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.toString();	 Catch:{ BufferUnderflowException -> 0x003c }
            android.util.Log.i(r7, r8);	 Catch:{ BufferUnderflowException -> 0x003c }
            goto L_0x000f;
        L_0x00e8:
            r7 = r12.getInt();	 Catch:{ BufferUnderflowException -> 0x003c }
            r10.traceId = r7;	 Catch:{ BufferUnderflowException -> 0x003c }
            r7 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8.<init>();	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = "traceId = ";
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r9 = r10.traceId;	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.append(r9);	 Catch:{ BufferUnderflowException -> 0x003c }
            r8 = r8.toString();	 Catch:{ BufferUnderflowException -> 0x003c }
            android.util.Log.i(r7, r8);	 Catch:{ BufferUnderflowException -> 0x003c }
            goto L_0x000f;
        L_0x010c:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.SaiIndication.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, java.nio.ByteBuffer):void");
        }
    }

    public class SetTimeRequest extends BaseQmiStructType {
        public QmiByte sntpSuccess;
        public QmiLong timeMseconds;
        public QmiLong timeStamp;

        public SetTimeRequest(byte sntpSuccess, long timeMseconds, long timeStamp) {
            this.sntpSuccess = new QmiByte(sntpSuccess);
            this.timeMseconds = new QmiLong(timeMseconds);
            this.timeStamp = new QmiLong(timeStamp);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.sntpSuccess;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.timeMseconds;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST] = this.timeStamp;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_CONTENT_CONTROL, EmbmsOemHook.TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_STATUS_CONTROL};
        }
    }

    public class Sib16Coverage {
        public boolean inCoverage = false;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Sib16Coverage(java.nio.ByteBuffer r10) {
            /*
            r8 = this;
            r7 = 1;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r9;
            r8.<init>();
            r4 = 0;
            r8.inCoverage = r4;
        L_0x0009:
            r4 = r10.hasRemaining();
            if (r4 == 0) goto L_0x006e;
        L_0x000f:
            r4 = r10.get();	 Catch:{ BufferUnderflowException -> 0x003d }
            r3 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r4);	 Catch:{ BufferUnderflowException -> 0x003d }
            r4 = r10.getShort();	 Catch:{ BufferUnderflowException -> 0x003d }
            r2 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r4);	 Catch:{ BufferUnderflowException -> 0x003d }
            switch(r3) {
                case 1: goto L_0x0048;
                default: goto L_0x0022;
            };	 Catch:{ BufferUnderflowException -> 0x003d }
        L_0x0022:
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x003d }
            r6 = "Sib16Coverage: Unexpected Type ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x003d }
            r5 = r5.append(r3);	 Catch:{ BufferUnderflowException -> 0x003d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x003d }
            android.util.Log.e(r4, r5);	 Catch:{ BufferUnderflowException -> 0x003d }
            goto L_0x0009;
        L_0x003d:
            r1 = move-exception;
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r5 = "Unexpected buffer format when parsing for Sib16Coverage";
            android.util.Log.e(r4, r5);
            goto L_0x0009;
        L_0x0048:
            r0 = r10.get();	 Catch:{ BufferUnderflowException -> 0x003d }
            if (r0 != r7) goto L_0x0051;
        L_0x004e:
            r4 = 1;
            r8.inCoverage = r4;	 Catch:{ BufferUnderflowException -> 0x003d }
        L_0x0051:
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x003d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x003d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x003d }
            r6 = "Unsol SIB16 coverage status = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x003d }
            r6 = r8.inCoverage;	 Catch:{ BufferUnderflowException -> 0x003d }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x003d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x003d }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x003d }
            goto L_0x0009;
        L_0x006e:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.Sib16Coverage.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, java.nio.ByteBuffer):void");
        }
    }

    public class SigStrengthResponse {
        public int code = EmbmsOemHook.SUCCESS;
        public float[] esnr = null;
        public int[] mbsfnAreaId = null;
        public float[] snr = null;
        public int status;
        public int[] tmgiPerMbsfn = null;
        public byte[] tmgilist = null;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public SigStrengthResponse(int r19, java.nio.ByteBuffer r20) {
            /*
            r17 = this;
            r0 = r18;
            r1 = r17;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r0;
            r17.<init>();
            r14 = 0;
            r0 = r17;
            r0.code = r14;
            r14 = 0;
            r0 = r17;
            r0.traceId = r14;
            r14 = 0;
            r0 = r17;
            r0.snr = r14;
            r14 = 0;
            r0 = r17;
            r0.mbsfnAreaId = r14;
            r14 = 0;
            r0 = r17;
            r0.esnr = r14;
            r14 = 0;
            r0 = r17;
            r0.tmgiPerMbsfn = r14;
            r14 = 0;
            r0 = r17;
            r0.tmgilist = r14;
            r0 = r19;
            r1 = r17;
            r1.status = r0;
        L_0x0032:
            r14 = r20.hasRemaining();
            if (r14 == 0) goto L_0x01de;
        L_0x0038:
            r13 = r20.get();	 Catch:{ BufferUnderflowException -> 0x005e }
            r6 = r20.getShort();	 Catch:{ BufferUnderflowException -> 0x005e }
            switch(r13) {
                case 1: goto L_0x01b4;
                case 2: goto L_0x018a;
                case 16: goto L_0x00a4;
                case 17: goto L_0x0069;
                case 18: goto L_0x00e0;
                case 19: goto L_0x011c;
                case 20: goto L_0x0158;
                default: goto L_0x0043;
            };	 Catch:{ BufferUnderflowException -> 0x005e }
        L_0x0043:
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15.<init>();	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = "SigStrengthResponse: Unexpected Type ";
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.append(r13);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.toString();	 Catch:{ BufferUnderflowException -> 0x005e }
            android.util.Log.e(r14, r15);	 Catch:{ BufferUnderflowException -> 0x005e }
            goto L_0x0032;
        L_0x005e:
            r2 = move-exception;
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r15 = "Invalid format of byte buffer received in SigStrengthResponse";
            android.util.Log.e(r14, r15);
            goto L_0x0032;
        L_0x0069:
            r10 = r20.get();	 Catch:{ BufferUnderflowException -> 0x005e }
            r9 = new float[r10];	 Catch:{ BufferUnderflowException -> 0x005e }
            r5 = 0;
        L_0x0070:
            if (r5 >= r10) goto L_0x007b;
        L_0x0072:
            r14 = r20.getFloat();	 Catch:{ BufferUnderflowException -> 0x005e }
            r9[r5] = r14;	 Catch:{ BufferUnderflowException -> 0x005e }
            r5 = r5 + 1;
            goto L_0x0070;
        L_0x007b:
            r0 = r17;
            r0.snr = r9;	 Catch:{ BufferUnderflowException -> 0x005e }
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15.<init>();	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = "SNR = ";
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0 = r0.snr;	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = r0;
            r16 = java.util.Arrays.toString(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.toString();	 Catch:{ BufferUnderflowException -> 0x005e }
            android.util.Log.i(r14, r15);	 Catch:{ BufferUnderflowException -> 0x005e }
            goto L_0x0032;
        L_0x00a4:
            r8 = r20.get();	 Catch:{ BufferUnderflowException -> 0x005e }
            r7 = new int[r8];	 Catch:{ BufferUnderflowException -> 0x005e }
            r5 = 0;
        L_0x00ab:
            if (r5 >= r8) goto L_0x00b6;
        L_0x00ad:
            r14 = r20.getInt();	 Catch:{ BufferUnderflowException -> 0x005e }
            r7[r5] = r14;	 Catch:{ BufferUnderflowException -> 0x005e }
            r5 = r5 + 1;
            goto L_0x00ab;
        L_0x00b6:
            r0 = r17;
            r0.mbsfnAreaId = r7;	 Catch:{ BufferUnderflowException -> 0x005e }
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15.<init>();	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = "MBSFN_Area_ID = ";
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0 = r0.mbsfnAreaId;	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = r0;
            r16 = java.util.Arrays.toString(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.toString();	 Catch:{ BufferUnderflowException -> 0x005e }
            android.util.Log.i(r14, r15);	 Catch:{ BufferUnderflowException -> 0x005e }
            goto L_0x0032;
        L_0x00e0:
            r4 = r20.get();	 Catch:{ BufferUnderflowException -> 0x005e }
            r3 = new float[r4];	 Catch:{ BufferUnderflowException -> 0x005e }
            r5 = 0;
        L_0x00e7:
            if (r5 >= r4) goto L_0x00f2;
        L_0x00e9:
            r14 = r20.getFloat();	 Catch:{ BufferUnderflowException -> 0x005e }
            r3[r5] = r14;	 Catch:{ BufferUnderflowException -> 0x005e }
            r5 = r5 + 1;
            goto L_0x00e7;
        L_0x00f2:
            r0 = r17;
            r0.esnr = r3;	 Catch:{ BufferUnderflowException -> 0x005e }
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15.<init>();	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = "EXCESS SNR = ";
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0 = r0.esnr;	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = r0;
            r16 = java.util.Arrays.toString(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.toString();	 Catch:{ BufferUnderflowException -> 0x005e }
            android.util.Log.i(r14, r15);	 Catch:{ BufferUnderflowException -> 0x005e }
            goto L_0x0032;
        L_0x011c:
            r12 = r20.get();	 Catch:{ BufferUnderflowException -> 0x005e }
            r11 = new int[r12];	 Catch:{ BufferUnderflowException -> 0x005e }
            r5 = 0;
        L_0x0123:
            if (r5 >= r12) goto L_0x012e;
        L_0x0125:
            r14 = r20.getInt();	 Catch:{ BufferUnderflowException -> 0x005e }
            r11[r5] = r14;	 Catch:{ BufferUnderflowException -> 0x005e }
            r5 = r5 + 1;
            goto L_0x0123;
        L_0x012e:
            r0 = r17;
            r0.tmgiPerMbsfn = r11;	 Catch:{ BufferUnderflowException -> 0x005e }
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15.<init>();	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = "NUMBER OF TMGI PER MBSFN = ";
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0 = r0.tmgiPerMbsfn;	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = r0;
            r16 = java.util.Arrays.toString(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.toString();	 Catch:{ BufferUnderflowException -> 0x005e }
            android.util.Log.i(r14, r15);	 Catch:{ BufferUnderflowException -> 0x005e }
            goto L_0x0032;
        L_0x0158:
            r0 = r18;
            r1 = r20;
            r14 = r0.parseActiveTmgi(r1);	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0.tmgilist = r14;	 Catch:{ BufferUnderflowException -> 0x005e }
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15.<init>();	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = "tmgiArray = ";
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0 = r0.tmgilist;	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = r0;
            r16 = com.qualcomm.qcrilhook.EmbmsOemHook.bytesToHexString(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.toString();	 Catch:{ BufferUnderflowException -> 0x005e }
            android.util.Log.i(r14, r15);	 Catch:{ BufferUnderflowException -> 0x005e }
            goto L_0x0032;
        L_0x018a:
            r14 = r20.getInt();	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0.code = r14;	 Catch:{ BufferUnderflowException -> 0x005e }
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15.<init>();	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = "code = ";
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0 = r0.code;	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = r0;
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.toString();	 Catch:{ BufferUnderflowException -> 0x005e }
            android.util.Log.i(r14, r15);	 Catch:{ BufferUnderflowException -> 0x005e }
            goto L_0x0032;
        L_0x01b4:
            r14 = r20.getInt();	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0.traceId = r14;	 Catch:{ BufferUnderflowException -> 0x005e }
            r14 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x005e }
            r15.<init>();	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = "traceId = ";
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r0 = r17;
            r0 = r0.traceId;	 Catch:{ BufferUnderflowException -> 0x005e }
            r16 = r0;
            r15 = r15.append(r16);	 Catch:{ BufferUnderflowException -> 0x005e }
            r15 = r15.toString();	 Catch:{ BufferUnderflowException -> 0x005e }
            android.util.Log.i(r14, r15);	 Catch:{ BufferUnderflowException -> 0x005e }
            goto L_0x0032;
        L_0x01de:
            r0 = r17;
            r14 = r0.snr;
            if (r14 != 0) goto L_0x01eb;
        L_0x01e4:
            r14 = 0;
            r14 = new float[r14];
            r0 = r17;
            r0.snr = r14;
        L_0x01eb:
            r0 = r17;
            r14 = r0.esnr;
            if (r14 != 0) goto L_0x01f8;
        L_0x01f1:
            r14 = 0;
            r14 = new float[r14];
            r0 = r17;
            r0.esnr = r14;
        L_0x01f8:
            r0 = r17;
            r14 = r0.tmgiPerMbsfn;
            if (r14 != 0) goto L_0x0205;
        L_0x01fe:
            r14 = 0;
            r14 = new int[r14];
            r0 = r17;
            r0.tmgiPerMbsfn = r14;
        L_0x0205:
            r0 = r17;
            r14 = r0.mbsfnAreaId;
            if (r14 != 0) goto L_0x0212;
        L_0x020b:
            r14 = 0;
            r14 = new int[r14];
            r0 = r17;
            r0.mbsfnAreaId = r14;
        L_0x0212:
            r0 = r17;
            r14 = r0.tmgilist;
            if (r14 != 0) goto L_0x021f;
        L_0x0218:
            r14 = 0;
            r14 = new byte[r14];
            r0 = r17;
            r0.tmgilist = r14;
        L_0x021f:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.SigStrengthResponse.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, int, java.nio.ByteBuffer):void");
        }
    }

    public class StateChangeInfo {
        public int ifIndex;
        public String ipAddress;
        public int state;

        public StateChangeInfo(int state, String address, int index) {
            this.state = state;
            this.ipAddress = address;
            this.ifIndex = index;
        }

        public StateChangeInfo(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                        this.state = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "State = " + this.state);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                        byte[] address = new byte[length];
                        for (int i = EmbmsOemHook.SUCCESS; i < length; i += EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE) {
                            address[i] = buf.get();
                        }
                        this.ipAddress = new QmiString(address).toString();
                        Log.i(EmbmsOemHook.LOG_TAG, "ip Address = " + this.ipAddress);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE /*3*/:
                        this.ifIndex = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "index = " + this.ifIndex);
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "StateChangeInfo: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class TimeResponse {
        public boolean additionalInfo = false;
        public int code = EmbmsOemHook.SUCCESS;
        public boolean dayLightSaving = false;
        public byte leapSeconds = (byte) 0;
        public long localTimeOffset = 0;
        public int status;
        public long timeMseconds = 0;
        public int traceId = EmbmsOemHook.SUCCESS;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public TimeResponse(int r11, java.nio.ByteBuffer r12) {
            /*
            r9 = this;
            r6 = 0;
            r8 = 1;
            r4 = 0;
            com.qualcomm.qcrilhook.EmbmsOemHook.this = r10;
            r9.<init>();
            r9.code = r4;
            r9.timeMseconds = r6;
            r9.additionalInfo = r4;
            r9.dayLightSaving = r4;
            r9.leapSeconds = r4;
            r9.traceId = r4;
            r9.localTimeOffset = r6;
            r9.status = r11;
        L_0x0019:
            r4 = r12.hasRemaining();
            if (r4 == 0) goto L_0x013c;
        L_0x001f:
            r4 = r12.get();	 Catch:{ BufferUnderflowException -> 0x004d }
            r3 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r4);	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = r12.getShort();	 Catch:{ BufferUnderflowException -> 0x004d }
            r2 = com.qualcomm.qcrilhook.PrimitiveParser.toUnsigned(r4);	 Catch:{ BufferUnderflowException -> 0x004d }
            switch(r3) {
                case 1: goto L_0x00f4;
                case 2: goto L_0x0118;
                case 3: goto L_0x0058;
                case 16: goto L_0x007b;
                case 17: goto L_0x00a5;
                case 18: goto L_0x00cc;
                default: goto L_0x0032;
            };	 Catch:{ BufferUnderflowException -> 0x004d }
        L_0x0032:
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = "TimeResponse: Unexpected Type ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.append(r3);	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x004d }
            android.util.Log.e(r4, r5);	 Catch:{ BufferUnderflowException -> 0x004d }
            goto L_0x0019;
        L_0x004d:
            r0 = move-exception;
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r5 = "Invalid format of byte buffer received in TimeResponse";
            android.util.Log.e(r4, r5);
            goto L_0x0019;
        L_0x0058:
            r4 = r12.getLong();	 Catch:{ BufferUnderflowException -> 0x004d }
            r9.timeMseconds = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = "timeMseconds = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = r9.timeMseconds;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x004d }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x004d }
            goto L_0x0019;
        L_0x007b:
            r4 = 1;
            r9.additionalInfo = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r1 = r12.get();	 Catch:{ BufferUnderflowException -> 0x004d }
            if (r1 != r8) goto L_0x0087;
        L_0x0084:
            r4 = 1;
            r9.dayLightSaving = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
        L_0x0087:
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = "dayLightSaving = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = r9.dayLightSaving;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x004d }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x004d }
            goto L_0x0019;
        L_0x00a5:
            r4 = 1;
            r9.additionalInfo = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = r12.get();	 Catch:{ BufferUnderflowException -> 0x004d }
            r9.leapSeconds = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = "leapSeconds = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = r9.leapSeconds;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x004d }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x004d }
            goto L_0x0019;
        L_0x00cc:
            r4 = 1;
            r9.additionalInfo = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = r12.get();	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = (long) r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r9.localTimeOffset = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = "localTimeOffset = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = r9.localTimeOffset;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x004d }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x004d }
            goto L_0x0019;
        L_0x00f4:
            r4 = r12.getInt();	 Catch:{ BufferUnderflowException -> 0x004d }
            r9.traceId = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = "traceId = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = r9.traceId;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x004d }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x004d }
            goto L_0x0019;
        L_0x0118:
            r4 = r12.getInt();	 Catch:{ BufferUnderflowException -> 0x004d }
            r9.code = r4;	 Catch:{ BufferUnderflowException -> 0x004d }
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5.<init>();	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = "code = ";
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r6 = r9.code;	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.append(r6);	 Catch:{ BufferUnderflowException -> 0x004d }
            r5 = r5.toString();	 Catch:{ BufferUnderflowException -> 0x004d }
            android.util.Log.i(r4, r5);	 Catch:{ BufferUnderflowException -> 0x004d }
            goto L_0x0019;
        L_0x013c:
            r4 = com.qualcomm.qcrilhook.EmbmsOemHook.LOG_TAG;
            r5 = new java.lang.StringBuilder;
            r5.<init>();
            r6 = "additionalInfo = ";
            r5 = r5.append(r6);
            r6 = r9.additionalInfo;
            r5 = r5.append(r6);
            r5 = r5.toString();
            android.util.Log.i(r4, r5);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.EmbmsOemHook.TimeResponse.<init>(com.qualcomm.qcrilhook.EmbmsOemHook, int, java.nio.ByteBuffer):void");
        }

        public TimeResponse(int traceId, int status, long timeMseconds, boolean additonalInfo, long localTimeOffset, boolean dayLightSaving, byte leapSeconds) {
            this.status = status;
            this.traceId = traceId;
            this.code = EmbmsOemHook.SUCCESS;
            this.timeMseconds = timeMseconds;
            this.localTimeOffset = localTimeOffset;
            this.additionalInfo = this.additionalInfo;
            this.dayLightSaving = dayLightSaving;
            this.leapSeconds = leapSeconds;
            Log.i(EmbmsOemHook.LOG_TAG, "TimeResponse: traceId = " + this.traceId + " code = " + this.code + " timeMseconds = " + this.timeMseconds + "additionalInfo = " + this.additionalInfo + " localTimeOffset = " + this.localTimeOffset + " dayLightSaving = " + this.dayLightSaving + " leapSeconds = " + this.leapSeconds);
        }
    }

    public class TmgiActivateRequest extends BaseQmiStructType {
        public QmiByte callId;
        public QmiArray<QmiInteger> earfcnList;
        public QmiInteger priority;
        public QmiArray<QmiInteger> saiList;
        public QmiArray<QmiByte> tmgi;
        public QmiInteger traceId;

        public TmgiActivateRequest(int trace, byte callId, byte[] tmgi, int priority, int[] saiList, int[] earfcnList) {
            this.traceId = new QmiInteger((long) trace);
            this.callId = new QmiByte(callId);
            this.priority = new QmiInteger((long) priority);
            this.tmgi = EmbmsOemHook.this.byteArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, tmgi);
            this.saiList = EmbmsOemHook.this.intArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, saiList);
            this.earfcnList = EmbmsOemHook.this.intArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, earfcnList);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_CELL_ID];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.callId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST] = this.tmgi;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE] = this.priority;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_AVAILABLE_TMGI_LIST] = this.saiList;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_OOS_STATE] = this.earfcnList;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TWO_BYTES, EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_IF_INDEX, EmbmsOemHook.TLV_TYPE_UNSOL_SAI_IND_AVAILABLE_SAI_LIST, EmbmsOemHook.TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_CONTENT_CONTROL, EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_ACTIVE};
        }
    }

    public class TmgiDeActivateRequest extends BaseQmiStructType {
        public QmiByte callId;
        public QmiArray<QmiByte> tmgi;
        public QmiInteger traceId;

        public TmgiDeActivateRequest(int trace, byte[] tmgi, byte callId) {
            this.traceId = new QmiInteger((long) trace);
            this.tmgi = EmbmsOemHook.this.byteArrayToQmiArray(EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, tmgi);
            this.callId = new QmiByte(callId);
        }

        public BaseQmiItemType[] getItems() {
            BaseQmiItemType[] baseQmiItemTypeArr = new BaseQmiItemType[EmbmsOemHook.UNSOL_TYPE_BROADCAST_COVERAGE];
            baseQmiItemTypeArr[EmbmsOemHook.SUCCESS] = this.traceId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE] = this.callId;
            baseQmiItemTypeArr[EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST] = this.tmgi;
            return baseQmiItemTypeArr;
        }

        public short[] getTypes() {
            return new short[]{EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_STATE, EmbmsOemHook.TWO_BYTES, EmbmsOemHook.TLV_TYPE_UNSOL_STATE_IND_IF_INDEX};
        }
    }

    public class TmgiListIndication {
        public int code = EmbmsOemHook.SUCCESS;
        public byte[] list = new byte[EmbmsOemHook.SUCCESS];
        public byte[] sessions = null;
        public int traceId = EmbmsOemHook.SUCCESS;

        public TmgiListIndication(ByteBuffer buf, short msgId) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            continue;
                        case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                            if (msgId == EmbmsOemHook.TLV_TYPE_UNSOL_SAI_IND_AVAILABLE_SAI_LIST || msgId == EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_ACTIVE) {
                                this.code = buf.getInt();
                                Log.i(EmbmsOemHook.LOG_TAG, "response code = " + this.code);
                                continue;
                            }
                        case 16:
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "TmgiListIndication: Unexpected Type " + type);
                            continue;
                    }
                    this.list = EmbmsOemHook.this.parseTmgi(buf);
                    Log.i(EmbmsOemHook.LOG_TAG, "tmgiArray = " + EmbmsOemHook.bytesToHexString(this.list));
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Invalid format of byte buffer received in TmgiListIndication");
                }
            }
        }
    }

    public class TmgiResponse {
        public int code = EmbmsOemHook.SUCCESS;
        public int status;
        public byte[] tmgi = null;
        public int traceId = EmbmsOemHook.SUCCESS;

        public TmgiResponse(int status, ByteBuffer buf) {
            this.status = status;
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE /*1*/:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case EmbmsOemHook.UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                        this.code = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "code = " + this.code);
                        break;
                    case 16:
                        Log.i(EmbmsOemHook.LOG_TAG, "callid = " + buf.get());
                        break;
                    case 17:
                        byte tmgiLength = buf.get();
                        byte[] tmgi = new byte[tmgiLength];
                        for (byte i = (byte) 0; i < tmgiLength; i += EmbmsOemHook.UNSOL_TYPE_STATE_CHANGE) {
                            tmgi[i] = buf.get();
                        }
                        this.tmgi = tmgi;
                        Log.i(EmbmsOemHook.LOG_TAG, "tmgi = " + EmbmsOemHook.bytesToHexString(this.tmgi));
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "TmgiResponse: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class UnsolObject {
        public Object obj;
        public int phoneId;
        public int unsolId;

        public UnsolObject(int i, Object o, int phone) {
            this.unsolId = i;
            this.obj = o;
            this.phoneId = phone;
        }
    }

    private EmbmsOemHook(Context context) {
        Log.v(LOG_TAG, "EmbmsOemHook ()");
        this.mQmiOemHook = QmiOemHook.getInstance(context);
        QmiOemHook.registerService(TWO_BYTES, this, UNSOL_TYPE_STATE_CHANGE);
        QmiOemHook.registerOnReadyCb(this, UNSOL_TYPE_ACTIVE_TMGI_LIST, null);
    }

    public static synchronized EmbmsOemHook getInstance(Context context) {
        EmbmsOemHook embmsOemHook;
        synchronized (EmbmsOemHook.class) {
            if (sInstance == null) {
                sInstance = new EmbmsOemHook(context);
                Log.d(LOG_TAG, "Singleton Instance of Embms created.");
            }
            mRefCount += UNSOL_TYPE_STATE_CHANGE;
            embmsOemHook = sInstance;
        }
        return embmsOemHook;
    }

    public synchronized void dispose() {
        int i = mRefCount + FAILURE;
        mRefCount = i;
        if (i == 0) {
            Log.d(LOG_TAG, "dispose(): Unregistering receiver");
            QmiOemHook.unregisterService(UNSOL_TYPE_ACTIVE_TMGI_LIST);
            QmiOemHook.unregisterOnReadyCb(this);
            this.mQmiOemHook.dispose();
            this.mQmiOemHook = null;
            sInstance = null;
            this.mRegistrants.removeCleared();
        } else {
            Log.v(LOG_TAG, "dispose mRefCount = " + mRefCount);
        }
    }

    public void handleMessage(Message msg) {
        Log.i(LOG_TAG, "received message : " + msg.what);
        AsyncResult ar = (AsyncResult) msg.obj;
        switch (msg.what) {
            case UNSOL_TYPE_STATE_CHANGE /*1*/:
                HashMap<Integer, Object> map = (HashMap<Integer, Object>) ar.result;
                if (map == null) {
                    Log.e(LOG_TAG, "Hashmap async userobj is NULL");
                    return;
                } else {
                    handleResponse(map);
                    return;
                }
            case UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
                notifyUnsol(UNSOL_TYPE_EMBMSOEMHOOK_READY_CALLBACK, ar.result, SUCCESS);
                return;
            default:
                Log.e(LOG_TAG, "Unexpected message received from QmiOemHook what = " + msg.what);
                return;
        }
    }

    private void handleResponse(HashMap<Integer, Object> map) {
        short msgId = ((Short) map.get(Integer.valueOf(UNSOL_TYPE_SAI_LIST))).shortValue();
        int responseSize = ((Integer) map.get(Integer.valueOf(UNSOL_TYPE_ACTIVE_TMGI_LIST))).intValue();
        int successStatus = ((Integer) map.get(Integer.valueOf(UNSOL_TYPE_BROADCAST_COVERAGE))).intValue();
        Message msg = (Message) map.get(Integer.valueOf(UNSOL_TYPE_AVAILABLE_TMGI_LIST));
        int phoneId = ((Integer) map.get(Integer.valueOf(UNSOL_TYPE_SIB16_COVERAGE))).intValue();
        if (msg != null) {
            msg.arg1 = phoneId;
        }
        ByteBuffer respByteBuf = (ByteBuffer) map.get(Integer.valueOf(UNSOL_TYPE_CELL_ID));
        Log.d(LOG_TAG, " responseSize=" + responseSize + " successStatus=" + successStatus + "phoneId: " + phoneId);
        switch (msgId) {
            case SUCCESS /*0*/:
                msg.obj = new EnableResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case UNSOL_TYPE_STATE_CHANGE /*1*/:
                msg.obj = new DisableResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case UNSOL_TYPE_ACTIVE_TMGI_LIST /*2*/:
            case UNSOL_TYPE_BROADCAST_COVERAGE /*3*/:
                msg.obj = new TmgiResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case UNSOL_TYPE_AVAILABLE_TMGI_LIST /*4*/:
            case (short) 15:
                if (msgId != TLV_TYPE_UNSOL_SAI_IND_AVAILABLE_SAI_LIST || successStatus == 0) {
                    notifyUnsol(UNSOL_TYPE_AVAILABLE_TMGI_LIST, new TmgiListIndication(respByteBuf, msgId), phoneId);
                    return;
                }
                Log.e(LOG_TAG, "Error received in EMBMSHOOK_MSG_ID_GET_AVAILABLE: " + successStatus);
                return;
            case UNSOL_TYPE_OOS_STATE /*5*/:
            case UNSOL_TYPE_EMBMS_STATUS /*12*/:
                if (msgId != EMBMSHOOK_MSG_ID_GET_ACTIVE || successStatus == 0) {
                    notifyUnsol(UNSOL_TYPE_ACTIVE_TMGI_LIST, new TmgiListIndication(respByteBuf, msgId), phoneId);
                    return;
                }
                Log.e(LOG_TAG, "Error received in EMBMSHOOK_MSG_ID_GET_ACTIVE: " + successStatus);
                return;
            case UNSOL_TYPE_SAI_LIST /*8*/:
            case UNSOL_TYPE_GET_INTERESTED_TMGI_LIST /*13*/:
                if (msgId != EMBMSHOOK_MSG_ID_GET_COVERAGE || successStatus == 0) {
                    notifyUnsol(UNSOL_TYPE_BROADCAST_COVERAGE, new CoverageState(respByteBuf, msgId), phoneId);
                    return;
                }
                Log.e(LOG_TAG, "Error received in EMBMSHOOK_MSG_ID_GET_COVERAGE: " + successStatus);
                return;
            case UNSOL_TYPE_SIB16_COVERAGE /*9*/:
                msg.obj = new SigStrengthResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case UNSOL_TYPE_CONTENT_DESC_PER_OBJ_CONTROL /*11*/:
                notifyUnsol(UNSOL_TYPE_STATE_CHANGE, new StateChangeInfo(respByteBuf), phoneId);
                return;
            case (short) 16:
                notifyUnsol(UNSOL_TYPE_OOS_STATE, new OosState(respByteBuf), phoneId);
                return;
            case (short) 17:
                msg.obj = new ActDeactResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 18:
                notifyUnsol(UNSOL_TYPE_CELL_ID, new CellIdIndication(respByteBuf), phoneId);
                return;
            case (short) 19:
                notifyUnsol(UNSOL_TYPE_RADIO_STATE, new RadioStateIndication(respByteBuf), phoneId);
                return;
            case (short) 20:
                notifyUnsol(UNSOL_TYPE_SAI_LIST, new SaiIndication(respByteBuf), phoneId);
                return;
            case (short) 21:
                msg.obj = new ActiveLogPacketIDsResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 22:
                Log.v(LOG_TAG, " deliverLogPacket response successStatus=" + successStatus);
                return;
            case (short) 23:
                msg.arg1 = successStatus;
                msg.sendToTarget();
                return;
            case (short) 24:
            case (short) 25:
                if (msgId != EMBMSHOOK_MSG_ID_GET_SIB16_COVERAGE || successStatus == 0) {
                    notifyUnsol(UNSOL_TYPE_SIB16_COVERAGE, new Sib16Coverage(respByteBuf), phoneId);
                    return;
                }
                Log.e(LOG_TAG, "Error received in EMBMSHOOK_MSG_ID_GET_SIB16_COVERAGE: " + successStatus);
                return;
            case (short) 26:
                msg.obj = new TimeResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 27:
            case (short) 28:
                notifyUnsol(UNSOL_TYPE_E911_STATE, new E911StateIndication(respByteBuf, msgId), phoneId);
                return;
            case (short) 29:
                Log.v(LOG_TAG, " contentDescription response successStatus=" + successStatus);
                return;
            case (short) 30:
                notifyUnsol(UNSOL_TYPE_CONTENT_DESC_PER_OBJ_CONTROL, new ContentDescPerObjectControlIndication(respByteBuf), phoneId);
                return;
            case (short) 31:
                msg.obj = new GetPLMNListResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 32:
            case (short) 33:
                notifyUnsol(UNSOL_TYPE_EMBMS_STATUS, new EmbmsStatus(respByteBuf, msgId), phoneId);
                return;
            case (short) 34:
                notifyUnsol(UNSOL_TYPE_GET_INTERESTED_TMGI_LIST, new RequestIndication(respByteBuf), phoneId);
                return;
            case (short) 35:
                Log.v(LOG_TAG, " getInterestedTmgiListResponse ack successStatus=" + successStatus);
                return;
            default:
                Log.e(LOG_TAG, "received unexpected msgId " + msgId);
                return;
        }
    }

    private void notifyUnsol(int type, Object payload, int phoneId) {
        AsyncResult ar = new AsyncResult(null, new UnsolObject(type, payload, phoneId), null);
        Log.i(LOG_TAG, "Notifying registrants type = " + type);
        this.mRegistrants.notifyRegistrants(ar);
    }

    public void registerForNotifications(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mRegistrants) {
            Log.i(LOG_TAG, "Adding a registrant");
            this.mRegistrants.add(r);
        }
    }

    public void unregisterForNotifications(Handler h) {
        synchronized (this.mRegistrants) {
            Log.i(LOG_TAG, "Removing a registrant");
            this.mRegistrants.remove(h);
        }
    }

    public int enable(int traceId, Message msg, int phoneId) {
        try {
            Log.i(LOG_TAG, "enable called on PhoneId: " + phoneId);
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_ENABLE, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during enable !!!!!!");
            return FAILURE;
        }
    }

    public int activateTmgi(int traceId, byte callId, byte[] tmgi, int priority, int[] saiList, int[] earfcnList, Message msg, int phoneId) {
        Log.i(LOG_TAG, "activateTmgi called on PhoneId: " + phoneId);
        TmgiActivateRequest req = new TmgiActivateRequest(traceId, callId, tmgi, priority, saiList, earfcnList);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) TWO_BYTES, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during activate !!!!!!");
            return FAILURE;
        }
    }

    public int deactivateTmgi(int traceId, byte callId, byte[] tmgi, Message msg, int phoneId) {
        Log.i(LOG_TAG, "deactivateTmgi called on PhoneId: " + phoneId);
        TmgiDeActivateRequest req = new TmgiDeActivateRequest(traceId, tmgi, callId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) TLV_TYPE_UNSOL_STATE_IND_IF_INDEX, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during deactivate !!!!!!");
            return FAILURE;
        }
    }

    public int actDeactTmgi(int traceId, byte callId, byte[] actTmgi, byte[] deActTmgi, int priority, int[] saiList, int[] earfcnList, Message msg, int phoneId) {
        Log.i(LOG_TAG, "actDeactTmgi called on PhoneId: " + phoneId);
        ActDeactRequest req = new ActDeactRequest(traceId, callId, actTmgi, deActTmgi, priority, saiList, earfcnList);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_STATUS_CONTROL, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during activate-deactivate !!!!!!");
            return FAILURE;
        }
    }

    public int getAvailableTMGIList(int traceId, byte callId, int phoneId) {
        Log.i(LOG_TAG, "getAvailableTMGIList called on PhoneId: " + phoneId);
        GenericRequest req = new GenericRequest(traceId, callId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) TLV_TYPE_UNSOL_SAI_IND_AVAILABLE_SAI_LIST, req.getTypes(), req.getItems(), null, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getAvailableTMGIList !!!!!!");
            return FAILURE;
        }
    }

    public int getActiveTMGIList(int traceId, byte callId, int phoneId) {
        Log.i(LOG_TAG, "getActiveTMGIList called on PhoneId: " + phoneId);
        GenericRequest req = new GenericRequest(traceId, callId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_ACTIVE, req.getTypes(), req.getItems(), null, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getActiveTMGIList !!!!!!");
            return FAILURE;
        }
    }

    public int getCoverageState(int traceId, int phoneId) {
        Log.i(LOG_TAG, "getCoverageState called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_COVERAGE, req.getTypes(), req.getItems(), null, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getActiveTMGIList !!!!!!");
            return FAILURE;
        }
    }

    public int getSignalStrength(int traceId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getSignalStrength called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_SIG_STRENGTH, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during enable !!!!!!");
            return FAILURE;
        }
    }

    public int disable(int traceId, byte callId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "disable called on PhoneId: " + phoneId);
        GenericRequest req = new GenericRequest(traceId, callId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) TLV_TYPE_UNSOL_STATE_IND_STATE, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during disable !!!!!!");
            return FAILURE;
        }
    }

    public int getActiveLogPacketIDs(int traceId, int[] supportedLogPacketIdList, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getActiveLogPacketIDs called on PhoneId: " + phoneId);
        ActiveLogPacketIDsRequest req = new ActiveLogPacketIDsRequest(traceId, supportedLogPacketIdList);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_ACTIVE_LOG_PACKET_IDS, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during activate log packet ID's !!!!!!");
            return FAILURE;
        }
    }

    public int deliverLogPacket(int traceId, int logPacketId, byte[] logPacket, int phoneId) {
        Log.i(LOG_TAG, "deliverLogPacket called on PhoneId: " + phoneId);
        DeliverLogPacketRequest req = new DeliverLogPacketRequest(traceId, logPacketId, logPacket);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_DELIVER_LOG_PACKET, req.getTypes(), req.getItems(), null, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during deliver logPacket !!!!!!");
            return FAILURE;
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(bytes.length * UNSOL_TYPE_ACTIVE_TMGI_LIST);
        for (int i = SUCCESS; i < bytes.length; i += UNSOL_TYPE_STATE_CHANGE) {
            ret.append("0123456789abcdef".charAt((bytes[i] >> UNSOL_TYPE_AVAILABLE_TMGI_LIST) & 15));
            ret.append("0123456789abcdef".charAt(bytes[i] & 15));
        }
        return ret.toString();
    }

    public int getTime(int traceId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getTime called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_TIME, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getTime !!!!!!");
            return FAILURE;
        }
    }

    public int getSib16CoverageStatus(Message msg, int phoneId) {
        Log.i(LOG_TAG, "getSib16CoverageStatus called on PhoneId: " + phoneId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync(TWO_BYTES, EMBMSHOOK_MSG_ID_GET_SIB16_COVERAGE, msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getSIB16 !!!!!!");
            return FAILURE;
        }
    }

    public int getEmbmsStatus(int traceId, int phoneId) {
        Log.i(LOG_TAG, "getEmbmsStatus called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_EMBMS_STATUS, req.getTypes(), req.getItems(), null, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getEmbmsStatus !!!!!!");
            return FAILURE;
        }
    }

    public int setTime(boolean sntpSuccess, long timeMseconds, long timeStamp, Message msg, int phoneId) {
        Log.i(LOG_TAG, "setTime called on PhoneId: " + phoneId);
        byte success = (byte) 0;
        if (sntpSuccess) {
            success = TLV_TYPE_SET_TIME_REQ_SNTP_SUCCESS;
        }
        Log.i(LOG_TAG, "setTime success = " + success + " timeMseconds = " + timeMseconds + " timeStamp = " + timeStamp);
        SetTimeRequest req = new SetTimeRequest(success, timeMseconds, timeStamp);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_SET_TIME, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occured during setTime !!!!!!");
            return FAILURE;
        }
    }

    public int getE911State(int traceId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getE911State called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_E911_STATE, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getE911State !!!!!!");
            return FAILURE;
        }
    }

    public int contentDescription(int traceId, byte callId, byte[] tmgi, int numberOfParameter, int[] parameterCode, int[] parameterValue, Message msg, int phoneId) {
        try {
            Log.i(LOG_TAG, "contentDescription called on PhoneId: " + phoneId);
            if (parameterCode == null || parameterValue == null) {
                Log.i(LOG_TAG, "contentDescription: either parameterCode or parameterValue is nullparameterCode = " + parameterCode + " parameterValue = " + parameterValue);
                parameterCode = new int[1];
                parameterValue = new int[1];
            }
            if (numberOfParameter == parameterCode.length && numberOfParameter == parameterValue.length && parameterCode.length == parameterValue.length) {
                int parameterArraySize = numberOfParameter * UNSOL_TYPE_ACTIVE_TMGI_LIST;
                int pointer = SUCCESS;
                int[] parameterArray = new int[parameterArraySize];
                for (int i = SUCCESS; i < parameterArraySize; i += UNSOL_TYPE_ACTIVE_TMGI_LIST) {
                    parameterArray[i] = parameterCode[pointer];
                    parameterArray[i + UNSOL_TYPE_STATE_CHANGE] = parameterValue[pointer];
                    pointer += UNSOL_TYPE_STATE_CHANGE;
                }
                Log.i(LOG_TAG, "contentDescription: parameterArray: " + Arrays.toString(parameterArray));
                ContentDescriptionReq req = new ContentDescriptionReq(traceId, callId, tmgi, parameterArray);
                this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_CONTENT_DESCRIPTION, req.getTypes(), req.getItems(), msg, phoneId);
                return SUCCESS;
            }
            Log.e(LOG_TAG, "contentDescription: Invalid input, numberOfParameter = " + numberOfParameter + " parameterCode = " + parameterCode + " parameterValue = " + parameterValue);
            return FAILURE;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during contentDescription !!!!!!");
            return FAILURE;
        }
    }

    public int getPLMNListRequest(int traceId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getPLMNListRequest called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_PLMN_LIST, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getPLMNListRequest !!!!!!");
            return FAILURE;
        }
    }

    public int getInterestedTMGIListResponse(int traceId, byte callId, byte[] tmgiList, int phoneId, Message msg) {
        try {
            GetInterestedTmgiResponse req = new GetInterestedTmgiResponse(traceId, callId, tmgiList);
            this.mQmiOemHook.sendQmiMessageAsync((short) TWO_BYTES, (short) EMBMSHOOK_MSG_ID_GET_INTERESTED_TMGI_LIST_RESP, req.getTypes(), req.getItems(), msg, phoneId);
            return SUCCESS;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getInterestedTMGIListResponse !!!!!!");
            return FAILURE;
        }
    }

    private byte[] parseTmgi(ByteBuffer buf) {
        int index = SUCCESS;
        byte totalTmgis = buf.get();
        byte[] tmgi = new byte[(totalTmgis * UNSOL_TYPE_CELL_ID)];
        byte i = (byte) 0;
        while (i < totalTmgis) {
            byte tmgiLength = buf.get();
            byte j = (byte) 0;
            int index2 = index;
            while (j < tmgiLength) {
                index = index2 + UNSOL_TYPE_STATE_CHANGE;
                tmgi[index2] = buf.get();
                j += UNSOL_TYPE_STATE_CHANGE;
                index2 = index;
            }
            i += UNSOL_TYPE_STATE_CHANGE;
            index = index2;
        }
        return tmgi;
    }

    private byte[] parseActiveTmgi(ByteBuffer buf) {
        int index = SUCCESS;
        short totalTmgis = buf.getShort();
        byte[] tmgi = new byte[(totalTmgis * UNSOL_TYPE_CELL_ID)];
        short i = EMBMSHOOK_MSG_ID_ENABLE;
        while (i < totalTmgis) {
            byte tmgiLength = buf.get();
            byte j = (byte) 0;
            int index2 = index;
            while (j < tmgiLength) {
                index = index2 + UNSOL_TYPE_STATE_CHANGE;
                tmgi[index2] = buf.get();
                j += UNSOL_TYPE_STATE_CHANGE;
                index2 = index;
            }
            i += UNSOL_TYPE_STATE_CHANGE;
            index = index2;
        }
        return tmgi;
    }

    private QmiArray<QmiByte> byteArrayToQmiArray(short vSize, byte[] arr) {
        BaseQmiItemType[] qmiByteArray = new QmiByte[arr.length];
        for (int i = SUCCESS; i < arr.length; i += UNSOL_TYPE_STATE_CHANGE) {
            qmiByteArray[i] = new QmiByte(arr[i]);
        }
        return new QmiArray(qmiByteArray, QmiByte.class, vSize);
    }

    private QmiArray<QmiByte> tmgiListArrayToQmiArray(short vSize, byte[] tmgiList) {
        int length = tmgiList == null ? SUCCESS : tmgiList.length;
        int numOfTmgi = length / UNSOL_TYPE_CELL_ID;
        QmiByte[] qmiByteArray = new QmiByte[(length + (numOfTmgi * UNSOL_TYPE_STATE_CHANGE))];
        int index = SUCCESS;
        for (int i = SUCCESS; i < numOfTmgi; i += UNSOL_TYPE_STATE_CHANGE) {
            int index2 = index + UNSOL_TYPE_STATE_CHANGE;
            qmiByteArray[index] = new QmiByte((int) UNSOL_TYPE_CELL_ID);
            int j = i * UNSOL_TYPE_CELL_ID;
            index = index2;
            while (j < (i + UNSOL_TYPE_STATE_CHANGE) * UNSOL_TYPE_CELL_ID) {
                index2 = index + UNSOL_TYPE_STATE_CHANGE;
                qmiByteArray[index] = new QmiByte(tmgiList[j]);
                j += UNSOL_TYPE_STATE_CHANGE;
                index = index2;
            }
        }
        return new QmiArray(qmiByteArray, QmiByte.class, vSize, (short) 7);
    }

    private QmiArray<QmiInteger> intArrayToQmiArray(short vSize, int[] arr) {
        int length = arr == null ? SUCCESS : arr.length;
        BaseQmiItemType[] qmiIntArray = new QmiInteger[length];
        for (int i = SUCCESS; i < length; i += UNSOL_TYPE_STATE_CHANGE) {
            qmiIntArray[i] = new QmiInteger((long) arr[i]);
        }
        return new QmiArray(qmiIntArray, QmiInteger.class, vSize);
    }

    private QmiArray<QmiInteger> intArrayToQmiArray(short vSize, int[] arr, short numOfElements) {
        int length = arr == null ? SUCCESS : arr.length;
        QmiInteger[] qmiIntArray = new QmiInteger[length];
        for (int i = SUCCESS; i < length; i += UNSOL_TYPE_STATE_CHANGE) {
            qmiIntArray[i] = new QmiInteger((long) arr[i]);
        }
        return new QmiArray(qmiIntArray, QmiInteger.class, vSize, numOfElements);
    }
}
