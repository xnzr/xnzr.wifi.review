package com.airtago.xnzrw24breview;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.ToggleButton;

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
                    Snackbar.make(getView(), "Networks in focus:\n" + nets, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        if (savedInstanceState.containsKey(ANALYZER_KEY)) {
            mAnalyzer.loadState(savedInstanceState.getBundle(ANALYZER_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Saving found networks
        Bundle analyzerBundle = new Bundle();
        mAnalyzer.saveState(analyzerBundle);
        outState.putBundle(ANALYZER_KEY, analyzerBundle);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mDriver == null) {
            mDriver = new DeviceDriver(getContext(), true, new DeviceDriverWatcher() {
                @Override
                public void onDeviceStart() {
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
                    mReadingThread.interrupt();
                    mReadingThread = null;
                }
            });
        }
        if (mDriver != null) {
            try {
                mDriver.init();
            } catch (DeviceNotFoundException e) {
                e.printStackTrace();
            } catch (DeviceOpenFailedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mDriver != null) {
            mDriver.close();
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

        return fragmentView;
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
