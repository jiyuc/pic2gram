package com.example.kulde.instagram.Home;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.kulde.instagram.Model.Comment;
import com.example.kulde.instagram.Model.Likes;
import com.example.kulde.instagram.Model.Photo;
import com.example.kulde.instagram.Model.UserAccountSettings;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.Mainfeedlistadapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.kulde.instagram.R.layout.main_page_fragment;

public class FragmentHome extends Fragment {
    private static final String TAG = "FragmentHome";

    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private Mainfeedlistadapter mAdapter;
    private int mResults;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_page_fragment, container, false);
        mListView = (ListView)view.findViewById(R.id.listview);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        getFollowing();

        return view;
    }

    private void getFollowing(){
        Log.d(TAG, "getFollowing: Searching for Following");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                   // Log.d(TAG, "onDataChange: Found Users " +
                    //singleSnapshot.child(getString(R.string.user_id)).getValue());
                    mFollowing.add(singleSnapshot.child("user_id").getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                getPhotos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void getPhotos(){
        Log.d(TAG, "getPhotos: Getting Photos");
        for(int i = 0; i < mFollowing.size(); i++) {
            final int count = i;
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                       try {
                           Photo photo = new Photo();
                           Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                           photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                           if(objectMap.containsKey(getString(R.string.field_city_name))){
                               photo.setCityName(objectMap.get(getString(R.string.field_city_name)).toString());
                           }else{
                               photo.setCityName("");
                           }
                           photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                           photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                           photo.setUser_id(objectMap.get(getString(R.string.user_id)).toString());
                           photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                           photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                           Log.d(TAG, "onDataChange: Getting the location of the image" + objectMap.get(getString(R.string.field_city_name).toString()));
                           // photo.setCityName(objectMap.get(R.string.field_city_name).toString());

                           ArrayList<Comment> comments = new ArrayList<Comment>();
                           for (DataSnapshot dsnapshot : singleSnapshot.child(getString(R.string.field_comments)).getChildren()) {
                               Comment comment = new Comment();
                               comment.setUser_id(dsnapshot.getValue(Comment.class).getUser_id());
                               comment.setComment(dsnapshot.getValue(Comment.class).getComment());
                               comment.setDate_created(dsnapshot.getValue(Comment.class).getDate_created());
                               comments.add(comment);
                           }
                           photo.setComments(comments);
                           mPhotos.add(photo);
                       }catch (Exception e){
                           Log.e(TAG, "onDataChange: Exception" + e.getMessage() );
                       }
                    }
                    if(count >= mFollowing.size() -1){
                        // display our photos
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayPhotos(){
        mPaginatedPhotos = new ArrayList<>();
        if(mPhotos != null){
            try{
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });
                int iterations = mPhotos.size();
                if(iterations > 10){
                    iterations = 10;
                }
                mResults = 10;
                for(int i = 0;i < iterations ; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mAdapter = new Mainfeedlistadapter(getActivity(),R.layout.layout_mainfeed_item, mPaginatedPhotos);
                mListView.setAdapter(mAdapter);
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException " +e.getMessage() );
            }

        }
    }

    public void displaymorephotos(){
        Log.d(TAG, "displaymorephotos: Displaying more photos");
        try{
            if(mPhotos.size() > mResults && mPhotos.size() > 0){
                int iterations;
                if(mPhotos.size() > (mResults + 10)){
                    Log.d(TAG, "displaymorephotos: there are more than 10 photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displaymorephotos: there are less than 10 more photos");
                    iterations = mPhotos.size() - mResults;
                }
                //add the new photos to the paginated results
                for(int i = mResults; i < mResults + iterations;i++){
                    mPaginatedPhotos.add(mPhotos.get(i));

                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException " +e.getMessage() );
        }
    }
}
