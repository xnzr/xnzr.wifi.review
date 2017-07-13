package com.airtago.xnzrw24breview;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.airtago.xnzrw24breview.usb.DeviceDriver;
import com.airtago.xnzrw24breview.usb.DeviceDriverWatcher;
import com.airtago.xnzrw24breview.usb.DeviceNotFoundException;
import com.airtago.xnzrw24breview.usb.DeviceOpenFailedException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final String TAG = MainActivityFragment.class.getSimpleName();

    private DeviceDriver mDriver;
    private Thread mReadingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            threadLoop();
        }
    });
    private Handler mHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
//                if (msg.what == MessageFields.CODE_DATA) {
//                    Log.d(TAG, "ant=" + msg.getData().getInt(MessageFields.FIELD_ANT_INT) + " ch=" + msg.getData().getInt(MessageFields.FIELD_CH_INT) + " ssid=" +msg.getData().getString(MessageFields.FIELD_SSID_STR) + " mac=" + msg.getData().getString(MessageFields.FIELD_MAC_STR) + " rssi=" + msg.getData().getDouble(MessageFields.FIELD_RSSI_DOUBLE));
//                    try {
//                        WFPacket packet = new WFPacket(msg.getData().getString(MessageFields.FIELD_RAW_STR));
//                        networksFragment.addInfo(packet);
//                        if (mSelectedNetwork != null && mSelectedNetwork.Ssid.equals(packet.apName) && mSelectedNetwork.Mac.equals(packet.mac)) {
//                            mChannelsFragment.addInfo(packet);
//
//                            if (mLevelCalculator != null) {
//                                mLevelCalculator.handleInfo(packet);
//                                mCameraFragmentInterface.setLevel(mLevelCalculator.getAvg());
//                            }
//                        }
//                    } catch (WFParseException e) {
//                        e.printStackTrace();
//                    }
//                }
            };
        };
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mDriver == null) {
            mDriver = new DeviceDriver(getContext(), true, new DeviceDriverWatcher() {
                @Override
                public void onDeviceStart() {
                    mReadingThread.start();
                }

                @Override
                public void onDeviceStop() {
                    mReadingThread.interrupt();
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

        View.OnClickListener channelButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ToggleButton)v).toggle();
                if (mDriver != null) {
                    mDriver.changeChannel(Integer.parseInt(v.getTag().toString()));
                }
            }
        };
        //((ToggleButton)fragmentView.findViewById(R.id.button1)).setOnCheckedChangeListener();
        fragmentView.findViewById(R.id.button1).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button2).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button3).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button4).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button5).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button6).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button7).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button8).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button9).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button10).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button11).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button12).setOnClickListener(channelButtonListener);
        fragmentView.findViewById(R.id.button13).setOnClickListener(channelButtonListener);

        fragmentView.findViewById(R.id.settingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent preferencesIntent = new Intent(getActivity(), SettingsActivity.class);
                getActivity().startActivity(preferencesIntent);
            }
        });

        return fragmentView;
    }

    private void threadLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            if (mDriver.tryReadPacket()) {
                mDriver.getPackets();
            }
        }
    }
}
