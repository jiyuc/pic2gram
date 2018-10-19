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

import com.example.kulde.instagram.Home.MainPage;
import com.example.kulde.instagram.Model.Comment;
import com.example.kulde.instagram.Model.Likes;
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

public class Mainfeedlistadapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItems{
        void onLoadMoreItems();
    }
    OnLoadMoreItems monLoadMoreItems;
    

    private static final String TAG = "Mainfeedlistadapter";

    private LayoutInflater mLayoutInflator;
    private int mLayoutresource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";

    public Mainfeedlistadapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutresource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder {
        CircleImageView mProfileImage;
        String likesString;
        TextView username, location,timedetail, caption, likes, comments;
        SquareImageView imageView;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likebyCurrentUser;
        Like heart;
        GestureDetector detector;
        Photo photo;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflator.inflate(mLayoutresource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.imageView = (SquareImageView) convertView.findViewById(R.id.postimage);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timedetail = (TextView) convertView.findViewById(R.id.image_time);
            holder.mProfileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.location = (TextView)convertView.findViewById(R.id.location);
            holder.heart = new Like(holder.heartRed, holder.heartWhite);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        getCurrentUsername();

        getLikesString(holder);

        //set the caption
        Log.d(TAG, "getView: Getting the caption of the Image" + getItem(position).getCaption());
        holder.caption.setText(getItem(position).getCaption());

        //set the location
        Log.d(TAG, "getView: Getting the location of the Image" + getItem(position).getCityName());
        holder.location.setText(getItem(position).getCityName());

        //Set the comment
        List<Comment> comments = getItem(position).getComments();
        if(comments.size() > 0 ) {
            holder.comments.setText("View all " + comments.size() + " comments");
        }
        else{
            holder.comments.setText(" ");
        }
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Loading comment thread for " + getItem(position).getPhoto_id());
                ((MainPage)mContext).onCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));
                ((MainPage)mContext).hidelayout();
            }
        });

        String timeDifference = getTimestampdifference(getItem(position));
        if(!timeDifference.equals("0")){
            holder.timedetail.setText(timeDifference + "Days Ago");
            
        }else{
            holder.timedetail.setText("Today");
        }

        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.imageView);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    //currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: Found User for username and Image" + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: Navigating to profile of " + holder.user.getUsername());
                            Intent intent = new Intent(mContext, Profile.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user),holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.mProfileImage);
                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: Navigating to profile of " + holder.user.getUsername());
                            Intent intent = new Intent(mContext, Profile.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user),holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainPage)mContext).onCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));
                            ((MainPage)mContext).hidelayout();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
         Query userQuery = reference1
                 .child(mContext.getString(R.string.dbname_users))
                 .orderByChild(mContext.getString(R.string.user_id))
                 .equalTo(getItem(position).getUser_id());
         userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                     //currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                     Log.d(TAG, "onDataChange: Found user : " + singleSnapshot.getValue(User.class).getUsername());
                     holder.user = singleSnapshot.getValue(User.class);
                 }
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });

         if(reachedEndofList(position)){
             loadmoredata();
         }

        return convertView;
        
    }

    private boolean reachedEndofList(int position){



        return position == getCount() - 1;
        
    }

    private void loadmoredata(){
        try{
            monLoadMoreItems = (OnLoadMoreItems)getContext();
            
        }catch (ClassCastException e){
            Log.e(TAG, "loadmoredata: ClassCastException" + e.getMessage() );
        }
        try{
            monLoadMoreItems.onLoadMoreItems();

        }catch (NullPointerException e){
            Log.e(TAG, "loadmoredata: NullPointerException" + e.getMessage() );
        }                                                                          

    }

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: Retrieving User from the user account setting");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
                Log.d(TAG, "getCurrentUsername: username is "+ currentUsername);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        ViewHolder mholder;

        public GestureListener(ViewHolder holder) {
            mholder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: Doublt tap detected");
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mholder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singledatasnapshot : dataSnapshot.getChildren()) {
                        String keyID = singledatasnapshot.getKey();
                        //case1: when user has liked the photos
                        if (mholder.likebyCurrentUser && singledatasnapshot.getValue(Likes.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mholder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mholder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mholder.heart.toggleLike();
                            getLikesString(mholder);
                        }
                        //case2: when the user has not liked the photos
                        else if (!mholder.likebyCurrentUser) {
                            //add new like
                            addNewlike(mholder);
                            break;
                        }

                    }
                    if (!dataSnapshot.exists()) {
                        addNewlike(mholder);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
        return sdf.format(new Date());
    }

    private void addNewlike(ViewHolder holder) {
        Log.d(TAG, "addNewlike: Adding New Like");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String newLikeID = reference.push().getKey();
        Likes likes = new Likes();
        likes.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        likes.setDate_created(getTimestamp());

        reference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(likes);

        reference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(likes);
        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting Likes string");
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot singledatasnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                        Query query = databaseReference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.user_id))
                                .equalTo(singledatasnapshot.getValue(Likes.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singledatasnapshot : dataSnapshot.getChildren()) {

                                    Log.d(TAG, "onDataChange: found like : " + singledatasnapshot.getValue(User.class).getUsername());
                                    holder.users.append(singledatasnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }
                                String[] splitUsers = holder.users.toString().split(",");
                                if (holder.users.toString().contains(currentUsername + ",")) {
                                    holder.likebyCurrentUser = true;
                                } else {
                                    holder.likebyCurrentUser = false;
                                }

                                int length = splitUsers.length;
                                if (length == 1) {
                                    holder.likesString = "Liked by " + splitUsers[0];
                                } else if (length == 2) {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + "and " + splitUsers[1];
                                } else if (length == 3) {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1] + " and " + splitUsers[2];
                                } else if (length == 4) {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + splitUsers[3];
                                } else if (length > 4) {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + (splitUsers.length - 3) + "others";
                                }
                                //setupWidgets();
                                setupLikesstring(holder, holder.likesString);  
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        holder.likesString = "";
                        holder.likebyCurrentUser = false;
                        //setupWidgets();
                        setupLikesstring(holder, holder.likesString);  
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "getLikesString: NullPointerException" + e.getMessage());
            holder.likesString = "";
            holder.likebyCurrentUser = false;
            setupLikesstring(holder, holder.likesString);
        }
    }

    private void setupLikesstring(final ViewHolder holder,String likesString) {
        Log.d(TAG, "setupLikesstring: likes string" + holder.likesString);
        if (holder.likebyCurrentUser) {
            Log.d(TAG, "setupLikesstring: photo is liked  by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        } else {
            Log.d(TAG, "setupLikesstring: photo is not liked  by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(likesString);
    }

    private String getTimestampdifference(Photo mphoto){
            Log.d(TAG, "getTimestampdifference: getting timestamp difference");
            String difference = "" ;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
            sdf.setTimeZone(TimeZone.getTimeZone("Australia/Melbourne"));
            Date timestamp;
            Date today = c.getTime();
            sdf.format(today);
            final String phototime = mphoto.getDate_created();
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