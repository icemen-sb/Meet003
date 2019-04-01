package ru.relastic.meet003;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ru.relastic.meet003.R;

public class NextActivity extends AppCompatActivity {
    private TextView mTextView;
    private Button mBind;
    private Button mUnbind;
    private Messenger mService;
    private final Messenger localMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            if (mService!=null) {
                switch (msg.what) {
                    case MyService.MSG_SERVICE_RESPONSE:
                        long val = msg.getData().getLong(MyService.MSG_SERVICE_VALUE);
                        viewValue(val);
                        break;
                    default:
                        break;
                }
            }
        }
    });
    private void viewValue(long val){
        mTextView.setText(String.valueOf(val));
    }
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            mService = new Messenger(service);
            Message msg = Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
            msg.replyTo = localMessenger;
            try{
                mService.send(msg);
                Log.v("NextActivity LOG:", "connected");
            }catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.v("NextActivity LOG:", "disconnected");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next);
        initViews();
        initListeners();
    }
    private void initViews(){
        mTextView = findViewById(R.id.textView);
        mBind = findViewById(R.id.btnBind);
        mUnbind = findViewById(R.id.btnUnbind);
    }
    private void initListeners(){
        mBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindSRV();
            }
        });
        mUnbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindSRV();
            }
        });
    }
    public static Intent newIntent(Context context) {
        return new Intent(context, NextActivity.class);
    }
    private void bindSRV(){
        bindService(MyService.newIntent(this,null), mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    private void unbindSRV(){
        Message msg = Message.obtain(null, MyService.MSG_UNREGISTER_CLIENT);
        msg.replyTo = localMessenger;
        try {
            mService.send(msg);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(mServiceConnection);
    }
}
