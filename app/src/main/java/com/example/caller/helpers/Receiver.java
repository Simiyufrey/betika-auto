package com.example.caller.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class Receiver  extends BroadcastReceiver {

    final static  String TELEPHONY="android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(TELEPHONY)){
            Bundle bundle=intent.getExtras();
            try
            {
                if(bundle !=null){
                    Object[] pdus=(Object[]) bundle.get("pdus");

                    if(pdus !=null){
                        for(Object pdu : pdus){
                            SmsMessage smsMessage=SmsMessage.createFromPdu((byte []) pdu);
                            String sender=smsMessage.getDisplayOriginatingAddress().toString();
                            String sms=smsMessage.getMessageBody().toString();
                            handleMessage(sms,sender,context);
                        }
                    }

                }
            }
            catch (Exception e){

                Toast.makeText(context, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void handleMessage(String sms,String smsFrom,Context ctx){

        Toast.makeText(ctx, ""+sms, Toast.LENGTH_SHORT).show();
    }
}
