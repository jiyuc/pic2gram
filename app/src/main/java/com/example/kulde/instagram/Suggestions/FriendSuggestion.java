package com.example.kulde.instagram.Suggestions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kulde.instagram.Model.User;
import com.example.kulde.instagram.Profile.Profile;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Search.Search;
import com.example.kulde.instagram.Utils.UserlistAdapter;
import com.example.kulde.instagram.Utils.Viewpost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FriendSuggestion extends AppCompatActivity {
    private static final String TAG = "Friend Suggestion";
    private Context mContext = FriendSuggestion.this;
    private ListView listView;
    private String latestfollower;
    private String currentUser;
    private ArrayList<String> mUsers;
    private List<User> mSuggestions;
    private SuggestionlistAdapter mAdapter;
    private User latestSuggestion;
    private ImageView mBackarrow;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_friend_suggestion);
        Log.d(TAG, "onCreate: Starting " + mContext.getString(R.string.friend_suggestion));
        mSuggestions = new ArrayList<>();
        mUsers = new ArrayList<>();
        listView = (ListView)findViewById(R.id.friendlist);
        mBackarrow = (ImageView)findViewById(R.id.backarrow);
        getfriend();

    }


    private void getfriend(){
        mUsers.clear();
        mSuggestions.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUsers.add(currentUser);
        Query query = reference.child(getString(R.string.dbname_following))
                .child(currentUser);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singlesnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: Getting the Users whom current users are following" + singlesnapshot.child("user_id").getValue().toString());
                    //latestfollower = singlesnapshot.child(getString(R.string.user_id)).getValue().toString();
                    mUsers.add(singlesnapshot.child("user_id").getValue().toString());
                    
                }
                DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();
                for (int i = 0; i < mUsers.size() ; i++) {
                    if (mUsers.get(i) == currentUser) {
                        continue;
                    } else {
                        Log.i("Member name: ", mUsers.get(i));
                        Query query1 = dbreference.child(getString(R.string.dbname_following))
                                .child(mUsers.get(i));
                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    try{
                                    latestfollower = singleSnapshot.child(getString(R.string.user_id)).getValue().toString();
                                    latestSuggestion = singleSnapshot.getValue(User.class);
                                    if (!mUsers.contains(latestfollower) && !mSuggestions.contains(latestSuggestion)) {
                                        Log.d(TAG, "onDataChange: User for Suggestion from are : " + latestfollower);
                                        if (latestfollower != null) {
                                                Log.d(TAG, "onDataChange: Getting User class" + singleSnapshot.getValue(User.class).toString());
                                                mSuggestions.add(singleSnapshot.getValue(User.class));
                                                updateSuggestionList();
                                        }
                                    }
                                    }catch (Exception e ){
                                        Log.e(TAG, "onDataChange: Friend Suggestion Exception" + e.getMessage() );
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        }
    private void updateSuggestionList(){
        Log.d(TAG, "updateSuggestionList: Updating the Suggestion list");
        mAdapter = new SuggestionlistAdapter(FriendSuggestion.this,R.layout.friend_suggestion,mSuggestions);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " + mSuggestions.get(position).toString());
                Intent intent = new Intent(mContext,Profile.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), mSuggestions.get(position));
                startActivity(intent);
            }
        });
    }
}



