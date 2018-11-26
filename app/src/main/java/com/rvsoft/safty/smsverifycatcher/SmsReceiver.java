package com.rvsoft.safty.smsverifycatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;


/**
 * Created by Ravi on 11/26/2018.
 * Algante
 * ravikant.vishwakarma@algante.com
 */
public class SmsReceiver extends BroadcastReceiver {
    private OnSmsCatchListener callback;
    private String phoneNumberFilter;
    private String parseExpresion;

    SmsReceiver(){

    }

    public void setCallback(OnSmsCatchListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        try {
            if (bundle!=null){
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i=0;i<pdusObj.length;i++){
                    SmsMessage currentMessage = this.getIncomingMessage(pdusObj[i],bundle);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    if (this.phoneNumberFilter !=null && !phoneNumber.toLowerCase().contains(this.phoneNumberFilter.toLowerCase())){
                        return;
                    }

                    String message = currentMessage.getDisplayMessageBody();
                    if (this.parseExpresion!=null && !TextUtils.isEmpty(this.parseExpresion)){
                        message = message.replaceAll(parseExpresion,"");
                    }

                    if (callback!=null){
                        this.callback.onSmsCatch(message);
                    }
                }
            }
        }catch (Exception e){
            Log.e("SmSReceiver",""+e);
        }
    }

    private SmsMessage getIncomingMessage(Object object, Bundle bundle){
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >=23){
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) object, format);
        }else {
            currentSMS = SmsMessage.createFromPdu((byte[]) object);
        }
        return currentSMS;
    }

    public void setPhoneNumberFilter(String senderId){
        this.phoneNumberFilter = senderId;
    }

    public void setParseExpresion(String regex){
        this.parseExpresion = regex;
    }
}
