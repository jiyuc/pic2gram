package com.example.kulde.instagram.camera;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.CommResources;
import com.example.kulde.instagram.share.NextActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CropActivity extends AppCompatActivity {

    CropImageView cropImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        setcropImage();
    }

    private void setcropImage() {

        cropImage = findViewById(R.id.crop_view);
        cropImage.setImageBitmap(CommResources.edit_template);
        cropImage.setRotation(-CommResources.rotationdegree);
        initialService();
        setBacktoFilterListener();
    }

    private void initialService() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                setApplyListener();
            }
        });

        t.start();
    }

    private void setApplyListener() {
        findViewById(R.id.apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropListener();
                Intent intent = new Intent(CropActivity.this, NextActivity.class);
                startActivity(intent);
            }
        });
    }


    private void setBacktoFilterListener() {
        findViewById(R.id.back_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommResources.cache=CommResources.edit_template;
                Intent intent = new Intent(CropActivity.this, FilterActivity.class);
                startActivity(intent);
            }
        });
    }
    private void cropListener() {

        cropImage.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
            }
        });
        CommResources.edit_template = cropImage.getCroppedImage();

    }
}
