package ru.relastic.meet003;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    private final static int MODE = Service.START_NOT_STICKY;
    public final static int MSG_REGISTER_CLIENT = 1;
    public final static int MSG_UNREGISTER_CLIENT = 2;
    public final static int MSG_SERVICE_DESTROY = 3;
    public final static int MSG_SERVICE_RESPONSE = 4;
    public final static String MSG_SERVICE_VALUE = "value";
    public final static String MSG_SOURCE_BINDER = "source";
    private final Messenger localMessenger;
    private List<Messenger> mClients = new ArrayList<>();
    private boolean exitValue=false;

    public MyService() {
        localMessenger = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //Log.v("handleMessageService","msg");
                switch (msg.what) {
                    case MSG_REGISTER_CLIENT:
                        mClients.add(msg.replyTo);
                        break;
                    case MSG_UNREGISTER_CLIENT:
                        mClients.remove(msg.replyTo);
                        break;
                    case MSG_SERVICE_DESTROY:
                        stopService();
                        break;
                }
            }
        });
        startWork();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getBundleExtra(MSG_SOURCE_BINDER);
        if (bundle!=null) {
            IBinder iBinder =bundle.getBinder(MSG_SOURCE_BINDER);
            Messenger messenger = new Messenger(iBinder);
            Message msg = Message.obtain(null,MSG_REGISTER_CLIENT);
            msg.replyTo = messenger;
            try {
                localMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return MODE;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return localMessenger.getBinder();
    }
    @Override
    public void onDestroy() {
        Log.v("LOG:", "Service destroyed");
        exitValue=true;
    }
    private final boolean isContinue(){
        return exitValue;
    }
    private void startWork(){
        final int timeout = 60;
        final int interval = 1000; //msec
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count=0;
                while(!isContinue() && count<=timeout) {
                    long t = (long)count;
                    Bundle bundle = new Bundle();
                    bundle.putLong(MSG_SERVICE_VALUE,t);
                    notifyClients(bundle);
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                stopService();
            }
        }).start();
    }
    private void stopService() {
        mClients.clear();
        stopSelf();
    }
    private void notifyClients(Bundle bundle){
        for (Messenger mService : mClients) {
            Message message = Message.obtain(null,MSG_SERVICE_RESPONSE);
            message.setData(bundle);
            //Log.v("Response from service:",msg.toString());
            message.replyTo = mService;
            try {
                mService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    public static Intent newIntent(Context context, IBinder iBinder) {
        Intent intent = new Intent(context,MyService.class);
        if(iBinder != null) {
            Bundle bundle = new Bundle();
            bundle.putBinder(MyService.MSG_SOURCE_BINDER,iBinder);
            intent.putExtra(MyService.MSG_SOURCE_BINDER,bundle);
        }
        return intent;
    }
}
