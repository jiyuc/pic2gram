package com.example.kulde.instagram.Home;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.kulde.instagram.LogIn;
import com.example.kulde.instagram.Model.Photo;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.Mainfeedlistadapter;
import com.example.kulde.instagram.Utils.Navigation;
import com.example.kulde.instagram.Utils.SectionPagerAdapter;
import com.example.kulde.instagram.Utils.UniversalImageLoader;
import com.example.kulde.instagram.Utils.ViewComments;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nostra13.universalimageloader.core.ImageLoader;


public class MainPage extends AppCompatActivity implements Mainfeedlistadapter.OnLoadMoreItems {


    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: Displaying More photos");
        FragmentHome fragmentHome = (FragmentHome)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());

        if(fragmentHome != null){
            fragmentHome.displaymorephotos();
        }
    }
    
    private static final String TAG = "MainPage";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;

    private Context mainContext = MainPage.this;

    //firebase thing
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ViewPager mViewPager;
    private FrameLayout frameLayout;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Log.d(TAG, "onCreate: Starting......");
        mViewPager = (ViewPager)findViewById(R.id.viewpager_container);
        frameLayout = (FrameLayout)findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.relLayoutparent);
        setupFirebaseAuth();
        initimageloader();
        navigation();
        setupViewpager();

    }

    public void onCommentThreadSelected(Photo photo, String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: Selected a comment thread");
        ViewComments fragment = new ViewComments();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity),getString(R.string.home_activity));
        fragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments));
        transaction.commit();


    }

    //firebase thing starts here
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser");
        if(user == null){
            Intent intent = new Intent(mainContext, LogIn.class);
            startActivity(intent);
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                checkCurrentUser(user);
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

    private void initimageloader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mainContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mViewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    public void navigation(){
        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavViewwBar);
        Navigation.enablenavigation(MainPage.this,this, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }
    private void setupViewpager(){
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentHome());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager_container);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.nuhuu);

    }

    public void hidelayout(){
        Log.d(TAG, "hidelayout: Hidding layout");
        mRelativeLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
    }


    public void showlayout(){
        Log.d(TAG, "hidelayout: showing layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(frameLayout.getVisibility() == View.VISIBLE){
            showlayout();
        }
    }

}
