package com.example.kulde.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.FirebaseMethods;
import com.example.kulde.instagram.Utils.Navigation;
import com.example.kulde.instagram.Utils.SectionPagerAdapter;
import com.example.kulde.instagram.Utils.SectionsStatePagerAdapter;

import java.util.ArrayList;

public class AccountSettings extends AppCompatActivity {

    private static final String TAG = "AccountSettings";
    private static final int ACTIVITY_NUM = 4;
    private SectionsStatePagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private RelativeLayout relativeLayout;
    private BottomNavigationView bottomNavigationView;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings_main);
        Log.d(TAG, "onCreate started");

        viewPager = (ViewPager)findViewById(R.id.viewpager_container);
        relativeLayout = (RelativeLayout)findViewById(R.id.relLayoutAccSettMain1);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavViewwBar);


        createList();
        setupFragments();
        navigation();
        getIncomingIntent();

        ImageView back_arrow = (ImageView)findViewById(R.id.backIcon);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Back to profile Activity");
                finish();
            }
        });
    }

    private void createList(){
        Log.d(TAG, "createList: setting the list in account setting");
        ListView menuList = findViewById(R.id.listOptionSettings);
        ArrayList<String> aList = new ArrayList<>();
        aList.add(getString(R.string.edit_profile)); //fragment 0
        aList.add(getString(R.string.sign_out)); // fragment 1

        ArrayAdapter adapter = new ArrayAdapter(AccountSettings.this, android.R.layout.simple_list_item_1, aList);
        menuList.setAdapter(adapter);

        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: navigating to fragment#." + position);
                setViewPager(position);
            }
        });
    }

    private void getIncomingIntent(){
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "getIncomingIntent: recieved incoming intent from " + getString(R.string.profileactivity));
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile)));
        }

    }

    private void setupFragments(){
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile)); // fragment 0
        pagerAdapter.addFragment(new SignOutFragment(),getString(R.string.sign_out)); // fragment 1
    }

    private void setViewPager(int fragmentNumber){
        relativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: Navigating to fragment number" + fragmentNumber);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(fragmentNumber);
    }

    public void navigation(){

        Navigation.enablenavigation(AccountSettings.this,this, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
