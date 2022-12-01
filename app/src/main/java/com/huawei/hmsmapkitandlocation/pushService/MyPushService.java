package com.huawei.hmsmapkitandlocation.pushService;

import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

public class MyPushService extends HmsMessageService {
    private static final String TAG = "MyPushService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i(TAG, "RECEIVED TOKEN "+ token);
    }

    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        Log.i(TAG, message.getMessageId());
    }
}
