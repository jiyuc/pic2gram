package com.example.kulde.instagram.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.kulde.instagram.Home.MainPage;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.CommResources;
import com.example.kulde.instagram.Utils.FirebaseInteraction;
import com.example.kulde.instagram.share.NextActivity;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link PhotoPreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoPreviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int CONFIRM = 1;
    private static final int EDIT = 2;

    // TODO: Rename and change types of parameters
    ImageView imagePreview;
    Button confirm;
    Button edit;
    Bitmap bmp;





    public PhotoPreviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment PhotoPreviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoPreviewFragment newInstance() {
        PhotoPreviewFragment fragment = new PhotoPreviewFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        imagePreview = view.findViewById(R.id.photo_preview);
        confirm = view.findViewById(R.id.confirm);
        edit = view.findViewById(R.id.edit);
        CommResources.isprofile = false;

        try {
            setImageView(imagePreview);
            initialService();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void initialService() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                /*
                 * Do something
                 */


                    setButtonListener(CONFIRM);
                    setButtonListener(EDIT);

            }
        });

        t.start();
    }

    private void setButtonListener(int mode){
        switch (mode){
            case CONFIRM:
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // link to share activity
                        passBitmap();

                        // upload photo to storage async


                        //back to main feed
                        Intent intent = new Intent(getContext(), NextActivity.class);
                        startActivity(intent);
                        getActivity().finish();

                    }
                });
                break;
            case EDIT:
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // link to edit activity
                        Intent intent = new Intent(getContext(), FilterActivity.class);
                        passBitmap();

                        startActivity(intent);
                        getActivity().finish();
                    }
                });

        }


    }


    private void passBitmap(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CommResources.photoFinishBitmap = ((BitmapDrawable)imagePreview.getDrawable()).getBitmap();
        CommResources.edit_template = CommResources.photoFinishBitmap;
    }




    @Override
    public void onDetach() {
        super.onDetach();
    }



    private Bitmap setImageView(ImageView imageEdit) throws IOException {

        bmp = CommResources.photoFinishBitmap;
        imageEdit.setImageBitmap(CommResources.photoFinishBitmap);
        imageEdit.setRotation(-CommResources.rotationdegree);
        return bmp;
    }
}
