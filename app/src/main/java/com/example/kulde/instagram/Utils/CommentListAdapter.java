package com.example.kulde.instagram.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kulde.instagram.Model.Comment;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.Profile.AccountSettings;
import com.example.kulde.instagram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentListAdapter";
    private LayoutInflater mInflater;
    private int layoutresource;
    private Context mContext;
    public CommentListAdapter(@NonNull Context context, int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutresource = resource;

    }

    private static class ViewHolder{
        TextView comment;
        CircleImageView profileImage;
        TextView username;
        TextView timestamp;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(layoutresource, parent,false);
            holder = new ViewHolder();
            holder.comment = (TextView)convertView.findViewById(R.id.comment);
            holder.username = (TextView)convertView.findViewById(R.id.comment_username);
            holder.timestamp = (TextView)convertView.findViewById(R.id.commenttime);
            holder.profileImage =(CircleImageView)convertView.findViewById(R.id.comment_profile_image);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.comment.setText(getItem(position).getComment());

        String timestampdifference = getTimestampdifference(getItem(position));
        if(!timestampdifference.equals("0")){
            holder.timestamp.setText(timestampdifference + "d");
        }else{
            holder.timestamp.setText("today");
        }

        //set userimage and profile
        Log.d(TAG, "getView: Setting Username and Profile Image");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singlesnapshot : dataSnapshot.getChildren()) {
                    holder.username.setText(singlesnapshot.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singlesnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });

        return convertView;
    }

    private String getTimestampdifference(Comment comment){
        Log.d(TAG, "getTimestampdifference: getting timestamp difference");
        String difference = "" ;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Melbourne"));
        Date timestamp;
        Date today = c.getTime();
        sdf.format(today);
        final String phototime = comment.getDate_created();
        try{
            timestamp = sdf.parse(phototime);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24  )));
        }catch(ParseException e){
            Log.d(TAG, "getTimestampdifference: Parse Exception " + e.getMessage());
            difference = "0";
        }
        return difference;
    }
}
