package com.example.kulde.instagram.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.kulde.instagram.Home.MainPage;
import com.example.kulde.instagram.R;
import com.example.kulde.instagram.Utils.CommResources;
import com.example.kulde.instagram.Utils.PermissionsDelegate;
import com.example.kulde.instagram.camera.filters.GridLines;
import com.example.kulde.instagram.share.ShareActivity;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.error.CameraErrorListener;
import io.fotoapparat.exception.camera.CameraException;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.view.CameraView;
import io.fotoapparat.view.FocusView;

import static io.fotoapparat.log.LoggersKt.fileLogger;

import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.selector.AspectRatioSelectorsKt.standardRatio;
import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FlashSelectorsKt.autoRedEye;
import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.on;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.LensPositionSelectorsKt.front;
import static io.fotoapparat.selector.PreviewFpsRangeSelectorsKt.highestFps;
import static io.fotoapparat.selector.ResolutionSelectorsKt.highestResolution;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;
import static io.fotoapparat.selector.SensorSensitivitySelectorsKt.highestSensorSensitivity;

public class TakePhotoActivity extends AppCompatActivity {

    private static final String LOGGING_TAG = "Photo Capture";

    private CameraView cameraView;
    private boolean hasCameraPermission;
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private Fotoapparat fotoapparat;
    private ImageButton capture;
    private ImageButton torchSwitch;
    private ImageButton gallery;
    private boolean flash_on = false;
    private GridLines gridLines;
    private ImageButton switchcm;
    private boolean activeCameraBack = true;
    private FocusView focusview;

    private CameraConfiguration cameraConfiguration = CameraConfiguration
            .builder()
            .photoResolution(standardRatio(
                    highestResolution()
            ))
            .flash(firstAvailable(
                    autoRedEye(),
                    autoFlash(),
                    torch(),
                    off()
            ))
            .previewFpsRange(highestFps())
            .sensorSensitivity(highestSensorSensitivity())
            .frameProcessor(new SampleFrameProcessor())
            .build();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_take_photo);
        backToMain();  // back to mainPage

        cameraView = findViewById(R.id.camera_view);
        capture = findViewById(R.id.capture);
        gallery = findViewById(R.id.gallary);
        switchcm = findViewById(R.id.switch_camera);
        focusview = findViewById(R.id.focusView);

        hasCameraPermission = permissionsDelegate.hasCameraPermission();
        if (hasCameraPermission) {
            cameraView.setVisibility(View.VISIBLE);


        } else {
            permissionsDelegate.requestCameraPermission();
        }

        fotoapparat = createFotoapparat();
        gridLines = findViewById(R.id.grid_lines);
        gridLines.setNumColumns(3);
        gridLines.setNumRows(3);
        cameraView.setVisibility(View.VISIBLE);
        takePictureOnClick();
        galleryOnClick();
        toggleTorchOnSwitch();   // control flash light
        switchCameraOnClick();   // control front or back camera

    }

    @Override
    public void onStart() {
        super.onStart();
        if (hasCameraPermission) {
            fotoapparat.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (hasCameraPermission) {
            fotoapparat.stop();
        }
    }


    private Fotoapparat createFotoapparat() {
        return Fotoapparat
                .with(this)
                .into(cameraView)
                .focusView(focusview)
                .previewScaleType(ScaleType.CenterCrop)
                .lensPosition(back())
                .frameProcessor(new SampleFrameProcessor())
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .cameraErrorCallback(new CameraErrorListener() {
                    @Override
                    public void onError(@NotNull CameraException e) {
                        Toast.makeText(TakePhotoActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            hasCameraPermission = true;
            fotoapparat.start();
            cameraView.setVisibility(View.VISIBLE);
        }
    }




    private class SampleFrameProcessor implements FrameProcessor {
        @Override
        public void process(@NotNull Frame frame) {
            // Perform frame processing, if needed
        }
    }

    private void toggleTorchOnSwitch() {
        torchSwitch = findViewById(R.id.flash);

        torchSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (flash_on) {
                    torchSwitch.setImageResource(R.drawable.flash_off);
                    flash_on = false;
                } else {
                    torchSwitch.setImageResource(R.drawable.flash_on);
                    flash_on = true;
                }

                fotoapparat.updateConfiguration(
                        UpdateConfiguration.builder()
                                .flash(
                                        flash_on ? on() : off()
                                )
                                .build()
                );

            }
        });
    }

    private void takePictureOnClick() {
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takePicture();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void switchCameraOnClick() {
        switchcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activeCameraBack = !activeCameraBack;
                fotoapparat.switchTo(
                        activeCameraBack ? back() : front(),
                        cameraConfiguration
                );

            }
        });
    }

    private void galleryOnClick() {
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TakePhotoActivity.this, ShareActivity.class);
                startActivity(intent);
            }
        });
    }

    private void backToMain() {
        findViewById(R.id.back_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TakePhotoActivity.this, MainPage.class);
                startActivity(intent);
            }
        });
    }

    private void takePicture() throws ExecutionException, InterruptedException {
        PhotoResult photoResult = fotoapparat.takePicture();


        Toast.makeText(TakePhotoActivity.this, "Photo Captured", Toast.LENGTH_SHORT).show();
        //passToFilter();

        photoResult
                .toBitmap()
                .whenDone(new WhenDoneListener<BitmapPhoto>() {
                    @Override
                    public void whenDone(@Nullable BitmapPhoto bitmapPhoto) {
                        if (bitmapPhoto == null) {
                            Log.e(LOGGING_TAG, "Couldn't capture photo.");
                            return;
                        }
                        passToPreview(bitmapPhoto.bitmap, bitmapPhoto.rotationDegrees);

                    }
                });





    }

    private void passToPreview(Bitmap bmp, int rotate) {


        PhotoPreviewFragment nextFrag= new PhotoPreviewFragment();
        Log.d(LOGGING_TAG, "Pass image to cache... Rotation:"+rotate);
        CommResources.photoFinishBitmap = bmp;
        CommResources.rotationdegree = rotate;
        this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.filter_container, nextFrag,"Start Filter Fragment")
                .addToBackStack(null)
                .commit();

    }




}
