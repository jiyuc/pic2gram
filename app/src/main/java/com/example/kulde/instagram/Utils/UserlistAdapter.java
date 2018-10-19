package com.example.kulde.instagram.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kulde.instagram.Model.User;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserlistAdapter extends ArrayAdapter<User> {

    private static final String TAG = "UserlistAdapter";

    private LayoutInflater mInflater;
    private List<User> mUsers = null;
    private int layoutresource;
    private Context mContext;
    
    public UserlistAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        Log.d(TAG, "UserlistAdapter: Welcome to List adapter");
        
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutresource = resource;
        this.mUsers = objects;
    }


    private static class ViewHolder{
        TextView username;
        CircleImageView profileImage;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "getView: Going to database");
        final ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(layoutresource,parent,false);
            holder = new ViewHolder();

            holder.username = (TextView)convertView.findViewById(R.id.username);
            holder.profileImage = (CircleImageView)convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();

        }

        holder.username.setText(getItem(position).getUsername());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleDatasnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found User" + singleDatasnapshot.getValue(UserAccountSettings.class).toString());
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleDatasnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return convertView;
    }
}
