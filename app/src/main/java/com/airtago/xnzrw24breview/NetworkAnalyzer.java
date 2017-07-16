package com.airtago.xnzrw24breview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.airtago.xnzrw24breview.data.WiFiPacket;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by alexe on 14.07.2017.
 */

public final class NetworkAnalyzer {
    private final String TAG = MainActivityFragment.class.getSimpleName();

    private Handler mHandler;
    private ArrayList<Network> mNetworks = new ArrayList<>();
    private double mThreshold;

    public NetworkAnalyzer(Handler handler) {
        mHandler = handler;
    }

    public void setThreshold(double value) {
        mThreshold = value;
    }

    public void clear() {
        mNetworks.clear();
    }

    public void analyze(ArrayList<WiFiPacket> packets) {
        for (WiFiPacket packet: packets) {
            boolean hasNetwork = false;
            for (int i = 0; i < mNetworks.size(); ++i) {
                Network net = mNetworks.get(i);
                if (net.Ssid.equals(packet.getAccessPoint()) && net.Mac.equals(packet.getMAC())) {
                    if (packet.getAntenna() == 0)
                        net.addRssi1(packet.getPower());
                    else
                        net.addRssi2(packet.getPower());
                    hasNetwork = true;
                    break;
                }
            }

            //no network for current packet, adding new one
            if (!hasNetwork) {
                mNetworks.add(new Network(packet.getAccessPoint(), packet.getMAC()));
            }
        }

        //Проверяем сети, которые "в фокусе"
        ArrayList<String> focused = new ArrayList<>();
        for (Network network: mNetworks) {
            if (network.hasData()) {
                if (network.getDiff() <= mThreshold) {
                    focused.add(network.Ssid);
                }
            }
        }
        if (!focused.isEmpty()) {
            Message message = mHandler.obtainMessage();
            message.getData().putStringArrayList("focused", focused);
            mHandler.sendMessage(message);
        }
    }

    public void saveState(Bundle state) {
        state.putInt("count", mNetworks.size());
        state.putDouble("threshold", mThreshold);
        for (int i = 0; i < mNetworks.size(); ++i) {
            Bundle netBundle = new Bundle();
            mNetworks.get(i).serialize(netBundle);
            state.putBundle(Integer.toString(i), netBundle);
        }
    }

    public void loadState(Bundle state) {
        mThreshold = state.getDouble("threshold", 10);
        mNetworks.clear();
        for (int i = 0; i < state.getInt("count", 0); ++i) {
            Bundle netBundle = state.getBundle(Integer.toString(i));
            Network network = new Network(netBundle);
            mNetworks.add(network);
        }
    }

    private final class Network {
        public String Ssid;
        public String Mac;

        public Network(Bundle bundle) {
            deserialize(bundle);
        }

        public Network(String ssid, String mac) {
            Ssid = ssid;
            Mac = mac;
        }

        private ArrayList<Double> mHistory1 = new ArrayList<>();
        private ArrayList<Double> mHistory2 = new ArrayList<>();

        private final int MAX_HISTORY = 20;

        public void addRssi1(double rssi) {
            mHistory1.add(rssi);
            if (mHistory1.size() > MAX_HISTORY)
                mHistory1.remove(0);
        }

        public void addRssi2(double rssi) {
            mHistory2.add(rssi);
            if (mHistory2.size() > MAX_HISTORY)
                mHistory2.remove(0);
        }

        public double getAvgRssi1() {
            double r = 0;
            for (double d: mHistory1) {
                r += d;
            }
            return mHistory1.size() > 0 ? r / mHistory1.size() : -255d;
        }

        public double getAvgRssi2() {
            double r = 0;
            for (double d: mHistory2) {
                r += d;
            }
            return mHistory2.size() > 0 ? r / mHistory2.size() : -0d;
        }

        public double getDiff() {
//            double r1 = getAvgRssi1();
//            double r2 = getAvgRssi2();
//            double r = Math.pow(10.0, ((r2 - r1) * 0.1 + 2.5) * 1);
//            Log.d(TAG, "ssid=" + Ssid + " DIFF=" + r);
//            return r;
            return Math.pow(10.0, ((double)(getAvgRssi2() - getAvgRssi1()) * 0.1 + 2.5) * 1);
        }

        public boolean hasData() {
            return  mHistory1.size() > 0 && mHistory2.size() > 0;
        }

        private double[] convertDoubles(ArrayList<Double> doubles) {
            double[] ret = new double[doubles.size()];
            Iterator<Double> iterator = doubles.iterator();
            int i = 0;
            while(iterator.hasNext()) {
                ret[i++] = iterator.next();
            }
            return ret;
        }

        private void convertToDoubles(ArrayList<Double> list, double[] values) {
            list.clear();
            for (int i = 0; i < values.length; ++i) {
                list.add(values[i]);
            }
        }

        public void serialize(Bundle bundle) {
            bundle.putString("ssid", Ssid);
            bundle.putString("mac", Mac);
            bundle.putDoubleArray("h1", convertDoubles(mHistory1));
            bundle.putDoubleArray("h2", convertDoubles(mHistory2));
        }

        public void deserialize(Bundle bundle) {
            Ssid = bundle.getString("ssid");
            Mac = bundle.getString("mac");
            if (bundle.containsKey("h1"))
                convertToDoubles(mHistory1, bundle.getDoubleArray("h1"));
            if (bundle.containsKey("h2"))
                convertToDoubles(mHistory2, bundle.getDoubleArray("h2"));
        }
    }
}
