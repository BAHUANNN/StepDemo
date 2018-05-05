package com.bignerdranch.android.stepdemo;

import android.app.Application;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kolibreath on 17-7-23.
 */

public class App extends Application {

    public static Context sContext;
    public static Map<Integer,String > sUserId2Name = new HashMap<>();
    private static Object[] stemp =  sUserId2Name.keySet().toArray();
    public static int[] sAll = new int[stemp.length];
    private void initAllArray(){
        int index = 0;
        for(int i=0;i<stemp.length;i++){
            sAll[index++] = (int) stemp[i];
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        initAllArray();
    }

    public static Context getContext(){
        return sContext;
    }
}
