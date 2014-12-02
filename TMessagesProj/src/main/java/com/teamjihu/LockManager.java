package com.teamjihu;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.ui.ApplicationLoader;

/**
 * Created by OOO on 2014-11-29.
 */
public class LockManager {

    public static boolean isLocked() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        return preferences.getBoolean("isLocked", false);
    }

    public static void OnOffToggle() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLocked", !isLocked());
        editor.commit();
    }

    public static void On() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLocked", true);
        editor.commit();
    }

    public static void Off() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLocked", false);
        editor.commit();
    }

    public static boolean isOpened() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        return preferences.getBoolean("isOpened", false);
    }

    public static void Lock() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isOpened", false);
        editor.commit();
    }

    public static void Unlock() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isOpened", true);
        editor.commit();
    }

    private static String getPassword() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        return preferences.getString("password", "0000");
    }

    public static boolean isCorrectPassword( String password ) {
        return ( password.equals( getPassword() )) ? true : false;
    }

    public static void ChangePassword( String password ) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Locks", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("password", password);
        editor.commit();
    }

    public static void AllPopupOff() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("popupAll", 0);
        editor.putInt("popupGroup", 0);
        editor.commit();
    }

}
