package com.example.kulde.instagram.camera.filters;

import android.graphics.Bitmap;

import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.kulde.instagram.Utils.CommResources;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter;


public class BnCFilter {

    SeekBar seekbar;
    static int fb, c = 1;
    public static int BRIGHTNESS = 1;
    public static int CONTRAST = 2;
    private ImageView vi;
    private int mode;

    private int brightnessFinal = 0;
    private float contrastFinal = 1.0f;

    public BnCFilter(SeekBar seekbar, ImageView vi, int mode) {
        this.seekbar = seekbar;
        this.vi = vi;
        this.mode = mode;
        if (mode == BRIGHTNESS) {
            seekbar.setMax(200);
            seekbar.setProgress(100);
        }
        else {
            seekbar.setMax(20);
            seekbar.setProgress(0);
        }
        setSeekbarListener();
    }
    static
    {
        System.loadLibrary("NativeImageProcessor");
    }


    private void setSeekbarListener() {

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //set the progress value
                if (mode == BRIGHTNESS) {

                    BnCFilterAsync task = new BnCFilterAsync(vi,seekBar,BRIGHTNESS);
                    task.execute();
                }
                if (mode == CONTRAST) {

                    BnCFilterAsync task = new BnCFilterAsync(vi,seekBar,CONTRAST);
                    task.execute();
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mode == BRIGHTNESS) {

                }
                if (mode == CONTRAST) {

                }
            }
        });

    }

}
