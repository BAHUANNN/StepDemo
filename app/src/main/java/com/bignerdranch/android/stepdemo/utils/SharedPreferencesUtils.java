package com.bignerdranch.android.stepdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bignerdranch.android.stepdemo.App;

/**
 * Created by yueuy on 18-2-1.
 */

public class SharedPreferencesUtils {

    public synchronized static void storeString(String key, String string){
        SharedPreferences sharedPreferences = App.getContext().getSharedPreferences(key,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,string);
        editor.apply();
    }

    //if the string exits if true read from memory; exit read string
    public synchronized static String readString(String key){
        SharedPreferences sharedPreferences = App.getContext().getSharedPreferences(key,
                Context.MODE_PRIVATE);
        String string = sharedPreferences.getString(key,"NOTHING");
        return string;
    }

    public synchronized static void storeInteger(String key,int value){
        SharedPreferences sharedPreferences = App.getContext().getSharedPreferences(key,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key,value);
        editor.apply();
    }

    public synchronized static Integer readInteger(String key){
        SharedPreferences sharedPreferences = App.getContext().getSharedPreferences(key,
                Context.MODE_PRIVATE);
        int number = sharedPreferences.getInt(key,0);
        return number;
    }
}