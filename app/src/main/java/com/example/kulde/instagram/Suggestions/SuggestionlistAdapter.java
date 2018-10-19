package com.example.kulde.instagram.Suggestions;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kulde.instagram.Model.User;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.Profile.Profile;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.FirebaseMethods;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SuggestionlistAdapter extends ArrayAdapter<User> {

    private static final String TAG = "UserlistAdapter";
    AppCompatActivity appCompatActivity;
    private LayoutInflater mInflater;
    private List<User> mUsers = null;
    private int layoutresource;
    private Context mContext;
    private User suggestion;
    private FirebaseMethods mFirebaseMethods;

    public SuggestionlistAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        Log.d(TAG, "UserlistAdapter: Welcome to List adapter");
        
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutresource = resource;
        this.mUsers = objects;
    }


    private static class ViewHolder{
        TextView username,displayname;
        CircleImageView profileImage;
        ImageView backarrow;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "getView: Going to database");
        suggestion =  getItem(position);

        final ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(layoutresource,parent,false);
            holder = new ViewHolder();
            holder.displayname = (TextView)convertView.findViewById(R.id.display_name);
            holder.username = (TextView)convertView.findViewById(R.id.username);
            holder.profileImage = (CircleImageView)convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();

        }


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: found User" + dataSnapshot.toString());
                    Log.d(TAG, "getView: Getting User name " + dataSnapshot.child("username").getValue().toString());
                    holder.username.setText(dataSnapshot.child("username").getValue().toString());
                    Log.d(TAG, "getView: Getting Display name " + dataSnapshot.child("display_name").getValue().toString());
                    holder.displayname.setText(dataSnapshot.child("display_name").getValue().toString());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(dataSnapshot.child("profile_photo").getValue().toString(),holder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return convertView;
    }
}
