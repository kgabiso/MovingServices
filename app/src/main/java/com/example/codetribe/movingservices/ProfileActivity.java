package com.example.codetribe.movingservices;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class ProfileActivity extends AppCompatActivity {

    private TextView displayname, email, sign_out,txtcontact,request_num;
    private LinearLayout refresh;
    private RelativeLayout prof_pic_upload;
    private CircleImageView prof_pic;
    private DatabaseReference mDB_users;
    private FirebaseAuth mAuth;
    private Button btn_Update;
    private EditText ed_displayName, ed_lastName,ed_contact;
    SpotsDialog dialog;
    private static int Gallery_REQUEST = 1;
    private Uri imgUri;
    private StorageReference storage;
    private LovelyStandardDialog lovelyStandardDialog;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private DatabaseReference mDb_request;
    private RecyclerView recyclerView;
    private boolean click = true;
    private SupportAnimator animator_reverse;
    private SupportAnimator animator;
    private String contextid;
    private RotateAnimation rotate_clock,rotate_antiClock;
    ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        lovelyStandardDialog = new LovelyStandardDialog(this);
        dialog = new SpotsDialog(this);
        mDB_users = FirebaseDatabase.getInstance().getReference().child("users");
        storage = FirebaseStorage.getInstance().getReference().child("profile_picture");
        mDb_request = FirebaseDatabase.getInstance().getReference().child("Request");
        mDb_request.keepSynced(true);
        mDB_users.keepSynced(true);
        mDB_users.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        displayname = (TextView) findViewById(R.id.txtprofile_display_name);
        email = (TextView) findViewById(R.id.txt_profile_email);
        sign_out = (TextView) findViewById(R.id.txt_sign_out);
        prof_pic_upload = (RelativeLayout) findViewById(R.id.profile_photo);
        prof_pic = (CircleImageView) findViewById(R.id.circle_profile_image);
        btn_Update = (Button) findViewById(R.id.btn_update);
        ed_displayName = (EditText) findViewById(R.id.ed_displayName);
        ed_lastName = (EditText) findViewById(R.id.ed_lastName);
        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_profile();
            }
        });
        refresh = (LinearLayout) findViewById(R.id.txtRefresh);
        recyclerView = (RecyclerView)findViewById(R.id.prof_recycler) ;
        icon =(ImageView)findViewById(R.id.txtRefresh_icon);
        txtcontact =(TextView)findViewById(R.id.txt_profile_contact);
        ed_contact =(EditText)findViewById(R.id.ed_contact);
        request_num =(TextView)findViewById(R.id.num_request);
        AuthListener();

        recyclerView.setVisibility(View.GONE);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideRequest();
            }
        });


        prof_pic_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_REQUEST);
            }
        });
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lovelyStandardDialog
                        .setTopColorRes(R.color.colorAccent)
                        .setButtonsColorRes(R.color.colorPrimaryDark)
                        .setIcon(R.drawable.delivery_truck_icon)
                        .setTitle("Sign out")
                        .setMessage("Do you want to Sign out ?")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mAuth.signOut();
                                AuthListener();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();


            }
        });
        fetchProfile();
        countRequest();
    }
    public void clockwise(){
        rotate_clock = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate_clock.setFillAfter(true);
        rotate_clock.setDuration(300);
        rotate_clock.setInterpolator(new AccelerateDecelerateInterpolator());
        icon.startAnimation(rotate_clock);
    }
    public void anti_clockwise(){
        rotate_antiClock = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate_antiClock.setFillAfter(true);
        rotate_antiClock.setDuration(300);
        rotate_antiClock.setInterpolator(new AccelerateDecelerateInterpolator());
        icon.startAnimation(rotate_antiClock);
    }
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public  void hideRequest(){

        int cx = (recyclerView.getLeft() + recyclerView.getRight());
        int cy = recyclerView.getTop();
        int radius = Math.max(recyclerView.getWidth(), recyclerView.getHeight());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            animator =
                    ViewAnimationUtils.createCircularReveal(recyclerView, cx, cy, 0, radius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(700);

            animator_reverse = animator.reverse();

            if (click) {
                recyclerView.setVisibility(View.VISIBLE);
                animator.start();
                clockwise();
                click = false;
            }else {
                animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                    @Override
                    public void onAnimationStart() {
                        anti_clockwise();
                    }

                    @Override
                    public void onAnimationEnd() {
                        recyclerView.setVisibility(View.GONE);
                        click = true;

                    }

                    @Override
                    public void onAnimationCancel() {

                    }

                    @Override
                    public void onAnimationRepeat() {

                    }
                });
                animator_reverse.start();
            } // Android LOLIPOP And ABOVE Version

        } if (click) {
            Animator anim = android.view.ViewAnimationUtils.
                    createCircularReveal(recyclerView, cx, cy, 0, radius);
        recyclerView.setVisibility(View.VISIBLE);
            anim.start();
        click = false;
        clockwise();
        } else {
        anti_clockwise();
            Animator anim = android.view.ViewAnimationUtils.
                    createCircularReveal(recyclerView, cx, cy, radius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    recyclerView.setVisibility(View.GONE);
                    click = true;

                }
            });
            anim.start();
        }

}
    public void fetchProfile() {

        if (mAuth.getCurrentUser() != null) {
            dialog.show();
            String uid = mAuth.getCurrentUser().getUid();

            mDB_users.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String uri_profile = dataSnapshot.child("profileimage").getValue().toString();
                    String name;
                    if(dataSnapshot.child("name").getValue()!= null) {
                         name = (String) dataSnapshot.child("name").getValue();
                    }else {
                        ed_displayName.setHint("Add First name");
                        name ="Add First name";
                    }
                    String lname;
                    if(dataSnapshot.child("lname").getValue()!= null) {
                        lname = (String) dataSnapshot.child("lname").getValue();
                    }else {
                        ed_lastName.setHint("Add Last name");
                        lname = "Add Last name ";
                    }
                    String contact;
                    if(dataSnapshot.hasChild("contact")) {
                       contact = (String) dataSnapshot.child("contact").getValue();
                    }else {
                        ed_contact.setHint("Add Contact number");
                        contact = "Add Contact number";
                    }
                    Glide.with(ProfileActivity.this).load(uri_profile).centerCrop().into(prof_pic);
                    displayname.setText(name + "  " + lname);
                    txtcontact.setText(contact);
                    ed_displayName.setHint(name);
                    ed_lastName.setHint(lname);
                    ed_contact.setHint(contact);
                    email.setText(mAuth.getCurrentUser().getEmail());
                    System.out.println(dataSnapshot.child("profileimage").getValue().toString());
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void AuthListener() {
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();
                }
            }
        };
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
            mDb_request.child(contextid).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError == null)
                    {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Deleted successfully",Toast.LENGTH_LONG).show();
                    }else {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();

                    }


                }
            });

        }else if(item.getItemId() == R.id.action_Open)
        {
            dialog.show();
            mDb_request.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(contextid).hasChild("post_id")) {
                        String value = dataSnapshot.child(contextid).child("post_id").getValue().toString();
                        Intent i = new Intent(ProfileActivity.this, SingleActivity.class);
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
        mAuth.addAuthStateListener(mAuthListner);

        FirebaseRecyclerAdapter<Request_status,Request_status_ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Request_status, Request_status_ViewHolder>(
                Request_status.class,R.layout.request_status,
                Request_status_ViewHolder.class,mDb_request.orderByChild("user_id").equalTo(mAuth.getCurrentUser().getUid())
        ) {
            @Override
            protected void populateViewHolder(Request_status_ViewHolder viewHolder, Request_status model, int position) {
                    final String key = getRef(position).getKey();

                viewHolder.setPost_id(model.getPost_id(),getApplicationContext());
                viewHolder.setUser_id(model.getUser_id());
                viewHolder.setOwner_id(model.getOwner_id());
                viewHolder.getStatus(key,getApplicationContext());
                viewHolder.setRequest_date(model.getRequest_date());

                viewHolder.mview.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        contextid =key;
                        return false;
                    }
                });
                registerForContextMenu(viewHolder.mview);
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
    public void countRequest() {
        if (mAuth.getCurrentUser() != null) {
            mDb_request.orderByChild("user_id").equalTo(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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
                        request_num.setText("" + count);
                        if(count > 99)
                        {
                            request_num.setText("99+");
                        }
                    } else {

                        request_num.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_REQUEST && resultCode == RESULT_OK) {
            imgUri = data.getData();
            Glide.with(getApplicationContext()).load(imgUri).centerCrop().error(R.drawable.image_not_available).into(prof_pic);
            CropImage.activity(imgUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);
            if (imgUri != null) {
                dialog.show();
                final String uid = mAuth.getCurrentUser().getUid();
                StorageReference filepath = storage.child(imgUri.getLastPathSegment());
                filepath.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if(taskSnapshot.getTask().isSuccessful()) {
                            String downloadUri = taskSnapshot.getDownloadUrl().toString();
                            mDB_users.child(uid).child("profileimage").setValue(downloadUri);
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "profile photo successfully updated", Toast.LENGTH_SHORT).show();
                        }else {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), taskSnapshot.getError().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (requestCode == RESULT_OK) {

                imgUri = result.getUri();

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception exception = result.getError();
            }
        }
    }

    public void update_profile() {
        if (mAuth.getCurrentUser() != null) {
            dialog.show();
            final String uid = mAuth.getCurrentUser().getUid();
            mDB_users.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!TextUtils.isEmpty(ed_displayName.getText().toString().trim())) {
                        mDB_users.child(uid).child("name").setValue(ed_displayName.getText().toString().trim());
                        Toast.makeText(getApplicationContext(),"name successfully updated",Toast.LENGTH_SHORT).show();
                        ed_displayName.setText("");
                        dialog.dismiss();
                    }
                    if (!TextUtils.isEmpty(ed_lastName.getText().toString().trim())) {
                        mDB_users.child(uid).child("lname").setValue(ed_lastName.getText().toString().trim());
                        Toast.makeText(getApplicationContext(),"last name successfully updated",Toast.LENGTH_SHORT).show();
                        ed_lastName.setText("");
                        dialog.dismiss();
                    }
                    if(!TextUtils.isEmpty(ed_contact.getText().toString().trim())){
                        mDB_users.child(uid).child("contact").setValue(ed_contact.getText().toString().trim());
                        Toast.makeText(getApplicationContext(),"contact successfully updated",Toast.LENGTH_SHORT).show();
                        ed_contact.setText("");
                        dialog.dismiss();
                    }



                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(),databaseError.getMessage().toString(),Toast.LENGTH_LONG).show();

                }
            });
        }


    }
    public static class Request_status_ViewHolder extends RecyclerView.ViewHolder{


       ImageView circleImageView;
        TextView txtcompany_name, txtphone, txtemail, txtstatus, txtDate;
        ImageView img_status;
        private DatabaseReference mDB_company,mDB_request;
        private View mview;

        public Request_status_ViewHolder(View itemView) {
            super(itemView);

            mview = itemView;
            circleImageView = (ImageView) itemView.findViewById(R.id.img_company);
            txtcompany_name =(TextView)itemView.findViewById(R.id.status_company);
            txtemail =(TextView)itemView.findViewById(R.id.status_email);
            txtDate =(TextView)itemView.findViewById(R.id.status_date);
            txtphone =(TextView)itemView.findViewById(R.id.status_phone);
            txtstatus=(TextView)itemView.findViewById(R.id.txtStatus);
            img_status =(ImageView)itemView.findViewById(R.id.img_status);
            mDB_company = FirebaseDatabase.getInstance().getReference().child("Moving_Services");
            mDB_request = FirebaseDatabase.getInstance().getReference().child("Request");
            mDB_request.keepSynced(true);
            mDB_company.keepSynced(true);
        }
        public void setOwner_id(String owner_id) {
           String id = owner_id;

        }
        public void setPost_id(final String post_id, final Context context) {
           String id = post_id;

            mDB_company.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(post_id != null) {
                        String company_name = dataSnapshot.child(post_id).child("title").getValue().toString();
                        String phone = dataSnapshot.child(post_id).child("contactNumber").getValue().toString();
                        String email = dataSnapshot.child(post_id).child("emailAddress").getValue().toString();
                        String picture = dataSnapshot.child(post_id).child("image").getValue().toString();
                        if(dataSnapshot.child(post_id).hasChild("request_date")) {
                           String date = dataSnapshot.child(post_id).child("request_date").getValue().toString();
                            txtDate.setText(date);
                        }
                        txtcompany_name.setText(company_name);
                        txtphone.setText(phone);
                        txtemail.setText(email);

                        Glide.with(context).load(picture).error(R.drawable.image_not_available).centerCrop().into(circleImageView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }
        public void setUser_id(String user_id) {
           String id = user_id;

        }
        public void getStatus(final String id, final Context context){

            mDB_request.addValueEventListener(new ValueEventListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(id).hasChild("status")) {
                        String status = dataSnapshot.child(id).child("status").getValue().toString();
                        if (status.equals("Accepted")) {
                            img_status.setImageResource(R.drawable.ic_done_all_black_24dp);
                            img_status.setColorFilter(context.getResources().getColor(R.color.accept));
                            txtstatus.setText(status);
                            txtstatus.setTextColor(context.getResources().getColor(R.color.accept));
                        } else if (status.equals("Rejected")) {
                            img_status.setImageResource(R.drawable.ic_block_black_24dp);
                            img_status.setColorFilter(context.getResources().getColor(R.color.reject));
                            txtstatus.setText(status);
                            txtstatus.setTextColor(context.getResources().getColor(R.color.reject));
                        }else {
                            img_status.setImageResource(R.drawable.ic_done_black_24dp);
                            img_status.setColorFilter(context.getResources().getColor(R.color.pending));
                            txtstatus.setText(status);
                            txtstatus.setTextColor(context.getResources().getColor(R.color.pending));
                        }
                    }else {
                        img_status.setImageResource(R.drawable.ic_done_black_24dp);
                        img_status.setColorFilter(context.getResources().getColor(R.color.pending));
                        txtstatus.setText("Pending");
                        txtstatus.setTextColor(context.getResources().getColor(R.color.pending));

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
        public void setRequest_date(String request_date) {
            txtDate.setText(request_date);
        }

    }

}
