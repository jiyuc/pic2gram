package com.example.kulde.instagram.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.kulde.instagram.Model.User;
import com.example.kulde.instagram.Profile.Profile;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.Navigation;
import com.example.kulde.instagram.Utils.UserlistAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Search extends AppCompatActivity{
    private static final String TAG = "Main Activity";
    private static final int ACTIVITY_NUM = 1;
    private Context mContext = Search.this;

    private EditText mSearchParam;
    private ListView mListView;

    private List<User> mUserList;
    private UserlistAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mSearchParam=(EditText)findViewById(R.id.search);
        mListView=(ListView)findViewById(R.id.listview);
        Log.d(TAG, "onCreate: Starting......");
        hidesoftkeyboard();
        navigation();
        initTextListener();
    }

    private void searchforMatch(String keyword){
        Log.d(TAG, "searchforMatch: Searching for a match " + keyword);
        mUserList.clear();
        if(keyword.length() == 0){

        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .startAt(keyword)
                    .endAt(keyword+"\uf8ff");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                       try {
                           Log.d(TAG, "onDataChange: found user" + singleSnapshot.getValue(User.class).toString());
                           mUserList.add(singleSnapshot.getValue(User.class));
                           updateUserList();
                       }catch (Exception e){
                           Log.e(TAG, "onDataChange: Searching User Exception " + e.getMessage() );
                       }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }



    private void updateUserList(){
        Log.d(TAG, "updateUserList: Updating Users list");

        mAdapter = new UserlistAdapter(Search.this,R.layout.layout_user_listitem,mUserList);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " + mUserList.get(position).toString());

                //Navigating to the user's Profile
                Intent intent = new Intent(Search.this,Profile.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
                startActivity(intent);

            }
        });

    }

    private void initTextListener(){
        Log.d(TAG, "initTextListener: initializing");
        mUserList = new ArrayList<>();
        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: getting value for ");
                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                searchforMatch(text);
            }
        });
    }
    private void hidesoftkeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

        }
    }

    /**
     * Function Bar Setup
     */
    public void navigation(){
        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavViewwBar);
        Navigation.enablenavigation(Search.this,this, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
