package com.example.kulde.instagram.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.CommResources;
import com.example.kulde.instagram.camera.filters.BnCFilter;
import com.example.kulde.instagram.share.NextActivity;

public class FilterActivity extends AppCompatActivity {

    private ImageView imageEdit;
    private Bitmap bitmapEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);


        imageEdit = findViewById(R.id.picture_view);



        // service in anpther thread
        initialService();

        // initial bottom bar
        navigation();

    }
    private void initialService() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                /*
                 * Do something
                 */
                // initial image edit studio desktop
                bitmapEdit = setImageEdit();

                // initial filter stylish module
                setFilterListener();

                // initial re-take photo module
                reCapture_Listener();


                // initial tune module
                setSeekBarListener();

                toCropListener();

                toShareListener();
            }
        });

        t.start();
    }

    private void refresh(ImageButton origin){
        // strip off all filters and go back to original style
        BitmapFilter task = new BitmapFilter(CommResources.RotateBitmap(bitmapEdit,90), origin, BitmapFilter.ORIGINAL_,false);
        task.execute();

        origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // brightness.setProgress(30);
                // contrast.setProgress(1);
                imageEdit.setImageBitmap(bitmapEdit);
                CommResources.edit_template = bitmapEdit;
                //reset tune function
                setSeekBarListener();
            }
        });
    }

    private void setFilterListener(){
        ImageButton oldStyle = findViewById(R.id.yellow);
        ImageButton origin = findViewById(R.id.origin);
        ImageButton gray = findViewById(R.id.black);
        ImageButton invert = findViewById(R.id.neon);
        ImageButton lime = findViewById(R.id.lime_stur);
        ImageButton night = findViewById(R.id.nigth_wh);
        ImageButton awe = findViewById(R.id.awe);

        filterListener(imageEdit, gray, BitmapFilter.GRAY_STYLE);
        filterListener(imageEdit, invert, BitmapFilter.BLUE_MESS);
        filterListener(imageEdit, oldStyle, BitmapFilter.OLD_STYLE);
        filterListener(imageEdit, lime, BitmapFilter.LIME_STUTTER);
        filterListener(imageEdit, awe, BitmapFilter.AWE_STRUCK);
        filterListener(imageEdit, night, BitmapFilter.NIGHT_WHIS);
        refresh(origin);



    }

    private void setSeekBarListener() {
        SeekBar contrast = findViewById(R.id.contrast_seekbar);
        SeekBar brightness = findViewById(R.id.bright_seekbar);

        new BnCFilter(contrast, imageEdit, BnCFilter.CONTRAST);
        new BnCFilter(brightness, imageEdit, BnCFilter.BRIGHTNESS);
    }

    private void filterListener(final ImageView image, final ImageButton button, final int styleNo){
        BitmapFilter task = new BitmapFilter(CommResources.RotateBitmap(bitmapEdit,90),button,styleNo,false);
        task.execute();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapFilter task = new BitmapFilter(bitmapEdit, image, styleNo,true);
                task.execute();
                //CommResources.edit_template = BitmapFilter.changeStyle(originBitmap, styleNo);
                //image.setImageBitmap(CommResources.edit_template);
                //reset tune function
                setSeekBarListener();

            }
        });

    }

    private void reCapture_Listener(){
        ImageButton backToTake = findViewById(R.id.back);
        backToTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go back to take photo again
                Intent intent = new Intent(FilterActivity.this, TakePhotoActivity.class);
                startActivity(intent);
            }
        });
    }



    private Bitmap setImageEdit(){

        Bitmap decodedBitmap = CommResources.photoFinishBitmap;
        int rotationDegrees = CommResources.rotationdegree;
        if (CommResources.photoFinishBitmap != null) {
            imageEdit.setImageBitmap(decodedBitmap);
            CommResources.edit_template = decodedBitmap;
        }
        imageEdit.setRotation(-rotationDegrees);

        return decodedBitmap;
    }

    private void navigation(){
        Button filter = findViewById(R.id.filter_navi);
        Button contrast = findViewById(R.id.edit_navi);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                findViewById(R.id.edit_panel).setVisibility(View.INVISIBLE);
                findViewById(R.id.edit_panel).setFocusable(false);
                findViewById(R.id.filter_panel).setFocusable(true);
            }
        });

        contrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                findViewById(R.id.edit_panel).setVisibility(View.VISIBLE);
                findViewById(R.id.edit_panel).setFocusable(true);
                findViewById(R.id.filter_panel).setFocusable(false);


            }
        });

    }
    private void toCropListener(){
        findViewById(R.id.crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommResources.edit_template=((BitmapDrawable)imageEdit.getDrawable()).getBitmap();
                Intent intent = new Intent(FilterActivity.this, CropActivity.class);
                startActivity(intent);
            }
        });
    }

    private void toShareListener() {
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommResources.edit_template = ((BitmapDrawable)imageEdit.getDrawable()).getBitmap();
                Intent intent = new Intent(FilterActivity.this, NextActivity.class);
                startActivity(intent);
            }
        });
    }
}
