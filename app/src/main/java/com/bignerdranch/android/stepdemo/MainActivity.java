package com.bignerdranch.android.stepdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.bignerdranch.android.stepdemo.inter.UpdateUiCallBack;
import com.bignerdranch.android.stepdemo.utils.SharedPreferencesUtils;

import static com.bignerdranch.android.stepdemo.StepService.CURRENT_DATE;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text_view);
        mTextView.setText(SharedPreferencesUtils.readInteger(CURRENT_DATE)+"");

        initService();
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService stepService = ((StepService.StepBinder) service).getService();

            stepService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount) {
                    mTextView.setText(stepCount+" ");
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private boolean isBind = false;

    private void initService() {
        Intent intent = new Intent(this,StepService.class);
        startService(intent);
        isBind = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBind) {
            this.unbindService(serviceConnection);
        }
    }
}
