package com.example.kulde.instagram.Utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Void, Object> {

    private static final String TAG = "DownloadTask";

    @Override
    protected Object doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            InputStream is = url.openStream();
            File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            OutputStream os = new FileOutputStream(f+"/"+strings[1]);

            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = is.read(buffer, 0, buffer.length)) >= 0) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            is.close();
            Log.d(TAG, "Download DONE");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
