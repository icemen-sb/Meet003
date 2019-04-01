package ru.relastic.meet003;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private Button mStart;
    private Button mStop;
    private Button mNext;
    private final Messenger localMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case MyService.MSG_SERVICE_RESPONSE:
                    long val = msg.getData().getLong(MyService.MSG_SERVICE_VALUE);
                    viewValue(val);
                    break;
                default:
                    break;
            }
        }
    });
    private void viewValue(long val){
        mTextView.setText(String.valueOf(val));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initViews();
        initListeners();
    }
    private void initViews() {
        mTextView = findViewById(R.id.textView);
        mStart = findViewById(R.id.btnStart);
        mStop = findViewById(R.id.btnStop);
        mNext = findViewById(R.id.btnNext);
    }
    private void initListeners() {
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSRV();
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSRV();
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewNext();
            }
        });
    }
    private void startSRV(){
        startService(MyService.newIntent(this, localMessenger.getBinder()));
    }
    private void stopSRV(){
        stopService(MyService.newIntent(this,null));
    }
    private void viewNext(){
        Intent intent = NextActivity.newIntent(this);
        startActivity(intent);
    }

}
