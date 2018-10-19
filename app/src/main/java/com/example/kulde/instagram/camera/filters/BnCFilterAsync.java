package com.example.kulde.instagram.camera.filters;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.kulde.instagram.Utils.CommResources;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter;

import java.lang.ref.WeakReference;


public class BnCFilterAsync extends AsyncTask<Integer, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private int data = 0;
    private int brightnessFinal = 0;
    private float contrastFinal = 1.0f;
    private SeekBar seekBar;
    public static int BRIGHTNESS = 1;
    public static int CONTRAST = 2;
    private int mode;

    public BnCFilterAsync(ImageView imageView,SeekBar seekBar, int mode) {
        this.seekBar=seekBar;
        this.mode=mode;
        imageViewReference = new WeakReference<ImageView>(imageView);
    }


    @Override
    protected Bitmap doInBackground(Integer... integers) {
        Bitmap bmp=null;
        if (mode == BRIGHTNESS) {
            int progress = seekBar.getProgress();
            int fb = progress-100;
            bmp = brightIt(fb, CommResources.edit_template);
        }
        if (mode == CONTRAST) {
            int progress = seekBar.getProgress();
            progress += 10;
            float c = .10f * progress;
            bmp = contrastIt(c, CommResources.edit_template);
        }
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private Bitmap brightIt(final int brightness, Bitmap finalImage) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubfilter(brightness));
        return myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true));

    }

    private Bitmap contrastIt(final float contrast, Bitmap finalImage) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubfilter(contrast));
        return myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true));
    }
}
