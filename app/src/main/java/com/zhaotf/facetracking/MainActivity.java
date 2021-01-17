package com.zhaotf.facetracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SurfaceView mShowPreview;
    private FaceRectView mFaceRectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        checkPermission();
        initData();
    }

    private void initView() {
        mShowPreview = findViewById(R.id.showPreview);
        mFaceRectView = findViewById(R.id.FaceRect);
        Button mTurnCamera = findViewById(R.id.turnCamera);

        mTurnCamera.setOnClickListener(this);

    }

    private void initData() {
        CameraUtils.getInstance().initCamera(mShowPreview, mFaceRectView);
        CameraUtils.getInstance().startCamera();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.turnCamera) {
            CameraUtils.getInstance().changeCamera();
        }
    }

    private void checkPermission() {
        try {
            if (Integer.parseInt(Build.VERSION.SDK) < 23) {
                return;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        PackageManager pm = getPackageManager();
        boolean permission_writeStorage = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "packageName"));
        boolean permission_camera = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.CAMERA", "packageName"));

        boolean permission_record = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.RECORD_AUDIO", "packageName"));

        if (!(permission_writeStorage && permission_camera && permission_record)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
            }, 0x01);
        }
    }

}
