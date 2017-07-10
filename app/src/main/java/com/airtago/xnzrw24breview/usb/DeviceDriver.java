package com.airtago.xnzrw24breview.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.airtago.xnzrw24breview.data.WiFiPacket;
import com.airtago.xnzrw24breview.data.WiFiPacketCreator;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by alexe on 04.07.2017.
 */

public final class DeviceDriver {
    private final String TAG = DeviceDriver.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.airtago.xnzrw24b.review.USB_PERMISSION";
    private final int READ_TIMEOUT_MS  = 500;
    private final int READ_BUF_SIZE    = 30 * 16;

    private Context mContext;
    private UsbManager mUsbManager;
    private UsbDevice mDevice = null;
    private UsbDeviceConnection mConnection = null;
    private UsbInterface mUsbInterface = null;
    private UsbDataReader mDataReader = null;
    private boolean mInitWithOldProtocol = false;

    private WiFiPacketCreator mPacketCreator = null;
    private ArrayList<WiFiPacket> mPackets;

    public DeviceDriver(Context context) {
        mContext = context;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        //filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbReceiver, filter);
    }

    public DeviceDriver(Context context, boolean useOldProtocol) {
        this(context);
        mInitWithOldProtocol = useOldProtocol;
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.d(TAG, "Device was detached");
                    close();
                }
            }
            else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.d(TAG, "Device was attached");
                    try {
                        init();
                    } catch (DeviceNotFoundException e) {
                        e.printStackTrace();
                    } catch (DeviceOpenFailedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private void findDevice() throws DeviceNotFoundException {
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            final int vendorId  = usbDevice.getVendorId();
            final int productId = usbDevice.getProductId();
            Log.d(TAG, String.format("DEVICE: 0x%04X 0x%04X", vendorId, productId));

            // ble
            if ( vendorId == 0x0451 && productId == 0x16C5 ) {
                mDevice = usbDevice;
                return;
            }

            // wifi
            if ( vendorId == 0x04B4 && productId == 0x0005 ) {
                mDevice = usbDevice;
                return;
            }

            // wifi STM
            if ( vendorId == 0x2341 && productId == 0x0043 ) {
                mDevice = usbDevice;
                return;
            }

            // wifi STM-2 (??? new one)
            if ( vendorId == 0x0483 && productId == 0x5740 ) {
                mDevice = usbDevice;
                return;
            }

            // fx3
            if ( vendorId == 0x04B4 && productId == 0x00f1 ) {
                mDevice = usbDevice;
                return;
            }

        }
        mDevice = null;
        throw new DeviceNotFoundException();
    }

    private void askPermissions() {
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mUsbManager.requestPermission(mDevice, mPermissionIntent);
    }

    private void open() throws DeviceNotFoundException, DeviceOpenFailedException {
        mPacketCreator = new WiFiPacketCreator();
        mPackets = new ArrayList<>();
        if (mDevice == null) {
            findDevice();
        }

        mConnection = mUsbManager.openDevice(mDevice);
        if (mConnection == null) {
            askPermissions();
            throw new DeviceOpenFailedException();
        }

        Log.d(TAG, String.format("Ifce count = %d", mDevice.getInterfaceCount()));
        for ( int i = 0; i < mDevice.getInterfaceCount(); ++i ) {
            UsbInterface ifce = mDevice.getInterface(i);

            Log.d(TAG, String.format("   Ifce[%d]: class %d",
                    i, ifce.getInterfaceClass()
            ));

            for ( int k = 0; k < ifce.getEndpointCount(); ++k ) {
                Log.d(TAG, String.format("       EP[%d] dir %d", k, ifce.getEndpoint(k).getDirection()));
            }
        }

        int ifce = 1;
        int rep = 1;
        int wep = 0;
        Log.d(TAG, "Claiming data interface " + String.format( "ifce=%d", ifce ));
        mUsbInterface = mDevice.getInterface(ifce);
        Log.d(TAG, "data iface=" + mUsbInterface);


        if (!mConnection.claimInterface(mUsbInterface, true)) {
            Log.e(TAG, "claimIfce error");
            throw new DeviceOpenFailedException();
        } else {
            Log.d(TAG, "claimIfce OK");
        }
        Log.d(TAG, "readEndpoint " + String.format( "ep=%d", rep ));
        UsbEndpoint readEndpoint = mUsbInterface.getEndpoint(rep);
        Log.d(TAG, "Read endpoint direction: " + readEndpoint.getDirection());
        // Should be UsbConstants.USB_DIR_IN = 0x80 (128 decimal)

        Log.d(TAG, "writeEndpoint " + String.format( "ep=%d", wep ));
        UsbEndpoint writeEndpoint = mUsbInterface.getEndpoint(wep);
        Log.d(TAG, "Write endpoint direction: " + writeEndpoint.getDirection());

        mDataReader = new UsbDataReader(mConnection, readEndpoint, writeEndpoint);
    }

    public void init() throws DeviceNotFoundException, DeviceOpenFailedException {
        findDevice();
        open();
    }

    public void close() {
        if (mDataReader != null) {
            mDataReader = null;
        }
        if (mConnection != null) {
            mConnection.releaseInterface(mUsbInterface);
            mConnection.close();
        }
    }

    public boolean useOldProtocol() {
        if (mDataReader != null) {
            String cmd = "$2122239";
            byte[] cmdBytes = new byte[cmd.length()];
            for (int i = 0; i < cmd.length(); ++i) {
                cmdBytes[i] = (byte) cmd.charAt(i);
            }
            try {
                mDataReader.write(cmdBytes, READ_TIMEOUT_MS);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private byte getChannelChar(int channel) {
        if (channel < 10) {
            return (byte)(48 + channel);
        } else {
            return (byte)(65 + channel - 10);
        }
    }

    public void changeChannel(int chan) {
        Log.d(TAG, "changeChannel " + chan);
        if (mDataReader != null ) {
            try {
                mDataReader.write(getChannelChar(chan), READ_TIMEOUT_MS);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public boolean tryReadPacket() {
        byte[] buf = new byte[READ_BUF_SIZE];
        int read = 0;
        try {
            read = mDataReader.read(buf, READ_TIMEOUT_MS);
            Log.d(TAG, "Read " + buf);

            mPacketCreator.putData(buf, read);
            ArrayList<WiFiPacket> packets = mPacketCreator.getPackets();
            Log.d(TAG, "Have " + packets.size() + " wifi packets" );

            if ( packets.size() > 0 ) {
                mPackets.addAll(packets);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
            return false;
        }
        return false;
    }

    public ArrayList<WiFiPacket> getPackets() {
        ArrayList<WiFiPacket> result = mPackets;
        mPackets = new ArrayList<>();
        return result;
    }
}
