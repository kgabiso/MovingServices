package com.example.codetribe.movingservices;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.arturogutierrez.Badges;
import com.github.arturogutierrez.BadgesNotSupportedException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    RecyclerView mRecycleList;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mDatabaseLikes;
    private DatabaseReference mDatabaseDislikes;
    private DatabaseReference mDataUsers, mDataRequest;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private boolean mProcessLike = false;
    private boolean mProcessDislike = false;
    private GoogleApiClient mGoogleApiClient;
    ImageView no_data_found;
    String token;
    //keeep state of recycler View
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private BroadcastReceiver broadcastReceiver;
    private LinearLayout mRevealView;
    private boolean hidden = true;
    private ImageButton add_btn, request_btn, settings_btn, contact_btn;
    private CircleImageView profile_btn, sign_out_btn;
    private SupportAnimator animator_reverse;
    private SupportAnimator animator;
    private TextView request_count;

    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Moving_Services");
        mDataUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("likes");
        mDatabaseDislikes = FirebaseDatabase.getInstance().getReference().child("Dislikes");
        mDataRequest = FirebaseDatabase.getInstance().getReference().child("Request");
        mAuth = FirebaseAuth.getInstance();
        //keep synced
        mDataUsers.keepSynced(true);
        mDatabaseRef.keepSynced(true);
        mDatabaseLikes.keepSynced(true);
        mDatabaseDislikes.keepSynced(true);
        mDataRequest.keepSynced(true);
        no_data_found = (ImageView) findViewById(R.id.No_imageView);

        //==================== menu ==========================
        add_btn = (ImageButton) findViewById(R.id.menu_add);
        profile_btn = (CircleImageView) findViewById(R.id.menu_profile);
        request_btn = (ImageButton) findViewById(R.id.menu_request);
        settings_btn = (ImageButton) findViewById(R.id.menu_settings);
        contact_btn = (ImageButton) findViewById(R.id.menu_about);
        sign_out_btn = (CircleImageView) findViewById(R.id.menu_sign_out);
        request_count = (TextView) findViewById(R.id.count_request);
        mRevealView = (LinearLayout) findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.GONE);

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
                startActivity(intent);
                hideRevealView();
            }
        });
        profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                hideRevealView();
            }
        });
        request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                startActivity(intent);
                hideRevealView();
            }
        });
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideRevealView();
            }
        });
        contact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideRevealView();
            }
        });
        sign_out_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
                hideRevealView();
            }
        });
        //==================================
        //checkConnection();

        //+++++++++++++++++animations===========================
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    no_data_found.setVisibility(View.VISIBLE);

                } else {
                    no_data_found.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDataUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("email").getValue() == null) {
                    mDataUsers.child(mAuth.getCurrentUser().getUid()).child("email").setValue(mAuth.getCurrentUser().getEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), connectionResult.toString(), Toast.LENGTH_SHORT).show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        checkUserExist();
        displayPicture();
        mRecycleList = (RecyclerView) findViewById(R.id.recycleList);
        mRecycleList.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setReverseLayout(false);
        mRecycleList.setLayoutManager(staggeredGridLayoutManager);
        mRecycleList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                hideRevealView();
            }
        });
        countRequest();
        stopInternService();
    }

    protected void sendEmail(String To) {
        Log.i("Send email", "");
        String[] TO = {To};
        String[] CC = {""};
        Intent intentEmail = new Intent(Intent.ACTION_SENDTO);
        intentEmail.setData(Uri.parse("mailto:"));
        intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{To});
        intentEmail.putExtra(intentEmail.EXTRA_SUBJECT, "Moving Services ");
        intentEmail.putExtra(intentEmail.EXTRA_TEXT, "Message goes here");
        if (intentEmail.resolveActivity(getPackageManager()) != null) {

        }

        try {
            startActivity(intentEmail);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void countRequest() {
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
                        request_count.setText("" + count);
                        if (count > 99) {
                            request_count.setText("99+");
                        }
                        try {
                            Badges.setBadge(getApplicationContext(), count);
                        } catch (BadgesNotSupportedException badgesNotSupportedException) {
                            //Log.d(TAG, badgesNotSupportedException.getMessage());
                        }
                    } else {
                        try {
                            Badges.setBadge(getApplicationContext(), 0);
                        } catch (BadgesNotSupportedException badgesNotSupportedException) {
                            //Log.d(TAG, badgesNotSupportedException.getMessage());
                        }
                        request_count.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    public void stopInternService(){
        Intent i = new Intent(getApplicationContext(),BadgeIntentService.class);
        stopService(i);
    }
public void startInternService(){
    Intent i = new Intent(getApplicationContext(),BadgeIntentService.class);
    startService(i);
}
    //fetch user profile
    public void displayPicture() {
        if (mAuth.getCurrentUser() != null) {
            mDataUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String uri = dataSnapshot.child("profileimage").getValue().toString();
                    Glide.with(getApplicationContext()).load(uri).centerCrop().error(R.drawable.ic_account_circle_black_24dp).into(profile_btn);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {

            int cx = (mRevealView.getLeft() + mRevealView.getRight());
            int cy = mRevealView.getTop();
            int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                animator =
                        ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(700);

                animator_reverse = animator.reverse();

                if (hidden) {
                    mRevealView.setVisibility(View.VISIBLE);
                    animator.start();
                    hidden = false;
                } else {
                    animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                        @Override
                        public void onAnimationStart() {

                        }

                        @Override
                        public void onAnimationEnd() {
                            mRevealView.setVisibility(View.INVISIBLE);
                            hidden = true;

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

            }
            if (hidden) {
                Animator anim = android.view.ViewAnimationUtils.
                        createCircularReveal(mRevealView, cx, cy, 0, radius);
                mRevealView.setVisibility(View.VISIBLE);
                anim.start();
                hidden = false;
            } else {
                Animator anim = android.view.ViewAnimationUtils.
                        createCircularReveal(mRevealView, cx, cy, radius, 0);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mRevealView.setVisibility(View.INVISIBLE);
                        hidden = true;
                    }
                });
                anim.start();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void hideRevealView() {
        // Android LOLIPOP And ABOVE Version
        int cx = (mRevealView.getLeft() + mRevealView.getRight());
        int cy = mRevealView.getTop();
        int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());
        if (mRevealView.getVisibility() == View.VISIBLE) {
            Animator anim = android.view.ViewAnimationUtils.
                    createCircularReveal(mRevealView, cx, cy, radius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mRevealView.setVisibility(View.INVISIBLE);
                    hidden = true;
                }
            });
            anim.start();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {

        int cx = (mRevealView.getLeft() + mRevealView.getRight());
        int cy = mRevealView.getTop();
        int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());
        if (mRevealView.getVisibility() == View.VISIBLE) {
            Animator anim = android.view.ViewAnimationUtils.
                    createCircularReveal(mRevealView, cx, cy, radius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mRevealView.setVisibility(View.INVISIBLE);
                    hidden = true;
                }
            });
            anim.start();
        }else {
            super.onBackPressed();
            startInternService();
        }
    }

    private void logout() {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

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

    @Override
    protected void onStart() {
        super.onStart();


        mAuth.addAuthStateListener(mAuthListner);

        FirebaseRecyclerAdapter<MovingServices, MovingService_ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MovingServices, MovingService_ViewHolder>(
                MovingServices.class,
                R.layout.rows,
                MovingService_ViewHolder.class, mDatabaseRef
        ) {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            protected void populateViewHolder(final MovingService_ViewHolder viewHolder, final MovingServices model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setContactNumber(model.getContactNumber());
                viewHolder.setEmail(model.getEmail());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setLocation(model.getLocation());
                viewHolder.setImg_likes(post_key, getApplicationContext());
                viewHolder.setDate(model.getDate());
                viewHolder.img_dislike.setBackgroundTintList(getResources().getColorStateList(R.color.reject));

                viewHolder.post_Image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent singleIntent = new Intent(MainActivity.this, SingleActivity.class);
                        singleIntent.putExtra("Single_Post_id", post_key);
                        startActivity(singleIntent);
                        hideRevealView();

                    }
                });

                viewHolder.img_likes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;

                        mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        //check if user already liked
                                        mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;
                                    } else {
                                        mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Likes");
                                        mProcessLike = false;
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                viewHolder.img_dislike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessDislike = true;
                        mDatabaseDislikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessDislike) {
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        //check if user already liked
                                        mDatabaseDislikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessDislike = false;

                                    } else {

                                        mDatabaseDislikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Dislike");
                                        mProcessDislike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                viewHolder.post_email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendEmail(viewHolder.post_email.getText().toString());
                    }
                });
                viewHolder.lin_phone.setClickable(true);
                viewHolder.lin_phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dial(viewHolder.post_Contact.getText().toString());
                    }
                });
                viewHolder.lin_email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendEmail(viewHolder.post_email.getText().toString());
                    }
                });
            }

        };
        mRecycleList.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setReverseLayout(false);
        mRecycleList.setLayoutManager(staggeredGridLayoutManager);
        // staggeredGridLayoutManager.scrollToPosition(0);

        mRecycleList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();
            mDataUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent mainIntent = new Intent(MainActivity.this, SetUpActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_LONG).show();

                }
            });
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    //Class
    public static class MovingService_ViewHolder extends RecyclerView.ViewHolder {

        View nView;
        TextView post_Contact, post_email, post_likes, post_dislike, post_date;
        ImageButton img_send,
                img_likes, img_dislike;
        ImageView post_Image;
        DatabaseReference mDatabaselikes;
        DatabaseReference mDatabaseDislikes;
        FirebaseAuth mAuth;
        private LinearLayout lin_email, lin_phone;


        public MovingService_ViewHolder(View itemView) {
            super(itemView);
            nView = itemView;

            mDatabaselikes = FirebaseDatabase.getInstance().getReference().child("likes");
            mDatabaseDislikes = FirebaseDatabase.getInstance().getReference().child("Dislikes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaselikes.keepSynced(true);
            mDatabaseDislikes.keepSynced(true);


            post_Image = (ImageView) nView.findViewById(R.id.img_Post);
            post_Contact = (TextView) nView.findViewById(R.id.txtContactNumber);
            post_email = (TextView) nView.findViewById(R.id.txtContactEmail);
            post_dislike = (TextView) nView.findViewById(R.id.txtDislikes);
            post_likes = (TextView) nView.findViewById(R.id.txtLikes);
            post_date = (TextView) nView.findViewById(R.id.txtDate);
            img_dislike = (ImageButton) nView.findViewById(R.id.img_dislikes);
            img_likes = (ImageButton) nView.findViewById(R.id.img_likes);
            lin_email = (LinearLayout) nView.findViewById(R.id.lin_email);
            lin_phone = (LinearLayout) nView.findViewById(R.id.lin_phone);

        }

        public void setImg_likes(final String post_key, final Context context) {

            mDatabaselikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                            // img_likes.setImageResource(R.drawable.ic_mood_active_24dp1);
                            post_likes.setText(" " + (int) dataSnapshot.child(post_key).getChildrenCount());
                            post_likes.setTextColor(ContextCompat.getColor(nView.getContext(), R.color.accept));
                            img_dislike.setEnabled(false);
                        } else {
                            // img_likes.setImageResource(R.drawable.ic_mood_black_24dp);
                            post_likes.setText(" " + (int) dataSnapshot.child(post_key).getChildrenCount());
                            post_likes.setTextColor(ContextCompat.getColor(nView.getContext(), R.color.editText));
                            img_dislike.setEnabled(true);

                        }
                    } catch (Exception e) {

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
            mDatabaseDislikes.addValueEventListener(new ValueEventListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                            //img_dislike.setImageResource(R.drawable.ic_mood_bad_active_24dp1);
                            post_dislike.setText(" " + (int) dataSnapshot.child(post_key).getChildrenCount());
                            post_dislike.setTextColor(ContextCompat.getColor(nView.getContext(), R.color.reject));
                            img_dislike.setBackgroundTintList(context.getResources().getColorStateList(R.color.reject));
                            img_likes.setEnabled(false);

                        } else {

                            //img_dislike.setImageResource(R.drawable.ic_mood_bad_black_24dp);
                            post_dislike.setText(" " + (int) dataSnapshot.child(post_key).getChildrenCount());
                            post_dislike.setTextColor(ContextCompat.getColor(nView.getContext(), R.color.editText));
                            img_dislike.setBackgroundTintList(context.getResources().getColorStateList(R.color.reject));
                            img_likes.setEnabled(true);
                        }
                    } catch (Exception e) {

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title) {
            TextView post_title = (TextView) nView.findViewById(R.id.txtTitle);
            post_title.setText(title);
        }

        public void setContactNumber(String contactNumber) {

            post_Contact.setText(contactNumber);
        }

        public void setEmail(String email) {

            post_email.setText(email);
        }

        public void setImage(Context cxt, String image) {

            Glide.with(cxt).load(image).error(R.drawable.image_not_available).centerCrop().into(post_Image);

        }

        public void setLocation(String loction) {
            TextView post_location = (TextView) nView.findViewById(R.id.txtlocation);
            post_location.setText(loction);
        }

        public void setDate(String date) {
            post_date.setText(date);
        }

    }


}

