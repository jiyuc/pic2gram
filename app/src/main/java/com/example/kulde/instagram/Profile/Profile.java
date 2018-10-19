package com.example.kulde.instagram.Profile;


import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.kulde.instagram.Model.Photo;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.ViewComments;
import com.example.kulde.instagram.Utils.ViewProfile;
import com.example.kulde.instagram.Utils.Viewpost;

//import static com.example.kulde.instagram.R.menu.profile_menu;

public class Profile extends AppCompatActivity implements ProfileFragment.onGridImageselector,Viewpost.OnCommentThreadSelectedListener, ViewProfile.onGridImageselector {
    private static final String TAG = "Profile Activity";
    private static final int ACTIVITY_NUM = 4;
    private Context mContext = Profile.this;
    private ProgressBar mProgressbar;
    private ImageView profilephoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: Activity Profile Starting......");
        init();

   }

    private void init() {
        Log.d(TAG, "init() called " + getString(R.string.profile_fragment));
        Intent intent = getIntent();

        if(intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: Searching for user object attached as Intent Extra");
            if (intent.hasExtra(getString(R.string.intent_user))) {
                Log.d(TAG, "init: Inflating ViewProfile");
                ViewProfile fragment = new ViewProfile();
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.intent_user), intent.getParcelableExtra(getString(R.string.intent_user)));

                fragment.setArguments(args);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.view_profile_fragment));
                transaction.commit();
            } else {
                Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
            else{
            Log.d(TAG, "init: Inflating Profile");
            ProfileFragment frag = new ProfileFragment();
            FragmentTransaction ftrans = Profile.this.getSupportFragmentManager().beginTransaction();
            ftrans.replace(R.id.container, frag);
            ftrans.addToBackStack(getString(R.string.profile_fragment));
            ftrans.commit();
            }

        }


    @Override
    public void onGridImageselected(Photo photo, int activitynumber) {
        Log.d(TAG, "onGridImageselected: Selected Image from Grid View" + photo.toString());
        Viewpost post = new Viewpost();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo),photo);
        args.putInt(getString(R.string.activity_number), activitynumber);

        post.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, post);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    @Override
    public void OnCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "OnCommentThreadSelectedListener: Selected a Comment Thread");
        ViewComments fragment = new ViewComments();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo),photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_comments));
        transaction.commit();
    }
}