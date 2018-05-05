package com.bignerdranch.android.stepdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.bignerdranch.android.stepdemo.base.StepCount;
import com.bignerdranch.android.stepdemo.base.StepValuePassListener;
import com.bignerdranch.android.stepdemo.inter.UpdateUiCallBack;
import com.bignerdranch.android.stepdemo.utils.SharedPreferencesUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zdf on 18-4-22.
 */

public class StepService extends Service implements SensorEventListener{

    private int stepNum;

    private int stepSensorType = -1;

    private SensorManager sensorManager;

    private StepBinder stepBinder = new StepBinder();

    private StepCount mStepCount;

    private boolean hasRecord = false;//有记录吗

    private int hasStepCount;//第一次统计打开ａｐｐ时的步数

    private int previousStepCount = 0;//上次打开ａｐｐ时的步数

    public static String CURRENT_DATE = "";//日期

    private BroadcastReceiver mReceiver;

    private NotificationCompat.Builder mBuilder;

    private NotificationManager mNotificationManager;

    int notifyId_Step = 100;


    @Override
    public void onCreate() {
        super.onCreate();
        initNotification();
        initTodayData();
        initBroadcastReceiver();
        new Thread(new Runnable() {
            public void run() {
                startStepDetector();
            }
        }).start();

    }

    private void initNotification() {
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("今日步数" + stepNum + " 步")
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setSmallIcon(R.drawable.step);
        Notification notification = mBuilder.build();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(notifyId_Step, notification);

    }

    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_SHUTDOWN);

        mReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
                    Intent i = new Intent(getApplicationContext(),StepService.class);
                    startService(i);
                }else if(Intent.ACTION_SHUTDOWN.equals(action)){

                }else if(Intent.ACTION_DATE_CHANGED.equals(action)){

                }
            }
        };
    }

    private void initTodayData() {
        CURRENT_DATE = getTodayDate();
        stepNum = SharedPreferencesUtils.readInteger(CURRENT_DATE);
        updateNum();
    }

    private void startStepDetector() {
        if (sensorManager != null) {
            sensorManager = null;
        }
        // 获取传感器管理器的实例
        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        //android4.4以后可以使用计步传感器
        int VERSION_CODES = Build.VERSION.SDK_INT;
        if (VERSION_CODES >= 19) {
            addCountStepListener();
        } else {
            addBasePedometerListener();
        }
    }

    private void addCountStepListener() {
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_COUNTER;
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (detectorSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_DETECTOR;
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            addBasePedometerListener();
        }
    }


    private void addBasePedometerListener() {
        mStepCount = new StepCount();
        mStepCount.setSteps(stepNum);

        Sensor sensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean isAvailable = sensorManager.registerListener(mStepCount.getStepDetector(), sensor,
                SensorManager.SENSOR_DELAY_UI);

        mStepCount.initListener(new StepValuePassListener() {
            @Override
            public void stepChanged(int steps) {
                stepNum = steps;
                updateNum();
            }
        });

        if (!isAvailable) {
            //Toast.makeText(StepService.this,"运动快乐！",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (stepSensorType == Sensor.TYPE_STEP_COUNTER) {
            //获取当前传感器返回的临时步数
            int tempStep = (int) event.values[0];

            //首次如果没有获取手机系统中已有的步数则获取一次系统中APP还未开始记步的步数
            if (!hasRecord) {
                hasRecord = true;
                hasStepCount = tempStep;
            } else {
                //获取APP打开到现在的总步数=本次系统回调的总步数-APP打开之前已有的步数
                int thisStepCount = tempStep - hasStepCount;
                //本次有效步数=（APP打开后所记录的总步数-上一次APP打开后所记录的总步数）
                int thisStep = thisStepCount - previousStepCount;
                //总步数=现有的步数+本次有效步数
                stepNum += (thisStep);
                //记录最后一次APP打开到现在的总步数
                previousStepCount = thisStepCount;

            }

        } else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                stepNum++;
            }
        }

        updateNum();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //binder~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    public IBinder onBind(Intent intent) {
        updateNum();
        return stepBinder;
    }

    public class StepBinder extends Binder {

        public StepService getService() {
            return StepService.this;
        }
    }

    //callback~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private UpdateUiCallBack mCallback;

    public void registerCallback(UpdateUiCallBack paramICallback) {
        this.mCallback = paramICallback;
    }

    private void updateNum() {
        SharedPreferencesUtils.readInteger(CURRENT_DATE);

        Intent hangIntent = new Intent(this, MainActivity.class);
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = mBuilder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("今日步数" + stepNum + " 步")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(hangPendingIntent)
                .build();
        mNotificationManager.notify(notifyId_Step, notification);

        if (mCallback != null) {
            mCallback.updateUi(stepNum);
            SharedPreferencesUtils.storeInteger(CURRENT_DATE,stepNum);
        }
    }

    //Date判断~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
