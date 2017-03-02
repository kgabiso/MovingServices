package com.example.codetribe.movingservices;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity implements OnMapReadyCallback  {

    private FirebaseAuth mAuth;
    private DatabaseReference mData_request;
    private RecyclerView recyclerView;
    private double long_from,long_to,lat_from,lat_to;
    TextView message;
    private boolean mReject_process = true,mAccept_process = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mAuth = FirebaseAuth.getInstance();
        mData_request = FirebaseDatabase.getInstance().getReference().child("Request");
        mData_request.keepSynced(true);
        recyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        message = (TextView) findViewById(R.id.mes_info);
        message.setVisibility(View.VISIBLE);
        mData_request.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Message,Request_ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message, Request_ViewHolder>(
                Message.class,
                R.layout.left_messageviewer,
                Request_ViewHolder.class, mData_request.orderByChild("owner_id").equalTo(mAuth.getCurrentUser().getUid())
        ) {
            @Override
            protected void populateViewHolder(final Request_ViewHolder viewHolder, final Message model, int position) {


                final String post_key = getRef(position).getKey();

                viewHolder.setDesc_req(model.getDesc_req());
                viewHolder.setLocation_from(model.getLocation_from());
                viewHolder.setLocation_to(model.getLocation_to());
                viewHolder.setMove_date(model.getMove_date(),model.getMove_time());
                viewHolder.setStatus(model.getStatus(),getApplicationContext());
                viewHolder.setUser_id(getApplicationContext(),model.getUser_id());
                viewHolder.setPost_id(model.getPost_id());

                mData_request.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child(post_key).child("owner_id").getValue().toString().equals(mAuth.getCurrentUser().getUid()))
                        {
                            message.setVisibility(View.GONE);
                        }else {
                            message.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.mReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(),post_key,Toast.LENGTH_LONG).show();
                        mData_request.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(mReject_process) {
                                    mData_request.child(post_key).child("status").setValue("Reject");
                                    mReject_process = false;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                viewHolder.mAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(),post_key,Toast.LENGTH_LONG).show();
                        mData_request.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(mAccept_process) {
                                    mData_request.child(post_key).child("status").setValue("Accept");
                                    mAccept_process = false;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                viewHolder.mes_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mData_request.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                long_from = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_from").child("latitude").getValue().toString());
                                lat_from =  Double.parseDouble(dataSnapshot.child(post_key).child("latlng_from").child("longitude").getValue().toString());

                                long_to = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_to").child("latitude").getValue().toString());
                                lat_to = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_to").child("longitude").getValue().toString());

                                Intent i = new Intent(MessageActivity.this,MapActivity.class);
                                i.putExtra("latitude_from",lat_from);
                                i.putExtra("longitude_from",long_from);
                                i.putExtra("post_key",post_key);
                                i.putExtra("latitude_to",lat_to);
                                i.putExtra("longitude_to",long_to);
                                startActivity(i);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public static class Request_ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        private TextView mUserName,mLoc_to,mLoc_from,mDate,mStatus,mMessage;
        private ImageButton mAccept,mReject;
        private String uid,postID;
        private CircleImageView messengerImageView;
        private DatabaseReference mDataUser;
        private CardView mes_card;

        public Request_ViewHolder(View itemView) {
            super(itemView);

            mUserName =(TextView)itemView.findViewById(R.id.mes_username);
            mLoc_to =(TextView)itemView.findViewById(R.id.mes_location_to);
            mLoc_from =(TextView)itemView.findViewById(R.id.mes_location_from);
            mDate =(TextView)itemView.findViewById(R.id.mes_date);
            mStatus =(TextView)itemView.findViewById(R.id.mes_status);
            mAccept =(ImageButton)itemView.findViewById(R.id.mes_accept);
            mReject =(ImageButton)itemView.findViewById(R.id.mes_reject);
            mMessage =(TextView)itemView.findViewById(R.id.mes_message);
            messengerImageView =(CircleImageView)itemView.findViewById(R.id.messengerImageView);
            mes_card =(CardView)itemView.findViewById(R.id.mes_card);
            mDataUser = FirebaseDatabase.getInstance().getReference().child("users");
            mDataUser.keepSynced(true);

        }

        public void setDesc_req(String desc_req) {
           if(TextUtils.isEmpty(desc_req))
           {
               mMessage.setVisibility(View.GONE);
           }else {
               mMessage.setText(desc_req);

           }
        }
        public void setLocation_from(String location_from) {
            mLoc_from.setText(location_from);
        }
        public void setLocation_to(String location_to) {
            mLoc_to.setText(location_to);
        }
        public void setMove_date(String move_date,String move_time) {
            mDate.setText(move_date +" "+move_time);
        }

        public void setUser_id(final Context context, String user_id) {

            uid = user_id;

            mDataUser.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String username,lname;
                    Glide.with(context).load(dataSnapshot.child("profileimage").getValue().toString()).centerCrop().into(messengerImageView);
                    if(dataSnapshot.child("name").getValue() != null)
                    {
                        username = dataSnapshot.child("name").getValue().toString();
                    }else {
                        username ="";
                    }
                    if(dataSnapshot.child("lname").getValue()!= null)
                    {
                        lname = dataSnapshot.child("lname").getValue().toString();

                    }else {
                        lname ="";
                    }
                    mUserName.setText(username +" "+ lname);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setPost_id(String post_id) {
           postID = post_id;
        }
        public void setStatus(String status,Context context) {
            if(TextUtils.isEmpty(status))
            {
                mStatus.setText("Status :Pending");
            }else {
                mStatus.setText("Status :"+status);
                if(status == "Reject"){
                    mStatus.setTextColor(context.getResources().getColor(R.color.reject));

                }else if(status == "Accept"){
                    mStatus.setTextColor(context.getResources().getColor(R.color.accept));
                }

            }

        }
public void setTextColor(Context context){
    
}
        @Override
        public void onMapReady(GoogleMap googleMap) {

        }
    }
}
