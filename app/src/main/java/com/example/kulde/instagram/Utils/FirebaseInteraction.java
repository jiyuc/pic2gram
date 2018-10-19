package com.example.kulde.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.kulde.instagram.Model.Photo;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.camera.FilterActivity;
import com.example.kulde.instagram.camera.TakePhotoActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseInteraction extends AsyncTask{

    private static final String TAG = "FirebaseInteraction";
    private String userID;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference myRef;
    FirebaseAuth mAuth;
    FirebaseDatabase fbaseDB;

    Bitmap bitmap;
    private double mPhotoUploadProgress = 0;
    Context mContext;
    String caption;
    String cityName;
    String phototype;



    public FirebaseInteraction(Context context, String phototype, Bitmap bitmap, String caption, String cityName) {

        //
        this.bitmap=bitmap;

        fbaseDB = FirebaseDatabase.getInstance();
        myRef = fbaseDB.getReference();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
        mContext = context;
        this.caption=caption;
        this.cityName = cityName;
        this.phototype = phototype;

    }

    @Override
    protected Object doInBackground(Object[] objects) {
        storageRef = FirebaseStorage.getInstance().getReference();
        uploadImageBitmap(bitmap);
        return null;
    }

    private void uploadImageBitmap( Bitmap bitmap) {
        // upload image via bitmap
        int count = 1;
        bitmap = rotateBitmap(bitmap, -CommResources.rotationdegree);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        assert storageRef != null;
        FilePaths filePaths = new FilePaths();
        StorageReference imagesRef = storageRef.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + userID + "/photo" + (count + 1));


        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();


                //add the new photo to 'photos' node and 'user_photos' node
                addPhotoToDatabase(phototype, caption, downloadUrl.toString(),cityName);

                Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();



            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                if(progress - 15 > mPhotoUploadProgress){
                    Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                    mPhotoUploadProgress = progress;
                }

            }
        });
    }

    private Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String phototype, String caption, String url, String cityName){

        // add get tag method

        //insert into databse
        Log.d(TAG, url);

        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();

        Log.d(TAG, newPhotoKey);


        Photo photo = new Photo(caption,
                getTimestamp(),
                url, newPhotoKey,
                userID,
                StringManipulation.getTags(caption),
                cityName);

        Log.d("location", photo.toString());

        Log.d("profile_edit",phototype);

        if (phototype.equals("profile")) {

            myRef.child("user_account_settings")
                    .child(userID).child("profile_photo").setValue(url);

            //myRef.child("photos").child(newPhotoKey).setValue(photo);
        } else {
            myRef.child("user_photos")
                    .child(userID).child(newPhotoKey).setValue(photo);

            myRef.child("photos").child(newPhotoKey).setValue(photo);

            // add code to reload profile photo

        }



        Log.d(TAG, myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).toString());



    }



}
