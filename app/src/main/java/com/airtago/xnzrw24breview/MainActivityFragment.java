package com.airtago.xnzrw24breview;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.airtago.xnzrw24breview.camera.FragmentCallback;
import com.airtago.xnzrw24breview.camera.OldCameraView;
import com.airtago.xnzrw24breview.data.WiFiPacket;
import com.airtago.xnzrw24breview.usb.DeviceDriver;
import com.airtago.xnzrw24breview.usb.DeviceDriverWatcher;
import com.airtago.xnzrw24breview.usb.DeviceNotFoundException;
import com.airtago.xnzrw24breview.usb.DeviceOpenFailedException;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final String TAG = MainActivityFragment.class.getSimpleName();
    private final String ANALYZER_KEY = NetworkAnalyzer.class.getSimpleName();

    private DeviceDriver mDriver;
    private Thread mReadingThread;
    private NetworkAnalyzer mAnalyzer;
    private Handler mHandler;

    private RadioGroup mChannelsGroup;
    private Snackbar mSnack = null;

    private FragmentCallback mCameraCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAnalyzer = new NetworkAnalyzer(new Handler() {
            public void handleMessage(Message msg) {
                if (msg.getData().containsKey("focused")) {
                    ArrayList<String> focused = msg.getData().getStringArrayList("focused");
                    String nets = "";
                    for (String net: focused) {
                        if (nets.length() > 0)
                            nets += ", ";
                        nets += net;
                    }
                    if (mSnack == null) {
                        mSnack = Snackbar.make(getView(), "", Toast.LENGTH_SHORT);
                    }
                    if (!mSnack.isShown()) {
                        mSnack.setText("Networks in focus: " + nets);
                        mSnack.show();
                    }
                }
            }
        });

        initDriver();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("channel")) {
                mDriver.changeChannel(savedInstanceState.getInt("channel", 1));
                if (mChannelsGroup != null) {
                    mChannelsGroup.getChildAt(savedInstanceState.getInt("channel", 1) - 1).callOnClick();
                }
            }
            if (savedInstanceState.containsKey(ANALYZER_KEY)) {
                mAnalyzer.loadState(savedInstanceState.getBundle(ANALYZER_KEY));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("channel", mDriver.getCurrentChannel());
        //Saving found networks
        Bundle analyzerBundle = new Bundle();
        mAnalyzer.saveState(analyzerBundle);
        outState.putBundle(ANALYZER_KEY, analyzerBundle);
    }

    @Override
    public void onStart() {
        super.onStart();

        mHandler = new Handler();

        initDriver();

        if (mCameraCallback != null) {
            mCameraCallback.onStart();
        }

        mAnalyzer.setThreshold(
                PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("THRESHOLD_VALUE", 10)
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDriver != null) {
            try {
                mDriver.init();
            } catch (DeviceNotFoundException e) {
                e.printStackTrace();
            } catch (DeviceOpenFailedException e) {
                e.printStackTrace();
            }
        }

        if (mCameraCallback != null) {
            mCameraCallback.onResume((FrameLayout)getView().findViewById(R.id.cameraLayout));
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mDriver != null) {
            mDriver.close();
        }
        mSnack = null;

        if (mCameraCallback != null) {
            mCameraCallback.onStop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCameraCallback != null) {
            mCameraCallback.onPause();
        }
    }

    @Override
    public void onDetach() {
        if (mDriver != null) {
            mDriver.close();
            mDriver = null;
        }
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        CompoundButton.OnCheckedChangeListener channelButtonListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mAnalyzer.clear();
                if (isChecked && mDriver != null) {
                    mDriver.changeChannel(Integer.parseInt(buttonView.getTag().toString()));
                }
            }
        };
        ((RadioButton)fragmentView.findViewById(R.id.radioButton1)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton2)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton3)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton4)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton5)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton6)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton7)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton8)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton9)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton10)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton11)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton12)).setOnCheckedChangeListener(channelButtonListener);
        ((RadioButton)fragmentView.findViewById(R.id.radioButton13)).setOnCheckedChangeListener(channelButtonListener);
        mChannelsGroup = (RadioGroup)fragmentView.findViewById(R.id.channelsGroup);
        if (mDriver != null) {
            mChannelsGroup.getChildAt(mDriver.getCurrentChannel() - 1).callOnClick();
        }
        boolean enabled = mReadingThread != null && mReadingThread.isAlive();
        for (int i = 0; i < mChannelsGroup.getChildCount(); ++i) {
            mChannelsGroup.getChildAt(i).setEnabled(enabled);
        }

        FrameLayout cameraLayout = (FrameLayout)fragmentView.findViewById(R.id.cameraLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //mCameraCallback = new CameraView(getContext(), (Fragment)this, cameraLayout);
        } else {
            mCameraCallback = new OldCameraView(cameraLayout);
        }

        return fragmentView;
    }

    private void initDriver() {
        if (mDriver == null) {
            mDriver = new DeviceDriver(getContext(), true, new DeviceDriverWatcher() {
                @Override
                public void onDeviceStart() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < mChannelsGroup.getChildCount(); ++i) {
                                mChannelsGroup.getChildAt(i).setEnabled(true);
                            }
                        }
                    });
                    mReadingThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            threadLoop();
                        }
                    });
                    mReadingThread.start();
                }

                @Override
                public void onDeviceStop() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < mChannelsGroup.getChildCount(); ++i) {
                                mChannelsGroup.getChildAt(i).setEnabled(false);
                            }
                        }
                    });
                    if (mReadingThread != null) {
                        mReadingThread.interrupt();
                        mReadingThread = null;
                    }
                }
            });
        }
    }

    private void threadLoop() {
        //mDriver.useOldProtocol();
        while (!Thread.currentThread().isInterrupted()) {
            if (mDriver.tryReadPacket()) {
                mAnalyzer.analyze(mDriver.getPackets());
            }
        }
    }
}
