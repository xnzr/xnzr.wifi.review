package com.airtago.xnzrw24breview;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.airtago.xnzrw24breview.usb.DeviceDriver;
import com.airtago.xnzrw24breview.usb.DeviceNotFoundException;
import com.airtago.xnzrw24breview.usb.DeviceOpenFailedException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements View.OnClickListener {

    private DeviceDriver _driver;

    public MainActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();

        if (_driver == null) {
            _driver = new DeviceDriver(getContext(), true);
        }
        if (_driver != null) {
            try {
                _driver.init();
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

        if (_driver != null) {
            _driver.close();
        }
    }

    @Override
    public void onDetach() {
        if (_driver != null) {
            _driver.close();
            _driver = null;
        }
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        fragmentView.findViewById(R.id.button1).setOnClickListener(this);
        fragmentView.findViewById(R.id.button2).setOnClickListener(this);
        fragmentView.findViewById(R.id.button3).setOnClickListener(this);
        fragmentView.findViewById(R.id.button4).setOnClickListener(this);
        fragmentView.findViewById(R.id.button5).setOnClickListener(this);
        fragmentView.findViewById(R.id.button6).setOnClickListener(this);
        fragmentView.findViewById(R.id.button7).setOnClickListener(this);
        fragmentView.findViewById(R.id.button8).setOnClickListener(this);
        fragmentView.findViewById(R.id.button9).setOnClickListener(this);
        fragmentView.findViewById(R.id.button10).setOnClickListener(this);
        fragmentView.findViewById(R.id.button11).setOnClickListener(this);
        fragmentView.findViewById(R.id.button12).setOnClickListener(this);
        fragmentView.findViewById(R.id.button13).setOnClickListener(this);
        return fragmentView;
    }

    @Override
    public void onClick(View v) {
        if (_driver != null) {
            _driver.changeChannel(Integer.parseInt(v.getTag().toString()));
        }
    }
}
