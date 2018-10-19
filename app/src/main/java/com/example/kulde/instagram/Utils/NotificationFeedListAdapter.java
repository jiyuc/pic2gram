package com.example.kulde.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import com.example.kulde.instagram.Home.MainPage;
import com.example.kulde.instagram.Model.Comment;
import com.example.kulde.instagram.Model.Likes;
import com.example.kulde.instagram.Model.Notice;
import com.example.kulde.instagram.Model.Photo;
import com.example.kulde.instagram.Model.User;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.Profile.Profile;
import com.example.kulde.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationFeedListAdapter extends ArrayAdapter<Notice> {

    public interface OnLoadMoreItems{
        void onLoadMoreItems();
    }
    OnLoadMoreItems monLoadMoreItems;


    private static final String TAG = "notificationFeedList";

    private LayoutInflater mLayoutInflator;
    private int mLayoutresource;
    private Context mContext;
    private String display_name_from;
    private String display_name_to;

    public NotificationFeedListAdapter(@NonNull Context context, int resource, @NonNull List<Notice> objects) {
        super(context, resource, objects);
        mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutresource = resource;
        this.mContext = context;
    }

    private static class ViewHolder{
        TextView notification,date;
        CircleImageView profileImage;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mLayoutInflator.inflate(mLayoutresource, parent, false);
            holder = new ViewHolder();

            holder.notification = (TextView) convertView.findViewById(R.id.context);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.date = (TextView) convertView.findViewById(R.id.datediff);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }


        //set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings));
//                .child(getItem(position).getUser_id_from());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d(TAG,"current user is"+dataSnapshot.child("display_name").getValue().toString());
                for(DataSnapshot dsnapshot: dataSnapshot.getChildren()) {
                    if(dsnapshot.child("user_id").getValue().toString().equals(getItem(position).getUser_id_from())){
                        if(FirebaseAuth.getInstance().getCurrentUser().getUid().toString().equals(getItem(position).getUser_id_from())){
                            display_name_from = "You";
                        }else{
                            display_name_from = "Your friend " + dsnapshot.child("display_name").getValue().toString();
                        }

                        ImageLoader imageLoader = ImageLoader.getInstance();
                        imageLoader.displayImage(
                                dsnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                                holder.profileImage);
                    }
                    if(dsnapshot.child("user_id").getValue().toString().equals(getItem(position).getUser_id_to())) {
                        if(FirebaseAuth.getInstance().getCurrentUser().getUid().toString().equals(getItem(position).getUser_id_to())){
                            display_name_to = "your photo!";
                        }else{
                            display_name_to = dsnapshot.child("display_name").getValue().toString()+"'s photo!";
                        }
                    }
                }

                String action= getItem(position).getAction();
                if(getItem(position).getAction().equals("commented")){
                    action = "commented on";
                }

                holder.notification.setText(display_name_from + " " + action + " " + display_name_to);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
//        Log.d("TAG",holder.display_name.getText().toString());
        String timeDifference = getTimestampdifference(getItem(position));
        if(!timeDifference.equals("0")){
            holder.date.setText(timeDifference + " Days Ago");

        }else{
            holder.date.setText("Today");
        }
        return convertView;
    }


    private String getTimestampdifference(Notice mnotice){
        Log.d(TAG, "getTimestampdifference: getting timestamp difference"+mnotice.getDate_created());
        String difference = "" ;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Melbourne"));
        Date timestamp;
        Date today = c.getTime();
        sdf.format(today);
        final String noticetime = mnotice.getDate_created();
        try{
            timestamp = sdf.parse(noticetime);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24  )));
        }catch(ParseException e){
            Log.d(TAG, "getTimestampdifference: Parse Exception " + e.getMessage());
            difference = "0";
        }
        return difference;
    }
}
