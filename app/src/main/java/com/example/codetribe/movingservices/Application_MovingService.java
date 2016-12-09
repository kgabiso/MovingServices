package com.example.codetribe.movingservices;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Codetribe on 11/16/2016.
 */
public class Application_MovingService extends Application {

    private static Application_MovingService mInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        if(!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

//        Picasso.Builder builder = new Picasso.Builder(this);
//        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
//        Picasso built = builder.build();
//        built.setIndicatorsEnabled(false);
//        built.setLoggingEnabled(true);
//        Picasso.setSingletonInstance(built);
    }
    public static synchronized Application_MovingService getInstance() {
        return mInstance;
    }
    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
