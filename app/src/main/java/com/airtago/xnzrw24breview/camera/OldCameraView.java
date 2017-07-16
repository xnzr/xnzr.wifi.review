package com.airtago.xnzrw24breview.camera;

import android.hardware.Camera;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.airtago.xnzrw24breview.R;

/**
 * Created by alexe on 16.07.2017.
 */

public class OldCameraView implements FragmentCallback {
    public OldCameraView(android.widget.FrameLayout layout) {
        mLayout = layout;
    }

    private Camera camera;
    private OldCameraPreview cameraPreview;
    private android.widget.FrameLayout mLayout;

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e) {
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onPause() {
        mLayout.removeView(cameraPreview);
        cameraPreview.setCamera(null);
        cameraPreview = null;
        if (camera != null)
            camera.release();
        camera = null;
    }

    @Override
    public void onResume(FrameLayout layout) {
        if (camera == null) {
            camera = getCameraInstance();
            if (cameraPreview == null) {
                cameraPreview = new OldCameraPreview(mLayout.getContext(), camera);
                mLayout.addView(cameraPreview);
            }
        }
    }
}
