package com.qualcomm.qcrilhook;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.uicc.IccUtils;
import com.qualcomm.qcrilmsgtunnel.IQcrilMsgTunnel;
import com.qualcomm.qcrilmsgtunnel.IQcrilMsgTunnel.Stub;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class QcRilHook implements IQcRilHook {
    public static final String ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW = "com.qualcomm.intent.action.ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW";
    private static final int AVOIDANCE_BUFF_LEN = 164;
    private static final int BYTE_SIZE = 1;
    private static final int DEFAULT_PHONE = 0;
    private static final int INT_SIZE = 4;
    private static final String LOG_TAG = "QC_RIL_OEM_HOOK";
    private static final int MAX_PDC_ID_LEN = 124;
    private static final int MAX_REQUEST_BUFFER_SIZE = 1024;
    private static final int MAX_SPC_LEN = 6;
    public static final String QCRIL_MSG_TUNNEL_PACKAGE_NAME = "com.qualcomm.qcrilmsgtunnel";
    public static final String QCRIL_MSG_TUNNEL_SERVICE_NAME = "com.qualcomm.qcrilmsgtunnel.QcrilMsgTunnelService";
    private static final int RESPONSE_BUFFER_SIZE = 2048;
    private static RegistrantList mRegistrants;
    private final String ENCODING;
    private boolean mBound;
    private Context mContext;
    private final int mHeaderSize;
    private BroadcastReceiver mIntentReceiver;
    private final String mOemIdentifier;
    private QcRilHookCallback mQcrilHookCb;
    private ServiceConnection mQcrilMsgTunnelConnection;
    private IQcrilMsgTunnel mService;

    @Deprecated
    public QcRilHook(Context context) {
        this(context, null);
    }

    public QcRilHook(Context context, QcRilHookCallback cb) {
        this.mOemIdentifier = QmiOemHookConstants.OEM_IDENTIFIER;
        this.mHeaderSize = QmiOemHookConstants.OEM_IDENTIFIER.length() + 8;
        this.mService = null;
        this.mBound = false;
        this.mQcrilHookCb = null;
        this.ENCODING = "ISO-8859-1";
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(QcRilHook.ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW)) {
                    Log.d(QcRilHook.LOG_TAG, "Received Broadcast Intent ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW");
                    byte[] payload = intent.getByteArrayExtra("payload");
                    int instanceId = intent.getIntExtra(QmiOemHookConstants.INSTANCE_ID, QcRilHook.DEFAULT_PHONE);
                    if (payload == null) {
                        return;
                    }
                    if (payload.length < QcRilHook.this.mHeaderSize) {
                        Log.e(QcRilHook.LOG_TAG, "UNSOL_RESPONSE_OEM_HOOK_RAW incomplete header");
                        Log.e(QcRilHook.LOG_TAG, "Expected " + QcRilHook.this.mHeaderSize + " bytes. Received " + payload.length + " bytes.");
                        return;
                    }
                    ByteBuffer response = QcRilHook.createBufferWithNativeByteOrder(payload);
                    byte[] oem_id_bytes = new byte[QmiOemHookConstants.OEM_IDENTIFIER.length()];
                    response.get(oem_id_bytes);
                    String oem_id_str = new String(oem_id_bytes);
                    Log.d(QcRilHook.LOG_TAG, "Oem ID in QCRILHOOK UNSOL RESP is " + oem_id_str);
                    if (oem_id_str.equals(QmiOemHookConstants.OEM_IDENTIFIER)) {
                        int remainingSize = payload.length - QmiOemHookConstants.OEM_IDENTIFIER.length();
                        if (remainingSize > 0) {
                            byte[] remainingPayload = new byte[remainingSize];
                            response.get(remainingPayload);
                            Message msg = Message.obtain();
                            msg.obj = remainingPayload;
                            msg.arg1 = instanceId;
                            QcRilHook.notifyRegistrants(new AsyncResult(null, msg, null));
                            return;
                        }
                        return;
                    }
                    Log.w(QcRilHook.LOG_TAG, "Incorrect Oem ID in QCRILHOOK UNSOL RESP. Expected QOEMHOOK. Received " + oem_id_str);
                    return;
                }
                Log.w(QcRilHook.LOG_TAG, "Received Unknown Intent: action = " + action);
            }
        };
        this.mQcrilMsgTunnelConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                QcRilHook.this.mService = Stub.asInterface(service);
                if (QcRilHook.this.mService == null) {
                    Log.e(QcRilHook.LOG_TAG, "QcrilMsgTunnelService Connect Failed (onServiceConnected)");
                } else {
                    Log.d(QcRilHook.LOG_TAG, "QcrilMsgTunnelService Connected Successfully (onServiceConnected)");
                }
                QcRilHook.this.mBound = true;
                if (QcRilHook.this.mQcrilHookCb != null) {
                    Log.d(QcRilHook.LOG_TAG, "Calling onQcRilHookReady callback");
                    QcRilHook.this.mQcrilHookCb.onQcRilHookReady();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(QcRilHook.LOG_TAG, "The connection to the service got disconnected unexpectedly!");
                QcRilHook.this.mService = null;
                QcRilHook.this.mBound = false;
                if (QcRilHook.this.mQcrilHookCb != null) {
                    Log.d(QcRilHook.LOG_TAG, "Calling onQcRilHookDisconnected callback");
                    QcRilHook.this.mQcrilHookCb.onQcRilHookDisconnected();
                }
            }
        };
        this.mQcrilHookCb = cb;
        mRegistrants = new RegistrantList();
        this.mContext = context;
        Intent intent = new Intent();
        intent.setClassName(QCRIL_MSG_TUNNEL_PACKAGE_NAME, QCRIL_MSG_TUNNEL_SERVICE_NAME);
        Log.d(LOG_TAG, "Starting QcrilMsgTunnel Service");
        this.mContext.startService(intent);
        this.mContext.bindService(intent, this.mQcrilMsgTunnelConnection, BYTE_SIZE);
        Log.d(LOG_TAG, "The QcrilMsgTunnelService will be connected soon ");
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW);
            this.mContext.registerReceiver(this.mIntentReceiver, filter);
            Log.d(LOG_TAG, "Registering for intent ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Uncaught Exception while while registering ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW intent. Reason: " + e);
        }
    }

    public void dispose() {
        if (this.mContext != null) {
            if (this.mBound) {
                Log.v(LOG_TAG, "dispose(): Unbinding service");
                this.mContext.unbindService(this.mQcrilMsgTunnelConnection);
                this.mBound = false;
            }
            Log.v(LOG_TAG, "dispose(): Unregistering receiver");
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
    }

    public static ByteBuffer createBufferWithNativeByteOrder(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(ByteOrder.nativeOrder());
        return buf;
    }

    private void addQcRilHookHeader(ByteBuffer buf, int requestId, int requestSize) {
        buf.put(QmiOemHookConstants.OEM_IDENTIFIER.getBytes());
        buf.putInt(requestId);
        buf.putInt(requestSize);
    }

    private AsyncResult sendRilOemHookMsg(int requestId, byte[] request) {
        return sendRilOemHookMsg(requestId, request, DEFAULT_PHONE);
    }

    private AsyncResult sendRilOemHookMsg(int requestId, byte[] request, int phoneId) {
        byte[] response = new byte[RESPONSE_BUFFER_SIZE];
        Log.v(LOG_TAG, "sendRilOemHookMsg: Outgoing Data is " + IccUtils.bytesToHexString(request));
        try {
            int retVal = this.mService.sendOemRilRequestRaw(request, response, phoneId);
            Log.d(LOG_TAG, "sendOemRilRequestRaw returns value = " + retVal);
            byte[] validResponseBytes;
            if (retVal >= 0) {
                validResponseBytes = null;
                if (retVal > 0) {
                    validResponseBytes = new byte[retVal];
                    System.arraycopy(response, DEFAULT_PHONE, validResponseBytes, DEFAULT_PHONE, retVal);
                }
                return new AsyncResult(Integer.valueOf(retVal), validResponseBytes, null);
            }
            validResponseBytes = new byte[response.length];
            System.arraycopy(response, DEFAULT_PHONE, validResponseBytes, DEFAULT_PHONE, response.length);
            return new AsyncResult(request, validResponseBytes, CommandException.fromRilErrno(retVal * -1));
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "sendOemRilRequestRaw RequestID = " + requestId + " exception, unable to send RIL request from this application", e);
            return new AsyncResult(Integer.valueOf(requestId), null, e);
        } catch (NullPointerException e2) {
            Log.e(LOG_TAG, "NullPointerException caught at sendOemRilRequestRaw.RequestID = " + requestId + ". Return Error");
            return new AsyncResult(Integer.valueOf(requestId), null, e2);
        }
    }

    private void sendRilOemHookMsgAsync(int requestId, byte[] request, IOemHookCallback oemHookCb, int phoneId) throws NullPointerException {
        Log.v(LOG_TAG, "sendRilOemHookMsgAsync: Outgoing Data is " + IccUtils.bytesToHexString(request));
        try {
            this.mService.sendOemRilRequestRawAsync(request, oemHookCb, phoneId);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "sendOemRilRequestRawAsync RequestID = " + requestId + " exception, unable to send RIL request from this application", e);
        } catch (NullPointerException e2) {
            Log.e(LOG_TAG, "NullPointerException caught at sendOemRilRequestRawAsync.RequestID = " + requestId + ". Throw to the caller");
            throw e2;
        }
    }

    public String qcRilGetConfig(int phoneId, int mbnType) {
        byte[] payload = new byte[((this.mHeaderSize + INT_SIZE) + BYTE_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_GET_CONFIG, 5);
        reqBuffer.put((byte) phoneId);
        reqBuffer.putInt(mbnType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_CONFIG, payload);
        if (ar.exception != null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_CONFIG failed w/ " + ar.exception);
            return null;
        } else if (ar.result == null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_CONFIG failed w/ null result");
            return null;
        } else {
            try {
                String result = new String((byte[]) ar.result, "ISO-8859-1");
                Log.v(LOG_TAG, "QCRIL_EVT_HOOK_GET_CONFIG returned w/ " + result);
                String str = result;
                return result;
            } catch (UnsupportedEncodingException e) {
                Log.d(LOG_TAG, "unsupport ISO-8859-1");
                return null;
            }
        }
    }

    public String qcRilGetConfig() {
        return qcRilGetConfig(DEFAULT_PHONE);
    }

    public String qcRilGetConfig(int phoneId) {
        return qcRilGetConfig(phoneId, DEFAULT_PHONE);
    }

    public boolean qcRilSetConfig(String file, String config, int subMask) {
        return qcRilSetConfig(file, config, subMask, DEFAULT_PHONE);
    }

    public boolean qcRilSetConfig(String file, String config, int subMask, int mbnType) {
        if (config.isEmpty() || config.length() > MAX_PDC_ID_LEN || file.isEmpty()) {
            Log.e(LOG_TAG, "set with incorrect config id: " + config);
            return false;
        }
        byte[] payload = new byte[((((this.mHeaderSize + 3) + INT_SIZE) + file.length()) + config.length())];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_SET_CONFIG, (file.length() + 7) + config.length());
        buf.put((byte) subMask);
        buf.putInt(mbnType);
        buf.put(file.getBytes());
        buf.put((byte) 0);
        try {
            buf.put(config.getBytes("ISO-8859-1"));
            buf.put((byte) 0);
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_CONFIG, payload);
            if (ar.exception == null) {
                return true;
            }
            Log.e(LOG_TAG, "QCRIL_EVT_HOOK_SET_CONFIG failed w/ " + ar.exception);
            return false;
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "unsupport ISO-8859-1");
            return false;
        }
    }

    public boolean qcRilSetConfig(String file) {
        return qcRilSetConfig(file, file, BYTE_SIZE);
    }

    public boolean qcRilSetConfig(String file, int subMask) {
        return qcRilSetConfig(file, file, subMask);
    }

    public byte[] qcRilGetQcVersionOfFile(String file) {
        if (file.isEmpty()) {
            return null;
        }
        byte[] payload = new byte[(this.mHeaderSize + file.getBytes().length)];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE, file.getBytes().length);
        buf.put(file.getBytes());
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE, payload);
        if (ar.exception != null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE failed w/ " + ar.exception);
            return null;
        } else if (ar.result == null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE failed w/ null result");
            return null;
        } else {
            Log.v(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE returned w/ " + ((byte[]) ar.result));
            return (byte[]) ar.result;
        }
    }

    public byte[] qcRilGetOemVersionOfFile(String file) {
        if (file.isEmpty()) {
            return null;
        }
        byte[] payload = new byte[(this.mHeaderSize + file.getBytes().length)];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE, file.getBytes().length);
        buf.put(file.getBytes());
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE, payload);
        if (ar.exception != null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE failed w/ " + ar.exception);
            return null;
        } else if (ar.result == null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE failed w/ null result");
            return null;
        } else {
            Log.v(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE returned w/ " + ((byte[]) ar.result));
            return (byte[]) ar.result;
        }
    }

    public byte[] qcRilGetQcVersionOfID(String configId) {
        if (configId.isEmpty() || configId.length() > MAX_PDC_ID_LEN) {
            Log.w(LOG_TAG, "invalid config id");
            return null;
        }
        byte[] payload = new byte[(this.mHeaderSize + configId.length())];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID, configId.length());
        try {
            buf.put(configId.getBytes("ISO-8859-1"));
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID, payload);
            if (ar.exception != null) {
                Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID failed w/ " + ar.exception);
                return null;
            } else if (ar.result == null) {
                Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID failed w/ null result");
                return null;
            } else {
                Log.v(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID returned w/ " + ((byte[]) ar.result));
                return (byte[]) ar.result;
            }
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "unsupport ISO-8859-1");
            return null;
        }
    }

    public byte[] qcRilGetOemVersionOfID(String config_id) {
        if (config_id.isEmpty() || config_id.length() > MAX_PDC_ID_LEN) {
            Log.w(LOG_TAG, "invalid config_id");
            return null;
        }
        byte[] payload = new byte[(this.mHeaderSize + config_id.length())];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID, config_id.length());
        try {
            buf.put(config_id.getBytes("ISO-8859-1"));
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID, payload);
            if (ar.exception != null) {
                Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID failed w/ " + ar.exception);
                return null;
            } else if (ar.result == null) {
                Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID failed w/ null result");
                return null;
            } else {
                Log.v(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID returned w/ " + ((byte[]) ar.result));
                return (byte[]) ar.result;
            }
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "unsupport ISO-8859-1");
            return null;
        }
    }

    public boolean qcRilActivateConfig(int phoneId) {
        return qcRilActivateConfig(phoneId, DEFAULT_PHONE);
    }

    public boolean qcRilActivateConfig(int phoneId, int mbnType) {
        byte[] payload = new byte[((this.mHeaderSize + INT_SIZE) + BYTE_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_ACT_CONFIGS, 5);
        reqBuffer.put((byte) phoneId);
        reqBuffer.putInt(mbnType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_ACT_CONFIGS, payload);
        if (ar.exception == null) {
            return true;
        }
        Log.w(LOG_TAG, "QCRIL_EVT_HOOK_ACT_CONFIGS failed w/ " + ar.exception);
        return false;
    }

    public boolean qcRilValidateConfig(String configId, int phoneId) {
        if (configId.isEmpty() || configId.length() > MAX_PDC_ID_LEN) {
            Log.w(LOG_TAG, "invalid config id");
            return false;
        }
        byte[] payload = new byte[((this.mHeaderSize + 2) + configId.length())];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_VALIDATE_CONFIG, configId.length() + 2);
        buf.put((byte) phoneId);
        try {
            buf.put(configId.getBytes("ISO-8859-1"));
            buf.put((byte) 0);
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_META_INFO, payload);
            if (ar.exception == null) {
                return true;
            }
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_VALIDATE_CONFIG failed w/ " + ar.exception);
            return false;
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "unsupport ISO-8859-1");
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String[] qcRilGetAvailableConfigs(java.lang.String r13) {
        /*
        r12 = this;
        r9 = 0;
        r6 = 0;
        r8 = 524311; // 0x80017 float:7.34716E-40 double:2.59044E-318;
        r0 = r12.sendQcRilHookMsg(r8);
        r8 = r0.exception;
        if (r8 == 0) goto L_0x0029;
    L_0x000d:
        r8 = "QC_RIL_OEM_HOOK";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS failed w/ ";
        r10 = r10.append(r11);
        r11 = r0.exception;
        r10 = r10.append(r11);
        r10 = r10.toString();
        android.util.Log.w(r8, r10);
        r8 = r9;
    L_0x0028:
        return r8;
    L_0x0029:
        r8 = r0.result;
        if (r8 != 0) goto L_0x0036;
    L_0x002d:
        r8 = "QC_RIL_OEM_HOOK";
        r9 = "QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS failed w/ null result";
        android.util.Log.e(r8, r9);
        r8 = r6;
        goto L_0x0028;
    L_0x0036:
        r10 = "QC_RIL_OEM_HOOK";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r11 = "QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS raw: ";
        r11 = r8.append(r11);
        r8 = r0.result;
        r8 = (byte[]) r8;
        r8 = (byte[]) r8;
        r8 = java.util.Arrays.toString(r8);
        r8 = r11.append(r8);
        r8 = r8.toString();
        android.util.Log.v(r10, r8);
        r8 = r0.result;	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r8 = (byte[]) r8;	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r8 = (byte[]) r8;	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r5 = java.nio.ByteBuffer.wrap(r8);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r8 = java.nio.ByteOrder.nativeOrder();	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r5.order(r8);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r4 = r5.get();	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r8 = "QC_RIL_OEM_HOOK";
        r10 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r10.<init>();	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r11 = "QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS success: ";
        r10 = r10.append(r11);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r10 = r10.append(r4);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r10 = r10.toString();	 Catch:{ BufferUnderflowException -> 0x00d9 }
        android.util.Log.d(r8, r10);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        if (r4 > 0) goto L_0x0090;
    L_0x0087:
        r8 = "QC_RIL_OEM_HOOK";
        r10 = "QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS failed w/invalid payload, numStrings = 0";
        android.util.Log.e(r8, r10);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r8 = r9;
        goto L_0x0028;
    L_0x0090:
        r6 = new java.lang.String[r4];	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r3 = 0;
    L_0x0093:
        if (r3 >= r4) goto L_0x00f2;
    L_0x0095:
        r7 = r5.get();	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r1 = new byte[r7];	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r5.get(r1);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r8 = new java.lang.String;	 Catch:{ UnsupportedEncodingException -> 0x00ce }
        r10 = "ISO-8859-1";
        r8.<init>(r1, r10);	 Catch:{ UnsupportedEncodingException -> 0x00ce }
        r6[r3] = r8;	 Catch:{ UnsupportedEncodingException -> 0x00ce }
        r8 = "QC_RIL_OEM_HOOK";
        r10 = new java.lang.StringBuilder;	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r10.<init>();	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r11 = "QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS string ";
        r10 = r10.append(r11);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r10 = r10.append(r7);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r11 = " ";
        r10 = r10.append(r11);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r11 = r6[r3];	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r10 = r10.append(r11);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r10 = r10.toString();	 Catch:{ BufferUnderflowException -> 0x00d9 }
        android.util.Log.d(r8, r10);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r3 = r3 + 1;
        goto L_0x0093;
    L_0x00ce:
        r2 = move-exception;
        r8 = "QC_RIL_OEM_HOOK";
        r10 = "unsupport ISO-8859-1";
        android.util.Log.d(r8, r10);	 Catch:{ BufferUnderflowException -> 0x00d9 }
        r8 = r9;
        goto L_0x0028;
    L_0x00d9:
        r2 = move-exception;
        r8 = "QC_RIL_OEM_HOOK";
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS failed to parse payload w/ ";
        r9 = r9.append(r10);
        r9 = r9.append(r2);
        r9 = r9.toString();
        android.util.Log.e(r8, r9);
    L_0x00f2:
        r8 = r6;
        goto L_0x0028;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.qcrilhook.QcRilHook.qcRilGetAvailableConfigs(java.lang.String):java.lang.String[]");
    }

    public boolean qcRilCleanupConfigs() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_DELETE_ALL_CONFIGS);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL_EVT_HOOK_DELETE_ALL_CONFIGS failed w/ " + ar.exception);
        return false;
    }

    public boolean qcRilDeactivateConfigs() {
        return qcRilDeactivateConfigs(DEFAULT_PHONE);
    }

    public boolean qcRilDeactivateConfigs(int mbnType) {
        byte[] payload = new byte[(this.mHeaderSize + INT_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_DEACT_CONFIGS, INT_SIZE);
        reqBuffer.putInt(mbnType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_DEACT_CONFIGS, payload);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL_EVT_HOOK_DEACT_CONFIGS failed w/ " + ar.exception);
        return false;
    }

    public boolean qcRilSelectConfig(String config, int subMask) {
        return qcRilSelectConfig(config, subMask, DEFAULT_PHONE);
    }

    public boolean qcRilSelectConfig(String config, int subMask, int mbnType) {
        if (config.isEmpty() || config.length() > MAX_PDC_ID_LEN) {
            Log.e(LOG_TAG, "select with incorrect config id: " + config);
            return false;
        }
        try {
            byte[] payload = new byte[(((this.mHeaderSize + BYTE_SIZE) + INT_SIZE) + config.getBytes("ISO-8859-1").length)];
            ByteBuffer buf = createBufferWithNativeByteOrder(payload);
            addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_SEL_CONFIG, config.getBytes("ISO-8859-1").length + 5);
            buf.put((byte) subMask);
            buf.putInt(mbnType);
            buf.put(config.getBytes("ISO-8859-1"));
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SEL_CONFIG, payload);
            if (ar.exception == null) {
                return true;
            }
            Log.e(LOG_TAG, "QCRIL_EVT_HOOK_SEL_CONFIG failed w/ " + ar.exception);
            return false;
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "unsupport ISO-8859-1");
            return false;
        }
    }

    public String qcRilGetMetaInfoForConfig(String config) {
        return qcRilGetMetaInfoForConfig(config, DEFAULT_PHONE);
    }

    public String qcRilGetMetaInfoForConfig(String config, int mbnType) {
        String result = null;
        if (config.isEmpty() || config.length() > MAX_PDC_ID_LEN) {
            Log.e(LOG_TAG, "get meta info with incorrect config id: " + config);
        } else {
            try {
                byte[] payload = new byte[((this.mHeaderSize + INT_SIZE) + config.getBytes("ISO-8859-1").length)];
                ByteBuffer buf = createBufferWithNativeByteOrder(payload);
                addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_META_INFO, config.getBytes("ISO-8859-1").length + INT_SIZE);
                buf.putInt(mbnType);
                buf.put(config.getBytes("ISO-8859-1"));
                AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_META_INFO, payload);
                if (ar.exception != null) {
                    Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_META_INFO failed w/ " + ar.exception);
                    return null;
                } else if (ar.result == null) {
                    Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_META_INFO failed w/ null result");
                    return null;
                } else {
                    try {
                        String result2 = new String((byte[]) ar.result, "ISO-8859-1");
                        Log.v(LOG_TAG, "QCRIL_EVT_HOOK_GET_META_INFO returned w/ " + result2);
                        result = result2;
                    } catch (UnsupportedEncodingException e) {
                        Log.d(LOG_TAG, "unsupport ISO-8859-1");
                        return null;
                    }
                }
            } catch (UnsupportedEncodingException e2) {
                Log.d(LOG_TAG, "unsupport ISO-8859-1");
                return null;
            }
        }
        return result;
    }

    public boolean qcRilGoDormant(String interfaceName) {
        AsyncResult result = sendQcRilHookMsg((int) IQcRilHook.QCRILHOOK_GO_DORMANT, interfaceName);
        if (result.exception == null) {
            return true;
        }
        Log.w(LOG_TAG, "Go Dormant Command returned Exception: " + result.exception);
        return false;
    }

    public boolean qcRilSetCdmaSubSrcWithSpc(int cdmaSubscription, String spc) {
        Log.v(LOG_TAG, "qcRilSetCdmaSubSrcWithSpc: Set Cdma Subscription to " + cdmaSubscription);
        if (spc.isEmpty() || spc.length() > MAX_SPC_LEN) {
            Log.e(LOG_TAG, "QCRIL Set Cdma Subscription Source Command incorrect SPC: " + spc);
            return false;
        }
        byte[] payload = new byte[(spc.length() + BYTE_SIZE)];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        buf.put((byte) cdmaSubscription);
        buf.put(spc.getBytes());
        AsyncResult ar = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_CDMA_SUB_SRC_WITH_SPC, payload);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "QCRIL Set Cdma Subscription Source Command returned Exception: " + ar.exception);
            return false;
        } else if (ar.result == null) {
            return false;
        } else {
            byte succeed = ByteBuffer.wrap((byte[]) ar.result).get();
            Log.v(LOG_TAG, "QCRIL Set Cdma Subscription Source Command " + (succeed == (byte) 1 ? "Succeed." : "Failed."));
            if (succeed == (byte) 1) {
                return true;
            }
            return false;
        }
    }

    public byte[] qcRilSendProtocolBufferMessage(byte[] protocolBuffer, int phoneId) {
        Log.v(LOG_TAG, "qcRilSendProtoBufMessage: protocolBuffer" + protocolBuffer.toString());
        AsyncResult ar = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_PROTOBUF_MSG, protocolBuffer, phoneId);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "qcRilSendProtoBufMessage: Exception " + ar.exception);
            return null;
        } else if (ar.result != null) {
            return (byte[]) ar.result;
        } else {
            Log.e(LOG_TAG, "QCRIL_EVT_HOOK_PROTOBUF_MSG returned null");
            return null;
        }
    }

    public boolean qcRilSetTuneAway(boolean tuneAway) {
        Log.v(LOG_TAG, "qcRilSetTuneAway: tuneAway Value to be set to " + tuneAway);
        byte payload = (byte) 0;
        if (tuneAway) {
            payload = (byte) 1;
        }
        Log.v(LOG_TAG, "qcRilSetTuneAway: tuneAway payload " + payload);
        AsyncResult ar = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_TUNEAWAY, payload);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "qcRilSetTuneAway: Exception " + ar.exception);
        return false;
    }

    public boolean qcRilGetTuneAway() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_TUNEAWAY);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "qcRilGetTuneAway: Exception " + ar.exception);
            return false;
        } else if (ar.result != null) {
            byte tuneAwayValue = ByteBuffer.wrap((byte[]) ar.result).get();
            Log.v(LOG_TAG, "qcRilGetTuneAway: tuneAwayValue " + tuneAwayValue);
            if (tuneAwayValue == (byte) 1) {
                return true;
            }
            return false;
        } else {
            Log.e(LOG_TAG, "qcRilGetTuneAway: Null Response");
            return false;
        }
    }

    public boolean qcRilSetPrioritySubscription(int priorityIndex) {
        Log.v(LOG_TAG, "qcRilSetPrioritySubscription: PrioritySubscription to be set to" + priorityIndex);
        byte payload = (byte) priorityIndex;
        Log.v(LOG_TAG, "qcRilSetPrioritySubscription: PrioritySubscription payload " + payload);
        AsyncResult ar = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_PAGING_PRIORITY, payload);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "qcRilSetPrioritySubscription: Exception " + ar.exception);
        return false;
    }

    public int qcRilGetPrioritySubscription() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_PAGING_PRIORITY);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "qcRilGetPrioritySubscription: Exception " + ar.exception);
            return DEFAULT_PHONE;
        } else if (ar.result != null) {
            int subscriptionIndex = ByteBuffer.wrap((byte[]) ar.result).get();
            Log.v(LOG_TAG, "qcRilGetPrioritySubscription: subscriptionIndex " + subscriptionIndex);
            return subscriptionIndex;
        } else {
            Log.e(LOG_TAG, "qcRilGetPrioritySubscription: Null Response");
            return DEFAULT_PHONE;
        }
    }

    public boolean qcRilInformShutDown(int phoneId) {
        Log.d(LOG_TAG, "QCRIL Inform shutdown for phoneId " + phoneId);
        sendQcRilHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_INFORM_SHUTDOWN, null, new OemHookCallback(null) {
            public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
                Log.d(QcRilHook.LOG_TAG, "QCRIL Inform shutdown DONE!");
            }
        }, phoneId);
        return true;
    }

    public boolean qcRilCdmaAvoidCurNwk() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_CDMA_AVOID_CUR_NWK);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL Avoid the current cdma network Command returned Exception: " + ar.exception);
        return false;
    }

    public boolean qcRilSetFieldTestMode(int phoneId, byte ratType, int enable) {
        byte[] request = new byte[(this.mHeaderSize + 8)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_ENABLE_ENGINEER_MODE, DEFAULT_PHONE);
        reqBuffer.putInt(ratType);
        reqBuffer.putInt(enable);
        Log.d(LOG_TAG, "enable = " + enable + "ratType =" + ratType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_ENABLE_ENGINEER_MODE, request, phoneId);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL enable engineer mode cmd returned exception: " + ar.exception);
        return false;
    }

    public boolean qcRilCdmaClearAvoidanceList() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_CDMA_CLEAR_AVOIDANCE_LIST);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL Clear the cdma avoidance list Command returned Exception: " + ar.exception);
        return false;
    }

    public byte[] qcRilCdmaGetAvoidanceList() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_CDMA_GET_AVOIDANCE_LIST);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "QCRIL Get the cdma avoidance list Command returned Exception: " + ar.exception);
            return null;
        } else if (ar.result != null) {
            byte[] result = (byte[]) ar.result;
            if (result.length == AVOIDANCE_BUFF_LEN) {
                return result;
            }
            Log.e(LOG_TAG, "QCRIL Get unexpected cdma avoidance list buffer length: " + result.length);
            return null;
        } else {
            Log.e(LOG_TAG, "QCRIL Get cdma avoidance list command returned a null result.");
            return null;
        }
    }

    public boolean qcRilPerformIncrManualScan(int phoneId) {
        byte[] request = new byte[this.mHeaderSize];
        addQcRilHookHeader(createBufferWithNativeByteOrder(request), IQcRilHook.QCRIL_EVT_HOOK_PERFORM_INCREMENTAL_NW_SCAN, phoneId);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_PERFORM_INCREMENTAL_NW_SCAN, request, phoneId);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL perform incr manual scan returned exception " + ar.exception);
        return false;
    }

    public boolean qcrilSetBuiltInPLMNList(byte[] payload, int phoneId) {
        boolean retval = false;
        if (payload == null) {
            Log.e(LOG_TAG, "payload is null");
            return false;
        }
        byte[] request = new byte[(this.mHeaderSize + payload.length)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_BUILTIN_PLMN_LIST, payload.length);
        reqBuffer.put(payload);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_BUILTIN_PLMN_LIST, request, phoneId);
        if (ar.exception == null) {
            retval = true;
        } else {
            Log.e(LOG_TAG, "QCRIL set builtin PLMN list returned exception: " + ar.exception);
        }
        return retval;
    }

    public boolean qcRilSetPreferredNetworkAcqOrder(int acqOrder, int phoneId) {
        byte[] request = new byte[(this.mHeaderSize + INT_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        Log.d(LOG_TAG, "acq order: " + acqOrder);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_ACQ_ORDER, INT_SIZE);
        reqBuffer.putInt(acqOrder);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_ACQ_ORDER, request, phoneId);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL set acq order cmd returned exception: " + ar.exception);
        return false;
    }

    public byte qcRilGetPreferredNetworkAcqOrder(int phoneId) {
        byte[] request = new byte[this.mHeaderSize];
        addQcRilHookHeader(createBufferWithNativeByteOrder(request), IQcRilHook.QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_ACQ_ORDER, INT_SIZE);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_ACQ_ORDER, request, phoneId);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "QCRIL set acq order cmd returned exception: " + ar.exception);
            return (byte) 0;
        } else if (ar.result != null) {
            byte acq_order = ByteBuffer.wrap((byte[]) ar.result).get();
            Log.v(LOG_TAG, "acq order is " + acq_order);
            return acq_order;
        } else {
            Log.e(LOG_TAG, "no acq order result return");
            return (byte) 0;
        }
    }

    public boolean qcRilSetLteTuneaway(int enable, int phoneId) {
        byte[] request = new byte[(this.mHeaderSize + INT_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        Log.d(LOG_TAG, "qcRilSetLteTuneaway enable :" + enable);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_LTE_TUNE_AWAY, INT_SIZE);
        reqBuffer.putInt(enable);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_LTE_TUNE_AWAY, request, phoneId);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL set lte tune away returned exception: " + ar.exception);
        return false;
    }

    public void qcRilSendDataEnableStatus(int enable, int phoneId) {
        OemHookCallback oemHookCb = new OemHookCallback(null) {
            public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
                Log.d(QcRilHook.LOG_TAG, "QCRIL send data enable status DONE!");
            }
        };
        byte[] request = new byte[(this.mHeaderSize + INT_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_IS_DATA_ENABLED, INT_SIZE);
        reqBuffer.putInt(enable);
        sendRilOemHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_SET_IS_DATA_ENABLED, request, oemHookCb, phoneId);
    }

    public void qcRilSendDataRoamingEnableStatus(int enable, int phoneId) {
        OemHookCallback oemHookCb = new OemHookCallback(null) {
            public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
                Log.d(QcRilHook.LOG_TAG, "QCRIL send data roaming enable status DONE!");
            }
        };
        byte[] request = new byte[(this.mHeaderSize + INT_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_IS_DATA_ROAMING_ENABLED, INT_SIZE);
        reqBuffer.putInt(enable);
        sendRilOemHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_SET_IS_DATA_ROAMING_ENABLED, request, oemHookCb, phoneId);
    }

    public void qcRilSendApnInfo(String type, String apn, int isValid, int phoneId) {
        OemHookCallback oemHookCb = new OemHookCallback(null) {
            public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
                Log.d(QcRilHook.LOG_TAG, "QCRIL send apn info DONE!");
            }
        };
        int payloadSize = ((type.length() + 12) + apn.length()) + 2;
        if (payloadSize > MAX_REQUEST_BUFFER_SIZE) {
            Log.e(LOG_TAG, "APN sent is larger than maximum buffer. Bail out");
            return;
        }
        byte[] request = new byte[(this.mHeaderSize + payloadSize)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_APN_INFO, payloadSize);
        reqBuffer.putInt(type.length() + BYTE_SIZE);
        reqBuffer.put(type.getBytes());
        reqBuffer.put((byte) 0);
        reqBuffer.putInt(apn.length() + BYTE_SIZE);
        reqBuffer.put(apn.getBytes());
        reqBuffer.put((byte) 0);
        reqBuffer.putInt(isValid);
        sendRilOemHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_SET_APN_INFO, request, oemHookCb, phoneId);
    }

    public boolean qcRilSendDDSInfo(int dds, int phoneId) {
        byte[] request = new byte[(this.mHeaderSize + INT_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        Log.d(LOG_TAG, "dds phoneId: " + dds);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_DATA_SUBSCRIPTION, INT_SIZE);
        reqBuffer.putInt(dds);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_DATA_SUBSCRIPTION, request, phoneId);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL send dds sub info returned exception: " + ar.exception);
        return false;
    }

    public boolean qcRilSetPreferredNetworkBandPref(int bandPref, int phoneId) {
        byte[] request = new byte[(this.mHeaderSize + INT_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        Log.d(LOG_TAG, "band pref: " + bandPref);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_BAND_PREF, INT_SIZE);
        reqBuffer.putInt(bandPref);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_BAND_PREF, request, phoneId);
        if (ar.exception == null) {
            return true;
        }
        Log.e(LOG_TAG, "QCRIL set band pref cmd returned exception: " + ar.exception);
        return false;
    }

    public byte qcRilGetPreferredNetworkBandPref(int bandType, int phoneId) {
        byte[] request = new byte[this.mHeaderSize];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_BAND_PREF, INT_SIZE);
        reqBuffer.putInt(bandType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_BAND_PREF, request, phoneId);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "QCRIL get band perf cmd returned exception: " + ar.exception);
            return (byte) 0;
        } else if (ar.result != null) {
            byte band_pref = ByteBuffer.wrap((byte[]) ar.result).get();
            Log.v(LOG_TAG, "band pref is " + band_pref);
            return band_pref;
        } else {
            Log.e(LOG_TAG, "no band pref result return");
            return (byte) 0;
        }
    }

    public AsyncResult sendQcRilHookMsg(int requestId) {
        byte[] request = new byte[this.mHeaderSize];
        addQcRilHookHeader(createBufferWithNativeByteOrder(request), requestId, DEFAULT_PHONE);
        return sendRilOemHookMsg(requestId, request);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, byte payload) {
        return sendQcRilHookMsg(requestId, payload, (int) DEFAULT_PHONE);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, byte payload, int phoneId) {
        byte[] request = new byte[(this.mHeaderSize + BYTE_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, BYTE_SIZE);
        reqBuffer.put(payload);
        return sendRilOemHookMsg(requestId, request, phoneId);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, byte[] payload) {
        return sendQcRilHookMsg(requestId, payload, (int) DEFAULT_PHONE);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, byte[] payload, int phoneId) {
        byte[] request = new byte[(this.mHeaderSize + payload.length)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, payload.length);
        reqBuffer.put(payload);
        return sendRilOemHookMsg(requestId, request, phoneId);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, int payload) {
        byte[] request = new byte[(this.mHeaderSize + INT_SIZE)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, INT_SIZE);
        reqBuffer.putInt(payload);
        return sendRilOemHookMsg(requestId, request);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, String payload) {
        byte[] request = new byte[(this.mHeaderSize + payload.length())];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, payload.length());
        reqBuffer.put(payload.getBytes());
        return sendRilOemHookMsg(requestId, request);
    }

    public void sendQcRilHookMsgAsync(int requestId, byte[] payload, OemHookCallback oemHookCb) {
        sendQcRilHookMsgAsync(requestId, payload, oemHookCb, DEFAULT_PHONE);
    }

    public void sendQcRilHookMsgAsync(int requestId, byte[] payload, OemHookCallback oemHookCb, int phoneId) {
        int payloadLength = DEFAULT_PHONE;
        if (payload != null) {
            payloadLength = payload.length;
        }
        byte[] request = new byte[(this.mHeaderSize + payloadLength)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, payloadLength);
        if (payload != null) {
            reqBuffer.put(payload);
        }
        sendRilOemHookMsgAsync(requestId, request, oemHookCb, phoneId);
    }

    public static void register(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (mRegistrants) {
            mRegistrants.add(r);
        }
    }

    public static void unregister(Handler h) {
        synchronized (mRegistrants) {
            mRegistrants.remove(h);
        }
    }

    public void registerForFieldTestData(Handler h, int what, Object obj) {
    }

    public void unregisterForFieldTestData(Handler h) {
    }

    public void registerForExtendedDbmIntl(Handler h, int what, Object obj) {
    }

    public void unregisterForExtendedDbmIntl(Handler h) {
    }

    protected void finalize() {
        Log.v(LOG_TAG, "is destroyed");
    }

    public static void notifyRegistrants(AsyncResult ar) {
        if (mRegistrants != null) {
            mRegistrants.notifyRegistrants(ar);
        } else {
            Log.e(LOG_TAG, "QcRilOemHook notifyRegistrants Failed");
        }
    }
}
