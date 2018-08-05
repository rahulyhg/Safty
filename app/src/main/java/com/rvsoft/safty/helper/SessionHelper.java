package com.rvsoft.safty.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionHelper {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private int PRIVATE_MODE = 0;
    private String PREF_NAME = "safty";

    public SessionHelper(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }
    public void putPermissionStatus(String permission, boolean status){
        editor.putBoolean(permission,status);
        editor.apply();
    }
    public boolean getPermissionStatus(String permission){
        return pref.getBoolean(permission,false);
    }
}
