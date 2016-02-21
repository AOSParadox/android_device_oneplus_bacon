package com.qualcomm.qcrilhook;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.qualcomm.qcrilhook.QmiOemHookConstants.ResponseType;
import com.qualcomm.qcrilhook.PresenceMsgBuilder.*;
import com.qualcomm.qcrilhook.PresenceMsgBuilder.Publish.*;
import com.qualcomm.qcrilhook.PresenceMsgBuilder.UnSubscribe.UnSubscribeRequest;
import com.qualcomm.qcrilhook.PresenceMsgBuilder.UnPublish.UnPublishRequest;
import com.qualcomm.qcrilhook.PresenceMsgBuilder.Subscribe.*;
import com.qualcomm.qcrilhook.PresenceMsgBuilder.NotifyFmt.SetFmt;
import com.qualcomm.qcrilhook.PresenceMsgBuilder.EventReport.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class PresenceOemHook {
    public static final String[] IMS_ENABLER_RESPONSE = new String[]{"UNKNOWN", "UNINITIALIZED", "INITIALIZED", "AIRPLANE", "REGISTERED"};
    private static String LOG_TAG = "PresenceOemHook";
    public static final int OEM_HOOK_UNSOL_IND = 1;
    private static final short PRESENCE_SERVICE_ID = (short) 3;
    public static final short QCRILHOOK_PRESENCE_IMS_ENABLER_STATE_REQ = (short) 36;
    public static final short QCRILHOOK_PRESENCE_IMS_GET_EVENT_REPORT_REQ = (short) 46;
    public static final short QCRILHOOK_PRESENCE_IMS_GET_NOTIFY_FMT_REQ = (short) 44;
    public static final short QCRILHOOK_PRESENCE_IMS_SEND_PUBLISH_REQ = (short) 37;
    public static final short QCRILHOOK_PRESENCE_IMS_SEND_PUBLISH_XML_REQ = (short) 38;
    public static final short QCRILHOOK_PRESENCE_IMS_SEND_SUBSCRIBE_REQ = (short) 40;
    public static final short QCRILHOOK_PRESENCE_IMS_SEND_SUBSCRIBE_XML_REQ = (short) 41;
    public static final short QCRILHOOK_PRESENCE_IMS_SEND_UNPUBLISH_REQ = (short) 39;
    public static final short QCRILHOOK_PRESENCE_IMS_SEND_UNSUBSCRIBE_REQ = (short) 42;
    public static final short QCRILHOOK_PRESENCE_IMS_SET_EVENT_REPORT_REQ = (short) 45;
    public static final short QCRILHOOK_PRESENCE_IMS_SET_NOTIFY_FMT_REQ = (short) 43;
    public static final short QCRILHOOK_PRESENCE_IMS_UNSOL_ENABLER_STATE = (short) 35;
    public static final short QCRILHOOK_PRESENCE_IMS_UNSOL_NOTIFY_UPDATE = (short) 34;
    public static final short QCRILHOOK_PRESENCE_IMS_UNSOL_NOTIFY_XML_UPDATE = (short) 33;
    public static final short QCRILHOOK_PRESENCE_IMS_UNSOL_PUBLISH_TRIGGER = (short) 32;
    private static PresenceOemHook mInstance;
    private static int mRefCount = 0;
    Context mContext;
    private QmiOemHook mQmiOemHook;

    public static class PresenceSolResponse {
        public Object data;
        public int result;
    }

    public static class PresenceUnsolIndication {
        public Object obj;
        public int oemHookMesgId;
    }

    public enum SubscriptionType {
        NONE,
        SIMPLE,
        POLLING
    }

    private PresenceOemHook(Context context, Looper listenerLooper) {
        this.mContext = context;
        this.mQmiOemHook = QmiOemHook.getInstance(context, listenerLooper);
    }

    public static PresenceOemHook getInstance(Context context, Handler listenerHandler) {
        if (mInstance == null) {
            mInstance = new PresenceOemHook(context, listenerHandler.getLooper());
            QmiOemHook.registerService(PRESENCE_SERVICE_ID, listenerHandler, OEM_HOOK_UNSOL_IND);
            Log.v(LOG_TAG, "Registered PresenceOemHook with QmiOemHook");
        } else {
            mInstance.mContext = context;
        }
        mRefCount += OEM_HOOK_UNSOL_IND;
        return mInstance;
    }

    public synchronized void dispose() {
        mRefCount--;
        if (mRefCount == 0) {
            Log.v(LOG_TAG, "dispose(): Unregistering service");
            QmiOemHook.unregisterService(3);
            this.mQmiOemHook.dispose();
            mInstance = null;
        } else {
            Log.v(LOG_TAG, "dispose mRefCount = " + mRefCount);
        }
    }

    public Object imsp_get_enabler_state_req() {
        NoTlvPayloadRequest req = new NoTlvPayloadRequest();
        try {
            return receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_ENABLER_STATE_REQ, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer imsp_send_publish_req(int publish_status, String contact_uri, String description, String ver, String service_id, int is_audio_supported, int audio_capability, int is_video_supported, int video_capability) {
        PublishStructRequest req = new PublishStructRequest(publish_status, contact_uri, description, ver, service_id, is_audio_supported, audio_capability, is_video_supported, video_capability);
        try {
            return (Integer) receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_SEND_PUBLISH_REQ, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer imsp_send_publish_xml_req(String xml) {
        PublishXMLRequest req = new PublishXMLRequest(xml);
        try {
            return (Integer) receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_SEND_PUBLISH_XML_REQ, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer imsp_send_unpublish_req() {
        UnPublishRequest req = new UnPublishRequest();
        try {
            return (Integer) receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_SEND_UNPUBLISH_REQ, null, null));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer imsp_send_subscribe_req(SubscriptionType subscriptionType, ArrayList<String> contactList) {
        SubscribeStructRequest req = new SubscribeStructRequest(subscriptionType, contactList);
        try {
            return (Integer) receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_SEND_SUBSCRIBE_REQ, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer imsp_send_subscribe_xml_req(String xml) {
        SubscribeXMLRequest req = new SubscribeXMLRequest(xml);
        try {
            return (Integer) receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_SEND_SUBSCRIBE_XML_REQ, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer imsp_send_unsubscribe_req(String peerURI) {
        UnSubscribeRequest req = new UnSubscribeRequest(peerURI);
        try {
            return (Integer) receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_SEND_UNSUBSCRIBE_REQ, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object imsp_set_notify_fmt_req(int flag) {
        SetFmt req = new SetFmt((short) flag);
        try {
            return receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_SET_NOTIFY_FMT_REQ, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object imsp_get_notify_fmt_req() {
        Object obj = null;
        NoTlvPayloadRequest req = new NoTlvPayloadRequest();
        try {
            obj = receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_GET_NOTIFY_FMT_REQ, null, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public Object imsp_set_event_report_req(int mask) {
        SetEventReport req = new SetEventReport(mask);
        try {
            return receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_SET_EVENT_REPORT_REQ, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object imsp_get_event_report_req() {
        Object obj = null;
        NoTlvPayloadRequest req = new NoTlvPayloadRequest();
        try {
            obj = receive(this.mQmiOemHook.sendQmiMessageSync((short) PRESENCE_SERVICE_ID, (short) QCRILHOOK_PRESENCE_IMS_GET_EVENT_REPORT_REQ, null, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Object receive(HashMap<Integer, Object> map) {
        int requestId = ((Integer) map.get(Integer.valueOf(OEM_HOOK_UNSOL_IND))).intValue();
        int responseSize = ((Integer) map.get(Integer.valueOf(2))).intValue();
        int successStatus = ((Integer) map.get(Integer.valueOf(3))).intValue();
        short messageId = ((Short) map.get(Integer.valueOf(8))).shortValue();
        ResponseType respType = (ResponseType) map.get(Integer.valueOf(5));
        Message msg = (Message) map.get(Integer.valueOf(4));
        ByteBuffer respByteBuf = (ByteBuffer) map.get(Integer.valueOf(6));
        Log.v(LOG_TAG, "receive respByteBuf = " + respByteBuf);
        Log.v(LOG_TAG, " responseSize=" + responseSize + " successStatus=" + successStatus + " messageId= " + messageId);
        Integer returnObject = Integer.valueOf(successStatus);
        int val;
        PresenceUnsolIndication ind;
        Object returnObject2 = returnObject;
        PresenceUnsolIndication presenceUnSolInd;
        PresenceSolResponse presenceSolResp;
        switch (messageId) {
            case (short) 32:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_UNSOL_PUBLISH_TRIGGER=" + successStatus);
                val = PresenceMsgParser.parsePublishTrigger(respByteBuf);
                ind = new PresenceUnsolIndication();
                ind.oemHookMesgId = 32;
                ind.obj = Integer.valueOf(val);
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_UNSOL_PUBLISH_TRIGGER result=" + successStatus + " publish_trigger=" + val);
                returnObject2 = ind;
                break;
            case (short) 33:
                Log.v(LOG_TAG, "Ind: QCRILHOOK_PRESENCE_IMS_UNSOL_NOTIFY_XML_UPDATE=" + successStatus);
                String xml = PresenceMsgParser.parseNotifyUpdateXML(respByteBuf);
                presenceUnSolInd = new PresenceUnsolIndication();
                presenceUnSolInd.oemHookMesgId = 33;
                presenceUnSolInd.obj = xml;
                returnObject2 = presenceUnSolInd;
                break;
            case (short) 34:
                Log.v(LOG_TAG, "Ind: QCRILHOOK_PRESENCE_IMS_UNSOL_NOTIFY_UPDATE=" + successStatus);
                presenceUnSolInd = new PresenceUnsolIndication();
                presenceUnSolInd.oemHookMesgId = 34;
                presenceUnSolInd.obj = PresenceMsgParser.parseNotifyUpdate(respByteBuf, responseSize, successStatus);
                returnObject2 = presenceUnSolInd;
                break;
            case (short) 35:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_UNSOL_ENABLER_STATE=" + successStatus);
                val = PresenceMsgParser.parseEnablerStateInd(respByteBuf);
                ind = new PresenceUnsolIndication();
                ind.oemHookMesgId = 35;
                ind.obj = Integer.valueOf(val);
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_UNSOL_ENABLER_STATE result=" + successStatus + " enabler_state=" + val);
                returnObject2 = ind;
                break;
            case (short) 36:
                PresenceSolResponse presenceSolResp2 = new PresenceSolResponse();
                if (successStatus == 0) {
                    int enablerState = PresenceMsgParser.parseEnablerState(respByteBuf);
                    Log.v(LOG_TAG, "Enabler state = " + enablerState);
                    presenceSolResp2.result = successStatus;
                    presenceSolResp2.data = Integer.valueOf(enablerState);
                    returnObject2 = presenceSolResp2;
                    Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_ENABLER_STATE_REQ=" + IMS_ENABLER_RESPONSE[enablerState]);
                    break;
                }
                Log.v(LOG_TAG, "OemHookError: QCRILHOOK_PRESENCE_IMS_ENABLER_STATE_REQ=" + successStatus);
                presenceSolResp2.result = successStatus;
                presenceSolResp2.data = Integer.valueOf(0);
                return presenceSolResp2;
            case (short) 37:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_SEND_PUBLISH_REQ=" + successStatus);
                break;
            case (short) 38:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_SEND_PUBLISH_XML_REQ=" + successStatus);
                break;
            case (short) 39:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_SEND_UNPUBLISH_REQ=" + successStatus);
                break;
            case (short) 40:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_SEND_SUBSCRIBE_REQ=" + successStatus);
                break;
            case (short) 41:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_SEND_SUBSCRIBE_XML_REQ=" + successStatus);
                break;
            case (short) 42:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_SEND_UNSUBSCRIBE_REQ=" + successStatus);
                break;
            case (short) 43:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_SET_NOTIFY_FMT_REQ=" + successStatus);
                presenceSolResp = new PresenceSolResponse();
                presenceSolResp.result = successStatus;
                presenceSolResp.data = Integer.valueOf(-1);
                returnObject2 = presenceSolResp;
                break;
            case (short) 44:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_GET_NOTIFY_FMT_REQ=" + successStatus);
                presenceSolResp = new PresenceSolResponse();
                if (successStatus == 0) {
                    val = PresenceMsgParser.parseGetNotifyReq(respByteBuf);
                    presenceSolResp.result = successStatus;
                    presenceSolResp.data = Integer.valueOf(val);
                    Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_GET_NOTIFY_FMT_REQ update_with_struct_info=" + val);
                } else {
                    presenceSolResp.result = successStatus;
                    presenceSolResp.data = Integer.valueOf(-1);
                }
                returnObject2 = presenceSolResp;
                break;
            case (short) 45:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_SET_EVENT_REPORT_REQ=" + successStatus);
                presenceSolResp = new PresenceSolResponse();
                presenceSolResp.result = successStatus;
                presenceSolResp.data = Integer.valueOf(-1);
                returnObject2 = presenceSolResp;
                break;
            case (short) 46:
                Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_GET_EVENT_REPORT_REQ=" + successStatus);
                presenceSolResp = new PresenceSolResponse();
                if (successStatus == 0) {
                    val = PresenceMsgParser.parseGetEventReport(respByteBuf);
                    presenceSolResp.result = successStatus;
                    presenceSolResp.data = Integer.valueOf(val);
                    Log.v(LOG_TAG, "Response: QCRILHOOK_PRESENCE_IMS_GET_EVENT_REPORT_REQ event_report_bit_masks=" + val);
                } else {
                    presenceSolResp.result = successStatus;
                    presenceSolResp.data = Integer.valueOf(-1);
                }
                returnObject2 = presenceSolResp;
                break;
        }
        return returnObject2;
    }

    public static Object handleMessage(Message msg) {
        switch (msg.what) {
            case OEM_HOOK_UNSOL_IND /*1*/:
            	AsyncResult ar = (AsyncResult) msg.obj;
                HashMap<Integer, Object> map = (HashMap<Integer, Object>) ar.result;
                if (map == null ) {
                	//hack by omerjerk
                	map = (HashMap<Integer, Object>) ar.userObj;
                }
                if (map != null) {
                    return receive(map);
                }
                Log.e(LOG_TAG, "Hashmap async userobj is NULL");
                return null;
            default:
                Log.d(LOG_TAG, "Recieved msg.what=" + msg.what);
                return null;
        }
    }

    protected void finalize() {
        Log.v(LOG_TAG, "finalize() hit");
    }
}
