package com.example.kulde.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kulde.instagram.Model.Comment;
import com.example.kulde.instagram.Model.Likes;
import com.example.kulde.instagram.Model.Photo;
import com.example.kulde.instagram.Model.User;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.Model.UserSettings;
import com.example.kulde.instagram.Profile.AccountSettings;
import com.example.kulde.instagram.Profile.Profile;
import com.example.kulde.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class ViewProfile extends Fragment {
    private static final String TAG = "ProfileFragment";
    private TextView mPosts, mFollowers, mFollowing, mDisplayname, mUsername, mWebsite, mDescription,mFollow,mUnfollow;
    private ProgressBar mProgressbar;
    private CircleImageView mProfilephoto;
    private GridView gridView;
    private ImageView profileMenu,mBackarrow;
    private BottomNavigationView bottomNavigationView;
    private static final int ACTIVITY_NUM = 1;
    private static final int NUM_GRID_COLUMNS = 3;
    private Context mContext;
    private TextView editProfile;



    private User mUser;

    public interface onGridImageselector{
        void onGridImageselected(Photo photo, int activityNumber);
    }
    onGridImageselector onGridImage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference myRef;

    private int mfollowersCount = 0;
    private int mfollowingCount = 0;
    private int mPostCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container,false);
        mDisplayname = (TextView)view.findViewById(R.id.display_name);
        mUsername = (TextView)view.findViewById(R.id.profileName);
        mWebsite = (TextView)view.findViewById(R.id.website);
        mDescription = (TextView)view.findViewById(R.id.description);
        mProfilephoto = (CircleImageView) view.findViewById(R.id.profile_image);
        mPosts = (TextView)view.findViewById(R.id.tvPosts);
        mFollowers = (TextView)view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView)view.findViewById(R.id.tvFollowing);
        mProgressbar = (ProgressBar)view.findViewById(R.id.progressBar);
        gridView = (GridView)view.findViewById(R.id.gridView);
        profileMenu = (ImageView)view.findViewById(R.id.profileMenu);
        bottomNavigationView =(BottomNavigationView)view.findViewById(R.id.bottomNavViewwBar);
        mFollow = (TextView)view.findViewById(R.id.follow);
        mUnfollow = (TextView)view.findViewById(R.id.unFollow);
        mContext = getActivity();
        editProfile = (TextView)view.findViewById(R.id.textEditProfile);
        mBackarrow = (ImageView)view.findViewById(R.id.backarrow);


        Log.d(TAG, "onCreateView: stared");
        try{
            mUser = getUserfromBundle();
            init();
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException" + e.getMessage() );
            Toast.makeText(mContext,"Something went wrong",Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }
        navigation();

        setupFirebaseAuth();
        isFollowiing();
        getFollowersCount();
        getFollowingCount();
        getPostsCount();
        //setupGridview();
        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Now following "+mUser.getUsername());
                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                setFollowing();
            }
        });
        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Now Unfollowing " + mUser.getUsername());
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();

                setUnFollowing();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile));
                Intent intent = new Intent(getActivity(), AccountSettings.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profileactivity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        return view;

    }

    private void getFollowersCount(){
        mfollowersCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found Follower" + singleSnapshot.getValue());
                    mfollowersCount++;
                }
                mFollowers.setText(String.valueOf(mfollowersCount));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getFollowingCount(){
        mfollowingCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found Following" + singleSnapshot.getValue());
                    mfollowingCount++;
                }
                mFollowing.setText(String.valueOf(mfollowingCount));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getPostsCount(){
        mPostCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found Posts" + singleSnapshot.getValue());
                    mPostCount++;
                }
                mPosts.setText(String.valueOf(mPostCount));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void isFollowiing(){
        Log.d(TAG, "isFollowiing:  Checking if follwing this users");
        setUnFollowing();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.user_id)).equalTo(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user" + singleSnapshot.getValue());
                    setFollowing();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setFollowing(){
        Log.d(TAG, "setFollowing: Updating UI for Following the User");
        mFollow.setVisibility(GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(GONE);
    }

    private void setUnFollowing(){
        Log.d(TAG, "setFollowing: Updating UI for UNFollowing the User");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(GONE);
    }

    private void setCurrentUserprofile(){
        Log.d(TAG, "setFollowing: Updating UI for SHOWING CURRENT the User");
        mFollow.setVisibility(GONE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);
    }

    private void init(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.user_id)).equalTo(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user" + singleSnapshot.getValue(UserAccountSettings.class).toString());
                    UserSettings settings = new UserSettings();
                    settings.setUser(mUser);
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));
                    setProfileWidgets(settings);


                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query2 = databaseReference
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Photo> photos = new ArrayList<Photo>();
                for (DataSnapshot singlesnapshot : dataSnapshot.getChildren()){

                    Photo photo = new Photo();
                    Map<String,Object> objectMap = (HashMap<String, Object>) singlesnapshot.getValue();
                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> comments = new ArrayList<Comment>();
                    for(DataSnapshot dsnapshot: singlesnapshot.child(getString(R.string.field_comments)).getChildren()){
                        Comment comment = new Comment();
                        comment.setUser_id(dsnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dsnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dsnapshot.getValue(Comment.class).getDate_created());

                        comments.add(comment);
                    }
                    photo.setComments(comments);

                    List<Likes> likesList = new ArrayList<Likes>();
                    for(DataSnapshot dsnapshot: singlesnapshot.child(getString(R.string.field_likes)).getChildren()){
                        Likes likes  = new Likes();
                        likes.setUser_id(dsnapshot.getValue(Likes.class).getUser_id());
                        likesList.add(likes);
                    }
                    photo.setLikes(likesList);
                    photos.add(photo);
                }
            setupImageGrid(photos);


    }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });
    }

    private void setupImageGrid(final ArrayList<Photo> photos){
        Log.d(TAG, "onDataChange: Photos retrieved, Setting up Image Grid");
        int gridwidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridwidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);
        ArrayList<String> imgUrls = new ArrayList<>();
        for(int i = 0; i<photos.size(); i++){
            imgUrls.add(photos.get(i).getImage_path());
        }
        GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,"",imgUrls);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onGridImage.onGridImageselected(photos.get(position),ACTIVITY_NUM);
            }
        });
    }

    private User getUserfromBundle(){
        Log.d(TAG, "getUserfromBundle: Arguments " + getArguments());
        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.intent_user));
        }else{
            return null;
        }
    }


    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database" + userSettings.toString());
        User user = userSettings.getUser();
        UserAccountSettings settings =  userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilephoto,null,"");
        mDisplayname.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mProgressbar.setVisibility(GONE);
        mBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating Back to search");
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
            }
        });


    }
    /**
     * Function Bar Setup
     */
    public void navigation(){
        Navigation.enablenavigation(mContext,getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
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

    @Override
    public void onAttach(Context context) {
        try{
            onGridImage = (onGridImageselector)getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException : " + e.getMessage());
        }
        super.onAttach(context);
    }
}

