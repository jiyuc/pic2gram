package com.example.kulde.instagram.UserFeed;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.eschao.android.widget.elasticlistview.LoadFooter;
import com.example.kulde.instagram.LogIn;
import com.example.kulde.instagram.Model.Comment;
import com.example.kulde.instagram.Model.Likes;
import com.example.kulde.instagram.Model.Notice;
import com.example.kulde.instagram.Model.Photo;
import com.example.kulde.instagram.Model.User;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.Profile.Profile;
import com.example.kulde.instagram.R;


import com.example.kulde.instagram.Utils.Like;
import com.example.kulde.instagram.Utils.Mainfeedlistadapter;
import com.example.kulde.instagram.Utils.NotificationFeedListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.kulde.instagram.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserFeedFragment extends Fragment{
    private static final String TAG = "UserFeedFragment";
    private static final int ACTIVITY_NUM = 3;
    private Context mContext;
    private BottomNavigationView bottomNavigationView;
    private ListView mListView;


    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("message");
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private NotificationFeedListAdapter adapter;
    private HashMap<String,String> nameList =new HashMap<String,String>();
    private ArrayList<String> followingList =new ArrayList<String>();
    private HashMap<Integer,String> idList = new HashMap<Integer,String>();
    private HashMap<String,String> dateList = new HashMap<String,String>();
    private HashMap<String,String> typeList = new HashMap<String,String>();
    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<Likes> likes = new ArrayList<>();
    private ArrayList<Notice> notices = new ArrayList<>();

    private String latestFollower;
    private TextView mFollowerName;
    private CircleImageView mFollowerImage;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;

    //widgets
    private ViewPager viewPager;

    public void onUpdate() {
        Log.d(TAG, "ElasticListView: updating list view...");

//        getFollowing();
    }


    public void onLoad() {
        Log.d(TAG, "ElasticListView: loading...");

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userfeed, container, false);
        mListView = (ListView) view.findViewById(R.id.listview);
        mFollowerImage = (CircleImageView) view.findViewById(R.id.latest_follower_photo);
        mFollowerName = (TextView) view.findViewById(R.id.latest_follower);

        getLatestFollower();
        getFollowingList();

        return view;
    }

    private void getLatestFollower(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singledatasnapshot : dataSnapshot.getChildren()) {

                    latestFollower = singledatasnapshot.child("user_id").getValue().toString();
                    Log.d(TAG,"latest following is " + latestFollower);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    Query query = databaseReference
                            .child(getString(R.string.dbname_user_account_settings))
                            .child(latestFollower);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG,"latest following is " + dataSnapshot.child("display_name").getValue().toString());

                            final User following = dataSnapshot.getValue(User.class);
                            mFollowerImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "onClick: Navigating to profile of " + following.getUsername());
                                    Intent intent = new Intent(getActivity(), Profile.class);
                                    intent.putExtra(getString(R.string.calling_activity), getString(R.string.activity_userfeed));
                                    intent.putExtra(getString(R.string.intent_user),following);
                                    startActivity(intent);
                                }
                            });

                            mFollowerName.setText("You just followed "+dataSnapshot.child("display_name").getValue().toString() + ".");
                            ImageLoader imageLoader = ImageLoader.getInstance();

                            imageLoader.displayImage(
                                    dataSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                                    mFollowerImage);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    break;
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getLatestFeeds(String userid){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_user_photos))
                .child(userid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singlesnapshot : dataSnapshot.getChildren()){
                    for(DataSnapshot dsnapshot: singlesnapshot.child(getString(R.string.field_comments)).getChildren()){
                        Notice notice = new Notice();
                        notice.setUser_id_to(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                        notice.setAction("commented");
                        notice.setDate_created(dsnapshot.getValue(Comment.class).getDate_created());
                        notice.setUser_id_from(dsnapshot.getValue(Comment.class).getUser_id());
                        comments.add(dsnapshot.getValue(Comment.class));
                        notices.add(notice);
//
                    }

                    for(DataSnapshot dsnapshot: singlesnapshot.child(getString(R.string.field_likes)).getChildren()){
                        Notice notice = new Notice();
                        notice.setUser_id_to(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                        notice.setAction("liked");
                        notice.setDate_created(dsnapshot.getValue(Likes.class).getDate_created());
                        notice.setUser_id_from(dsnapshot.getValue(Likes.class).getUser_id());
                        notices.add(notice);
                        likes.add(dsnapshot.getValue(Likes.class));
                    }
                }

                Log.d(TAG, "setupListview: display " + notices.size());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });
    }
    private void setupListview(){
        Log.d(TAG, "setupListview: Settig up");




    }

    private void getFollowingList(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found Following" + singleSnapshot.child("user_id").getValue());
                    final String userid = singleSnapshot.child("user_id").getValue().toString();

                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    Query query = databaseReference
                            .child(getString(R.string.dbname_user_photos))
                            .child(userid);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot singlesnapshot : dataSnapshot.getChildren()){
                                for(DataSnapshot dsnapshot: singlesnapshot.child(getString(R.string.field_comments)).getChildren()){
                                    Notice notice = new Notice();
                                    notice.setUser_id_to(userid);
                                    notice.setAction("commented");
                                    notice.setDate_created(dsnapshot.getValue(Comment.class).getDate_created());
                                    notice.setUser_id_from(dsnapshot.getValue(Comment.class).getUser_id());
                                    comments.add(dsnapshot.getValue(Comment.class));
                                    notices.add(notice);
//
                                }

                                for(DataSnapshot dsnapshot: singlesnapshot.child(getString(R.string.field_likes)).getChildren()){
                                    Notice notice = new Notice();
                                    notice.setUser_id_to(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                                    notice.setAction("liked");
                                    notice.setDate_created(dsnapshot.getValue(Likes.class).getDate_created());
                                    notice.setUser_id_from(dsnapshot.getValue(Likes.class).getUser_id());
                                    notices.add(notice);
                                    likes.add(dsnapshot.getValue(Likes.class));
                                }
                            }


                            Log.d(TAG, "setupListview: display2 " + notices.size());
                            adapter = new NotificationFeedListAdapter(getActivity(),R.layout.list_item_layout,notices);
                            mListView.setAdapter(adapter);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: Query Cancelled");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}