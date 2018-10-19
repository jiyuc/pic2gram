package com.example.kulde.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kulde.instagram.Model.Comment;
import com.example.kulde.instagram.Model.Likes;
import com.example.kulde.instagram.Model.Photo;
import com.example.kulde.instagram.Model.User;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.Profile.AccountSettings;
import com.example.kulde.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Viewpost extends Fragment {

    private static final String TAG = "Viewpost";

    public interface OnCommentThreadSelectedListener{
        void OnCommentThreadSelectedListener(Photo photo);
    }
    OnCommentThreadSelectedListener mOnCommentTreadSelectedListener;

    private Photo mphoto;

    private int mActivityNumber=0;

//    private Context mContext;
    private SquareImageView mPostImage;
    private BottomNavigationView bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes, mComments, editProfile;
    private ImageView mBackarrow, mEllipses, mHeartwhite,mHeartred, mProfileImage, mComment;

    private UserAccountSettings accountSettings;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private GestureDetector mGesture;
    private Like mlike;
    private String mUserphoto;
    private String mProfileUrl;
    private Boolean mLikedbyCurrentuser;
    private StringBuilder mUsers;
    private String mLikesstring = "";
    private String currentUsername = null;

    private User mUser;

    public Viewpost() {
        super();
        setArguments(new Bundle());
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post,container,false);
        mPostImage = (SquareImageView)view.findViewById(R.id.postimage);
//        bottomNavigationView = (BottomNavigationView)view.findViewById(R.id.bottomNavViewwBar);
        mBackarrow = (ImageView)view.findViewById(R.id.imageBackArrow);
        mBackLabel = (TextView)view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView)view.findViewById(R.id.image_caption);
        mUsername = (TextView)view.findViewById(R.id.username);
        mTimestamp = (TextView)view.findViewById(R.id.image_time);
        mEllipses = (ImageView)view.findViewById(R.id.ivmenu);
        mHeartwhite = (ImageView)view.findViewById(R.id.image_heart);
        mHeartred = (ImageView)view.findViewById(R.id.image_heart_red);
        mProfileImage = (ImageView)view.findViewById(R.id.profile_photo);
        mGesture = new GestureDetector(getActivity(),new GestureListener());
        mComment = (ImageView)view.findViewById(R.id.speech_bubble);
        mComments = (TextView)view.findViewById(R.id.image_comments);
        mlike = new Like(mHeartred,mHeartwhite);
        mLikes = (TextView)view.findViewById(R.id.image_likes);

        setupFirebaseAuth();
        getCurrentUsername();

            mphoto = getphotofrombundle();
            UniversalImageLoader.setImage(mphoto.getImage_path(),mPostImage,null,"");
            mActivityNumber = getActivitynumBundle();
            getPhotodetails();
            getLikesString();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnCommentTreadSelectedListener = (OnCommentThreadSelectedListener)getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException "+e.getMessage());
        }
    }

    private void getLikesString() {
        Log.d(TAG, "getLikesString: getting Likes string");
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(getString(R.string.dbname_photos))
                    .child(mphoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUsers = new StringBuilder();
                    for (DataSnapshot singledatasnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        Log.d(TAG,"singledatasnapshot for each like"+singledatasnapshot);
                        Query query = databaseReference
                                .child(getString(R.string.dbname_users))
                                .orderByChild(getString(R.string.user_id))
                                .equalTo(singledatasnapshot.getValue(Likes.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singledatasnapshot : dataSnapshot.getChildren()) {

                                    Log.d(TAG, "onDataChange: found like : " + singledatasnapshot.getValue(User.class).getUsername());
                                    mUsers.append(singledatasnapshot.getValue(User.class).getUsername());
                                    mUsers.append(",");
                                }
                                String[] splitUsers = mUsers.toString().split(",");
                                if (mUsers.toString().contains(currentUsername + ",")) {
                                    mLikedbyCurrentuser = true;
                                } else {
                                    mLikedbyCurrentuser = false;
                                }

                                int length = splitUsers.length;
                                if (length == 1) {
                                    mLikesstring = "Liked by " + splitUsers[0];
                                } else if (length == 2) {
                                    mLikesstring = "Liked by " + splitUsers[0]
                                            + "and " + splitUsers[1];
                                } else if (length == 3) {
                                    mLikesstring = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1] + " and " + splitUsers[2];
                                } else if (length == 4) {
                                    mLikesstring = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + splitUsers[3];
                                } else if (length > 4) {
                                    mLikesstring = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + (splitUsers.length - 3) + "others";
                                }
                                setupWidgets();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        mLikesstring = "";
                        mLikedbyCurrentuser = false;
                        setupWidgets();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e) {
            Log.e(TAG, "getLikesString: NullPointerException" + e.getMessage());
        }
    }


    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: Retrieving User from the user account setting");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getActivity().getString(R.string.dbname_users))
                .orderByChild(getActivity().getString(R.string.user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
                Log.d(TAG, "getCurrentUsername: username is "+ currentUsername);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: Double tap detected");
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(getString(R.string.dbname_photos))
                    .child(mphoto.getPhoto_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    DataSnapshot dataSnapshot_likes = dataSnapshot.child(getString(R.string.field_likes));
                    for(DataSnapshot singledatasnapshot : dataSnapshot_likes.getChildren()){
                        String keyID = singledatasnapshot.getKey();
                        //case1: when user has liked the photos
                        if(mLikedbyCurrentuser && singledatasnapshot.getValue(Likes.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mphoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            String photo_user_id = dataSnapshot.child("user_id").getValue().toString();
                            Log.d(TAG,"photo_user_id is "+photo_user_id);

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(photo_user_id)
                                    .child(mphoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mlike.toggleLike();

                            getLikesString();
                        }
                        //case2: when the user has not liked the photos
                        else if(!mLikedbyCurrentuser){
                            //add new like
                            addNewlike();
                            break;
                        }

                    }
                    if(!dataSnapshot.exists()){
                        addNewlike();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewlike(){
        Log.d(TAG, "addNewlike: Adding New Like");
        String newLikeID = myRef.push().getKey();
        Likes likes = new Likes();
        likes.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        likes.setDate_created(getTimestamp());

        myRef.child(getString(R.string.dbname_photos))
                .child(mphoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(likes);

        myRef.child(getString(R.string.dbname_user_photos))
                .child(mphoto.getUser_id())
                .child(mphoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(likes);
        mlike.toggleLike();
        getLikesString();
    }
    private int getActivitynumBundle(){
        Log.d(TAG, "getActivitynumBundle: arguments "+ getArguments());
        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getInt(getString(R.string.activity_number));

        }else{
            return 0;
        }
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
        return sdf.format(new Date());
    }


    private Photo getphotofrombundle(){
        Log.d(TAG, "getphotofrombundle: arguments "+ getArguments());
        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.photo));

        }else{
            return null;
        }
    }

    private void getPhotodetails(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.user_id))
                .equalTo(mphoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singlesnapshot : dataSnapshot.getChildren()) {
                    accountSettings = singlesnapshot.getValue(UserAccountSettings.class);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });
    }
    private String getTimestampdifference(){
        Log.d(TAG, "getTimestampdifference: getting timestamp difference");
        String difference = "" ;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Melbourne"));
        Date timestamp;
        Date today = c.getTime();
        sdf.format(today);
        final String phototime = mphoto.getDate_created();
        try{
            timestamp = sdf.parse(phototime);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24  )));
        }catch(ParseException e){
            Log.d(TAG, "getTimestampdifference: Parse Exception " + e.getMessage());
            difference = "0";
        }
        return difference;
    }

    private void setupWidgets(){
        String timestampdiff = getTimestampdifference();
        if(!timestampdiff.equals("0")){
            mTimestamp.setText(timestampdiff + "days ago");
        }else{
            mTimestamp.setText("Today");
        }
        UniversalImageLoader.setImage(accountSettings.getProfile_photo(),mProfileImage,null,"");
        mUsername.setText(accountSettings.getUsername());
        mLikes.setText(mLikesstring);
        mCaption.setText(mphoto.getCaption());

        if(mphoto.getComments().size() > 0){
            String comment_context = "View all " + mphoto.getComments().size() + " comments";
            //for(Comment comment:mphoto.getComments()){

              //  comment_context = comment_context+comment.getComment()+"\n";

            //}
            mComments.setText(comment_context);
        }else
            {
                mComments.setText("  ");
            }
        mBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating back");
                mOnCommentTreadSelectedListener.OnCommentThreadSelectedListener(mphoto);
            }
        });

        if(mLikedbyCurrentuser) {
            mHeartwhite.setVisibility(View.GONE);
            mHeartred.setVisibility(View.VISIBLE);
            mHeartred.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected");
                    return mGesture.onTouchEvent(event);
                }
            });
        }else {
            mHeartwhite.setVisibility(View.VISIBLE);
            mHeartred.setVisibility(View.GONE);

            mHeartwhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: white heart touch detected");
                    return mGesture.onTouchEvent(event);
                }
            });
        }
    }
    public void navigation(){
        Navigation.enablenavigation(getActivity(),getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }


    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth");
        mAuth = FirebaseAuth.getInstance();
        mFirebasedatabase = FirebaseDatabase.getInstance();
        myRef = mFirebasedatabase.getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //somebody signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in" + user.getUid());
                } else{
                    //nobodys here
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}