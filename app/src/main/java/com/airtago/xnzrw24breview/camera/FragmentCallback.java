package com.airtago.xnzrw24breview.camera;

import android.widget.FrameLayout;

/**
 * Created by alexe on 16.07.2017.
 */

public interface FragmentCallback {
    void onStart();
    void onStop();
    void onPause();
    void onResume(FrameLayout layout);
}
