package com.example.codetribe.movingservices;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Codetribe on 2/22/2017.
 */
public class SharedPref {

    private static final String SHARED_PREF= "fcmShare";
    private static final String KEY_TOKEN= "token";

    private static Context con;
    private static SharedPref mInstance;

    private SharedPref(Context context){
        con = context;
    }
    public static synchronized SharedPref geInstance(Context context){

        if(mInstance == null)
        {
            mInstance =  new SharedPref(context);
        }
        return mInstance;
    }
public boolean storeToken(String token)
{
    SharedPreferences sharedPreferences = con.getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(KEY_TOKEN,token);
    editor.apply();
    return true;
}
    public String getToken(){
        SharedPreferences sharedPreferences = con.getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN,null);

    }

}
