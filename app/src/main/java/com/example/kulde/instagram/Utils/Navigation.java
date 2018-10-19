package com.example.kulde.instagram.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.kulde.instagram.Home.MainPage;
import com.example.kulde.instagram.Profile.Profile;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Search.Search;
import com.example.kulde.instagram.UserFeed.Userfeed;
import com.example.kulde.instagram.camera.TakePhotoActivity;

public class Navigation {
    private static final String TAG = "Navigation";

    public static void enablenavigation(final Context context, final Activity callingActivity,BottomNavigationView bottomNavigationView){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.ic_house:
                        Intent intent1 = new Intent(context, MainPage.class);
                        context.startActivity(intent1);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_search:
                        Intent intent4 = new Intent(context, Search.class);
                        context.startActivity(intent4);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_add:
                        Intent intent2 = new Intent(context, TakePhotoActivity.class);
                        context.startActivity(intent2);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_like:
                        Intent intent5 = new Intent(context,Userfeed.class);
                        context.startActivity(intent5);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_profile:
                        Intent intent3 = new Intent(context, Profile.class);
                        context.startActivity(intent3);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }
}
