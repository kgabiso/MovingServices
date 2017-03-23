package com.example.codetribe.movingservices;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class MessageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private DatabaseReference mData_request, mData_user;
    private RecyclerView recyclerView;
    private double long_from, long_to, lat_from, lat_to;
    TextView message,txtcount;
    private boolean mReject_process = true, mAccept_process = true;
    LovelyStandardDialog lovelyStandardDialog;
    String emailBody;
    private String contextID;
    private SpotsDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        lovelyStandardDialog = new LovelyStandardDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mData_request = FirebaseDatabase.getInstance().getReference().child("Request");
        mData_user = FirebaseDatabase.getInstance().getReference().child("users");
        mData_request.keepSynced(true);
        recyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        message = (TextView) findViewById(R.id.mes_info);
        message.setVisibility(View.VISIBLE);
        mData_request.keepSynced(true);
        txtcount = (TextView)findViewById(R.id.mes_count);
        dialog = new SpotsDialog(this);
        countRequest();
    }
    public void countRequest(){
        mData_request.orderByChild("owner_id").equalTo(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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
                    txtcount.setText(""+count);
                }else {

                    txtcount.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu, menu);

    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_Delete){
            dialog.show();
            mData_request.child(contextID).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError == null)
                    {
                        dialog.setMessage("Deleting...");
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Deleted successfully",Toast.LENGTH_LONG).show();
                    }else {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();

                    }


                }
            });

        }

        else if(item.getItemId() == R.id.action_Open)
        {
            dialog.show();
            mData_request.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(contextID).hasChild("post_id")) {
                        String value = dataSnapshot.child(contextID).child("post_id").getValue().toString();
                        Intent i = new Intent(MessageActivity.this, SingleActivity.class);
                        i.putExtra("Single_Post_id", value);
                        startActivity(i);
                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"page can not be fund",Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        return super.onContextItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Message, Request_ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message, Request_ViewHolder>(
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
                viewHolder.setStatus(model.getStatus());
                viewHolder.setUser_id(getApplicationContext(), model.getUser_id());
                viewHolder.setPost_id(model.getPost_id());
                viewHolder.status_color(post_key, getApplicationContext());
                viewHolder.setMove_date(model.getMove_date(), model.getMove_time(),getApplicationContext(),post_key);
                viewHolder.mes_card.setClickable(false);


                mData_request.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(post_key).child("owner_id").getValue() != null) {
                            if (dataSnapshot.child(post_key).child("owner_id").getValue().toString().equals(mAuth.getCurrentUser().getUid())) {
                                message.setVisibility(View.GONE);
                            } else {
                                message.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.mReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(),"Rejected", Toast.LENGTH_LONG).show();
                        mData_request.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mReject_process) {

                                    mData_request.child(post_key).child("status").setValue("Rejected");
                                    mReject_process = false;
                                    viewHolder.mAccept.setClickable(false);
                                    viewHolder.mReject.setClickable(false);
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

                        Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_LONG).show();

                        mData_request.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mAccept_process) {

                                    mData_request.child(post_key).child("status").setValue("Accepted");
                                     final String id = dataSnapshot.child("user_id").getValue().toString();
                                    final String from = dataSnapshot.child("location_from").getValue().toString();
                                    final String to = dataSnapshot.child("location_to").getValue().toString();
                                    final String date = dataSnapshot.child("move_date").getValue().toString();
                                    final String time = dataSnapshot.child("move_time").getValue().toString();
                                    mAccept_process = false;
                                    viewHolder.mReject.setClickable(false);
                                    viewHolder.mAccept.setClickable(false);
                                    emailBody = "Your request was  Accepted\n ------------------------------\n"+
                                            "Request details \n -----------------------------\n"+
                                            "location you moving from: "+from+
                                            "\n location you moving to:"+to+
                                            "\n Date and time: "+date+"-"+time;

                                    mData_user.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String email;
                                            if(dataSnapshot.child(id).hasChild("email")) {
                                                email = dataSnapshot.child(id).child("email").getValue().toString();
                                                sendEmail(email,"Accepted",emailBody );
                                            }else {
                                                email = "";
                                                sendEmail(email,"Accepted", emailBody );
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
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
                                lat_from = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_from").child("longitude").getValue().toString());

                                long_to = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_to").child("latitude").getValue().toString());
                                lat_to = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_to").child("longitude").getValue().toString());

                                Intent i = new Intent(MessageActivity.this, MapActivity.class);
                                i.putExtra("latitude_from", lat_from);
                                i.putExtra("longitude_from", long_from);
                                i.putExtra("post_key", post_key);
                                i.putExtra("latitude_to", lat_to);
                                i.putExtra("longitude_to", long_to);
                                startActivity(i);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                viewHolder.mes_maps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mData_request.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                long_from = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_from").child("latitude").getValue().toString());
                                lat_from = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_from").child("longitude").getValue().toString());

                                long_to = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_to").child("latitude").getValue().toString());
                                lat_to = Double.parseDouble(dataSnapshot.child(post_key).child("latlng_to").child("longitude").getValue().toString());

                                Intent i = new Intent(MessageActivity.this, MapActivity.class);
                                i.putExtra("latitude_from", lat_from);
                                i.putExtra("longitude_from", long_from);
                                i.putExtra("post_key", post_key);
                                i.putExtra("latitude_to", lat_to);
                                i.putExtra("longitude_to", long_to);
                                //i.putExtra("Message",Message);
                                startActivity(i);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                viewHolder.mess_contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       String number = viewHolder.mess_contact.getText().toString();
                        if(!TextUtils.isEmpty(number))
                        {
                            Dial(number);
                        }else {
                            Toast.makeText(getApplicationContext(),"No number to call",Toast.LENGTH_LONG).show();
                        }

                    }
                });

                viewHolder.mV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        contextID = post_key;
                        return false;
                    }
                });
                registerForContextMenu(viewHolder.mV);
            }
        };

        LinearLayoutManager
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
    public void Dial(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber.trim()));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(callIntent);
    }
    protected void sendEmail(String To, String subject, String message) {
        Log.i("Send email", "");
        String[] TO = {To};
        String[] CC = {""};
        Intent intentEmail = new Intent(Intent.ACTION_SENDTO);
        intentEmail.setData(Uri.parse("mailto:"));
        intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{To});
        intentEmail.putExtra(intentEmail.EXTRA_SUBJECT, "Moving Services: Request "+subject);
        intentEmail.putExtra(intentEmail.EXTRA_TEXT, message);

        if (intentEmail.resolveActivity(getPackageManager()) != null) {

        }

        try {
            startActivity(intentEmail);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MessageActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
    public static class Request_ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        private TextView mUserName, mLoc_to, mLoc_from, mDate, mStatus, mMessage, mess_status,mess_dateDiff,mess_contact,mess_email;
        private ImageButton mAccept, mReject;
        private String uid, postID;
        private CircleImageView messengerImageView;
        private DatabaseReference mDataUser, mData_request;
        private CardView mes_card;
        LinearLayout lin,lin_email,lin_phone,lin_status,lin_button,mes_maps;
        private ImageView img_status;
        private View mV;
        boolean checkDate = true;
        public Request_ViewHolder(View itemView) {
            super(itemView);
            mV = itemView;

            mUserName = (TextView) itemView.findViewById(R.id.mes_username);
            mLoc_to = (TextView) itemView.findViewById(R.id.mes_location_to);
            mLoc_from = (TextView) itemView.findViewById(R.id.mes_location_from);
            mDate = (TextView) itemView.findViewById(R.id.mes_date);
            mStatus = (TextView) itemView.findViewById(R.id.mes_status);
            mAccept = (ImageButton) itemView.findViewById(R.id.mes_accept);
            mReject = (ImageButton) itemView.findViewById(R.id.mes_reject);
            mMessage = (TextView) itemView.findViewById(R.id.mes_message);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            mes_card = (CardView) itemView.findViewById(R.id.mes_card);
            mess_status = (TextView) itemView.findViewById(R.id.mes_status1);
            lin = (LinearLayout) itemView.findViewById(R.id.lin_card);
            img_status = (ImageView) itemView.findViewById(R.id.img_status1);
            mess_dateDiff =(TextView)itemView.findViewById(R.id.mes_dateDif);
            mes_maps =(LinearLayout)itemView.findViewById(R.id.mes_maps);
            mess_contact =(TextView)itemView.findViewById(R.id.mes_contact);
            mess_email =(TextView)itemView.findViewById(R.id.mes_email);
            lin_email =(LinearLayout)itemView.findViewById(R.id.lin_mess_email);
            lin_phone =(LinearLayout)itemView.findViewById(R.id.lin_mess_contact);
            lin_status =(LinearLayout)itemView.findViewById(R.id.lin_status);
            lin_button = (LinearLayout)itemView.findViewById(R.id.lin_button);
            mDataUser = FirebaseDatabase.getInstance().getReference().child("users");
            mData_request = FirebaseDatabase.getInstance().getReference().child("Request");
            mDataUser.keepSynced(true);

        }

        public void status_color(final String key, final Context context) {
            mData_request.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(key).hasChild("status") ) {
                        if(dataSnapshot.child(key).child("status").getValue()!= null) {
                            if (dataSnapshot.child(key).child("status").getValue().toString().equals("Rejected")) {

                                mess_status.setTextColor(context.getResources().getColor(R.color.reject_text));
                                mess_dateDiff.setTextColor(context.getResources().getColor(R.color.reject_text));
                                img_status.setImageResource(R.drawable.ic_block_black_24dp);
                                img_status.setColorFilter(context.getResources().getColor(R.color.reject));
                                mes_card.setClickable(false);
                                lin_button.setVisibility(View.GONE);
                                lin_status.setVisibility(View.VISIBLE);

                            } else if (dataSnapshot.child(key).child("status").getValue().toString().equals("Accepted")) {

                                mess_status.setTextColor(context.getResources().getColor(R.color.accept));
                                mess_dateDiff.setTextColor(context.getResources().getColor(R.color.accept));
                                img_status.setImageResource(R.drawable.ic_done_all_black_24dp);
                                img_status.setColorFilter(context.getResources().getColor(R.color.accept));
                                mes_card.setClickable(true);
                                lin_button.setVisibility(View.GONE);
                                mes_maps.setVisibility(View.VISIBLE);
                                lin_status.setVisibility(View.GONE);

                            } else if (dataSnapshot.child(key).child("status").getValue().toString().equals("Pending"))
                            {
                                // mStatus.setTextColor(context.getResources().getColor(R.color.pending));
                                mess_status.setTextColor(context.getResources().getColor(R.color.pending));
                                mess_dateDiff.setTextColor(context.getResources().getColor(R.color.pending));
                                img_status.setImageResource(R.drawable.ic_done_black_24dp);
                                img_status.setColorFilter(context.getResources().getColor(R.color.pending));
                                mes_card.setClickable(false);
                                mes_maps.setVisibility(View.GONE);
                                lin_status.setVisibility(View.GONE);
                                lin_button.setVisibility(View.VISIBLE);
                            }else {
                                mess_status.setTextColor(context.getResources().getColor(R.color.primaryText));
                                mess_dateDiff.setTextColor(context.getResources().getColor(R.color.primaryText));
                                img_status.setImageResource(R.drawable.ic_alarm_off_black_24dp);
                                img_status.setColorFilter(context.getResources().getColor(R.color.primaryText));
                                mes_card.setClickable(false);
                                mes_maps.setVisibility(View.GONE);
                                lin_status.setVisibility(View.VISIBLE);
                                lin_button.setVisibility(View.GONE);
                            }
                        }
                    }else {
                       // mStatus.setTextColor(context.getResources().getColor(R.color.pending));
                        mess_status.setTextColor(context.getResources().getColor(R.color.pending));
                        mess_dateDiff.setTextColor(context.getResources().getColor(R.color.pending));
                        img_status.setImageResource(R.drawable.ic_done_black_24dp);
                        img_status.setColorFilter(context.getResources().getColor(R.color.pending));
                        mes_card.setClickable(false);
                        mes_maps.setVisibility(View.GONE);
                        lin_status.setVisibility(View.GONE);
                        lin_button.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setDesc_req(String desc_req) {
            if (TextUtils.isEmpty(desc_req)) {
                mMessage.setVisibility(View.GONE);
            } else {
                mMessage.setText(desc_req);
            }
        }

        public void setLocation_from(String location_from) {
            mLoc_from.setText(location_from);
        }

        public void setLocation_to(String location_to) {
            mLoc_to.setText(location_to);
        }

        public void setMove_date(String move_date, String move_time,Context context,String post_key) {
            mDate.setText(move_date + " " + move_time);
            dateFormat(move_date,context,post_key);


        }
        @TargetApi(Build.VERSION_CODES.N)
        public void dateFormat(String datetime,Context context,String post_key)
        {

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            String Currentdate=formatter.format(Calendar.getInstance().getTime());

            try {

                Date date = formatter.parse(datetime);
                Date date1 = formatter.parse(Currentdate);
                getTimeDifference(date,date1,context,post_key);


            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        public void getTimeDifference(Date endDate, Date startDate, final Context context, final String post_key) {
            long timeDiff = endDate.getTime() - startDate.getTime();

           if (timeDiff/(24*60*60*1000) >= 0)
           {
               mess_dateDiff.setText("("+timeDiff/(24*60*60*1000)+" days left)");


           }else {
               mData_request.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       if(!dataSnapshot.child(post_key).child("status").getValue().equals("Expired")) {
                           mData_request.child(post_key).child("status").setValue("Expired");
                       }
                       mess_dateDiff.setText("(date expired)");

                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });


           }
            System.out.println("differ "+ timeDiff/(24*60*60*1000));
        }
        public void setUser_id(final Context context, String user_id) {

            uid = user_id;

            mDataUser.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String username, lname,contact,email;
                    Glide.with(context).load(dataSnapshot.child("profileimage").getValue().toString()).centerCrop().into(messengerImageView);
                    if (dataSnapshot.child("name").getValue() != null) {
                        username = dataSnapshot.child("name").getValue().toString();
                    } else {
                        username = "";
                    }
                    if (dataSnapshot.child("lname").getValue() != null) {
                        lname = dataSnapshot.child("lname").getValue().toString();

                    } else {
                        lname = "";
                    }
                    if(dataSnapshot.child("contact").getValue() != null)
                    {
                        contact = dataSnapshot.child("contact").getValue().toString();
                    }else {
                        contact ="";
                        mess_contact.setVisibility(View.GONE);
                        lin_phone.setVisibility(View.GONE);
                    }
                    if(dataSnapshot.child("email").getValue() != null)
                    {
                         email = dataSnapshot.child("email").getValue().toString();
                    }
                    else {
                        email = "";
                        mess_email.setVisibility(View.GONE);
                        lin_email.setVisibility(View.GONE);
                    }
                    mUserName.setText(username + " " + lname);
                    mess_contact.setText(contact);
                    mess_email.setText(email);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setPost_id(String post_id) {
            postID = post_id;
        }

        public void setStatus(String status) {
            if (TextUtils.isEmpty(status)) {
                mStatus.setText("Pending");
                mess_status.setText("Pending");
            } else {
                mStatus.setText(status);
                mess_status.setText(status);
            }

        }

        @Override
        public void onMapReady(GoogleMap googleMap) {

        }
    }
}
