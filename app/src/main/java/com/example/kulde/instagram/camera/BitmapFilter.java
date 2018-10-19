package com.example.kulde.instagram.camera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.widget.ImageView;


import com.example.kulde.instagram.Utils.CommResources;
import com.example.kulde.instagram.camera.filters.GrayFilter;

import com.example.kulde.instagram.camera.filters.OldFilter;
import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.imageprocessors.Filter;

import java.lang.ref.WeakReference;


public class BitmapFilter extends AsyncTask<Integer, Void, Bitmap> {

    /**
     * filter style id;
     */
    public static final int GRAY_STYLE = 1; // gray scale

    public static final int BLUE_MESS = 2; // invert the colors

    public static final int OLD_STYLE = 3; // old photo

    public static final int AWE_STRUCK = 4;

    public static final int LIME_STUTTER = 5;

    public static final int NIGHT_WHIS = 6;

    public static final int ORIGINAL_ = 0;

    private WeakReference<ImageView> imageViewReference;
    private int styleNo;
    private Bitmap bitmap;
    private boolean flag = false;

    static
    {
        System.loadLibrary("NativeImageProcessor");
    }




    /**
     * change bitmap filter style
     * @param bitmap
     * @param styleNo, filter sytle id
     */

    public BitmapFilter(Bitmap bitmap, ImageView imageView, int styleNo, boolean flag) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        this.bitmap=bitmap;
        this.styleNo=styleNo;
        this.flag = flag;
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    public static Bitmap changeStyle(Bitmap bitmap, int styleNo) {
        if (styleNo == GRAY_STYLE) {
            return GrayFilter.changeToGray(bitmap);
        }

        else if (styleNo == BLUE_MESS) {

            Filter fooFilter = SampleFilters.getBlueMessFilter();

            bitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap ouputImage = fooFilter.processFilter(bitmap);
            return ouputImage;
           // return InvertFilter.chageToInvert(bitmap);
        }

        else if (styleNo == OLD_STYLE) {
            return OldFilter.changeToOld(bitmap);
        }

        else if (styleNo == AWE_STRUCK) {
            Filter fooFilter = SampleFilters.getAweStruckVibeFilter();

            bitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap ouputImage = fooFilter.processFilter(bitmap);
            return ouputImage;
        }

        else if (styleNo == LIME_STUTTER) {
            Filter fooFilter = SampleFilters.getLimeStutterFilter();
            bitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap ouputImage = fooFilter.processFilter(bitmap);
            return ouputImage;

        }

        else if (styleNo == NIGHT_WHIS) {
            Filter fooFilter = SampleFilters.getNightWhisperFilter();
            bitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap ouputImage = fooFilter.processFilter(bitmap);
            return ouputImage;
        }
        else if (styleNo == ORIGINAL_) {

            return bitmap;
        }

        return bitmap;
    }


    @Override
    protected Bitmap doInBackground(Integer ... integers) {
        bitmap = changeStyle(bitmap, styleNo);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                if (flag) {
                    CommResources.edit_template = bitmap;
                }
                imageView.setImageBitmap(bitmap);
            }
        }
    }

}
