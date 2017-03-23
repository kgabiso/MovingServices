package com.example.codetribe.movingservices;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.github.arturogutierrez.Badges;
import com.github.arturogutierrez.BadgesNotSupportedException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Codetribe on 3/22/2017.
 */
public class BadgeIntentService extends IntentService {

    private FirebaseAuth mAuth;
    private DatabaseReference mDataRequest;
    public BadgeIntentService() {
        super("BadgeIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int Noti_id = 123;
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.delivery_truck_icon)
                .setContentTitle("Moving Services")
                .setContentText("Running")
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        NotificationManager notificationManager =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Noti_id,notification);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDataRequest = FirebaseDatabase.getInstance().getReference().child("Request");
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mDataRequest.orderByChild("owner_id").equalTo(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int count = 0;

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (dataSnapshot.child(child.getKey()).child("status").getValue() != null) {
                            if (dataSnapshot.child(child.getKey()).child("status").getValue().toString().equals("Pending")) {

                                count = count + 1;

                            }
                        }
                    }

                    if (count > 0) {

                        try {
                            Badges.setBadge(getApplicationContext(), count);
                        } catch (BadgesNotSupportedException badgesNotSupportedException) {
                            //Log.d(TAG, badgesNotSupportedException.getMessage());
                        }
                    } else {
                        count = 0;
                        try {
                            Badges.setBadge(getApplicationContext(), count);
                        } catch (BadgesNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}

