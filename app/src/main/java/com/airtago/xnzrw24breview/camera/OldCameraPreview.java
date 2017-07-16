package com.airtago.xnzrw24breview.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by alexe on 16.07.2017.
 */

public class OldCameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    public OldCameraPreview(Context context, android.hardware.Camera androidCamera) {
        super(context);

        setCamera(androidCamera);
    }

    private android.hardware.Camera mCamera;

    public void setCamera(android.hardware.Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            getHolder().removeCallback(this);
            getHolder().addCallback(this);
        } else {
            getHolder().removeCallback(this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (mCamera == null)
            return;

        mCamera.stopPreview();

        setCameraDisplayOrientation();

        surfaceCreated(holder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null)
            return;

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void setCameraDisplayOrientation() {
        if (mCamera == null)
            return;

        // определяем насколько повернут экран от нормального положения
        int rotation = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation)
        {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        }
        else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // передняя камера
            result = ((360 - degrees) - info.orientation);
            result += 360;
        }
        result = result % 360;
        try
        {
            mCamera.setDisplayOrientation(result);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
