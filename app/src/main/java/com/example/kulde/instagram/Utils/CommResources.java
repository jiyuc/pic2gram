package com.example.kulde.instagram.Utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class CommResources {
    public static Bitmap photoFinishBitmap = null;
    public static int rotationdegree = 0;
    public static Bitmap edit_template = null;
    public static Bitmap cache = null;
    public static String location = "";
    public static Boolean isprofile = false;

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int x, int y, int newWidth, int newHeight) {

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, x, y, newWidth, newHeight);
        bm.recycle();
        return resizedBitmap;
    }



}
