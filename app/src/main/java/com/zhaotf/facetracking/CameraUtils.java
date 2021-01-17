package com.zhaotf.facetracking;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.io.IOException;


public class CameraUtils implements Camera.PreviewCallback {
    private static final String TAG = "CameraUtils";
    private SurfaceView mSurfaceView;
    private FaceRectView mFaceRectView;
    @SuppressLint("StaticFieldLeak")
    private static CameraUtils mCameraUtils;
    private SurfaceHolder mSurfaceViewHolder;
    private int cameraPosition = 0;
    private Camera mCamera;

    public static CameraUtils getInstance() {
        if (mCameraUtils == null) {
            synchronized (CameraUtils.class) {
                if (mCameraUtils == null) {
                    mCameraUtils = new CameraUtils();
                }
            }
        }
        return mCameraUtils;
    }

    public void initCamera(SurfaceView surfaceView, FaceRectView faceRectView) {
        this.mSurfaceView = surfaceView;
        this.mFaceRectView = faceRectView;
        mSurfaceViewHolder = mSurfaceView.getHolder();
        mSurfaceViewHolder.setFormat(PixelFormat.OPAQUE);
    }

    public void startCamera() {
        if (mSurfaceViewHolder != null) {
            mSurfaceViewHolder.addCallback(new SurfaceHolderCB());
        }
    }

    public void stopCamera() {
        stopPreview();
    }

    private void startPreview() {
        if (mCamera == null) {
            mCamera = Camera.open(cameraPosition);
            Camera.Parameters parameters = setParameters(mCamera, cameraPosition);
            mCamera.setDisplayOrientation(90);

            try {
                if (cameraPosition == 0) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //后置必须聚焦设置
                }
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(mSurfaceViewHolder);
                if (mCamera != null) {
                    mCamera.setPreviewCallback(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mCamera != null) {
                mCamera.startPreview();
                mCamera.cancelAutoFocus();//聚焦
                mCamera.startFaceDetection();
                mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                    @Override
                    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                        if (faces.length > 0) {
                            int score = faces[0].score;
                            Log.i(TAG, "onFaceDetection: score " + score);
                            mFaceRectView.drawFaceRect(faces, mSurfaceView, cameraPosition);
                        } else {
                            mFaceRectView.clearRect();
                        }
                    }
                });
            }
        }
    }

    /**
     * 切换前后相机
     */
    public void changeCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        if (numberOfCameras >= 2) {
            if (cameraPosition == 0) { //现在为后置，变成为前置
                Camera.getCameraInfo(1, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { //CAMERA_FACING_FRONT 前置方位  CAMERA_FACING_BACK 后置方位
                    if (mCamera != null) {
                        stopPreview();
                    }
                    cameraPosition = 1;
                    startPreview();
                }
            } else if (cameraPosition == 1) {//前置更改为后置相机
                Camera.getCameraInfo(0, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    if (mCamera != null) {
                        stopPreview();
                    }
                    cameraPosition = 0;
                    startPreview();
                }
            }
        }
    }


    private class SurfaceHolderCB implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            startPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopPreview();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    }

    private void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 设置相机参数
     *
     * @param camera
     * @param cameraPosition
     * @return
     */
    public Camera.Parameters setParameters(Camera camera, int cameraPosition) {
        Camera.Parameters parameters = null;
        if (camera != null) {
            parameters = camera.getParameters();
            if (cameraPosition == 0) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                parameters.setPreviewFpsRange(0, 25);
            }
            parameters.setPictureFormat(ImageFormat.NV21);
            int CAMERA_WIDTH = 640;
            int CAMERA_HEIGHT = 480;
            parameters.setPictureSize(CAMERA_WIDTH, CAMERA_HEIGHT);
            parameters.setPreviewSize(CAMERA_WIDTH, CAMERA_HEIGHT);
        }
        return parameters;
    }
}
