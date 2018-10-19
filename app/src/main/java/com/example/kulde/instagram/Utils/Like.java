package com.example.kulde.instagram.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Like  {
    private static final String TAG = "Like";

    public ImageView mHeartRed, mHeartwhite;

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    public Like(ImageView mHeartRed, ImageView mHeartwhite) {
        this.mHeartRed = mHeartRed;
        this.mHeartwhite = mHeartwhite;
    }

    public void toggleLike(){
        Log.d(TAG, "toggleLike: toggling heart");

        AnimatorSet animationSet = new AnimatorSet();
        if(mHeartRed.getVisibility() == View.VISIBLE){
            Log.d(TAG, "toggleLike: toggling the redheart off");
            mHeartRed.setScaleX(0.1f);
            mHeartRed.setScaleY(0.1f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mHeartRed,"scaleY",1f,0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mHeartRed,"scaleX",1f,0f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

            mHeartRed.setVisibility(View.GONE);
            mHeartwhite.setVisibility(View.VISIBLE);

            animationSet.playTogether(scaleDownY, scaleDownX);

        }

        else if(mHeartRed.getVisibility() == View.GONE){
            Log.d(TAG, "toggleLike: toggling the redheart on");
            mHeartRed.setScaleX(0.1f);
            mHeartRed.setScaleY(0.1f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mHeartRed,"scaleY",0.1f,1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mHeartRed,"scaleX",0.1f,1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            mHeartRed.setVisibility(View.VISIBLE);
            mHeartwhite.setVisibility(View.GONE);

            animationSet.playTogether(scaleDownY, scaleDownX);

        }
        animationSet.start();
    }
}
