package com.airtago.xnzrw24breview.usb;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by alexe on 04.07.2017.
 */

final class UsbDataReader {
    private final String TAG = UsbDataReader.class.getSimpleName();

    private static final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;
    private static final int DEFAULT_WRITE_BUFFER_SIZE = 1024;
    private byte[] mReadBuffer;
    private byte[] mWriteBuffer;
    private final Object readBufferLock = new Object();
    private final Object writeBufferLock = new Object();

    private UsbDeviceConnection mConnection = null;
    private UsbEndpoint mReadEndpoint;
    private UsbEndpoint mWriteEndpoint;
    private boolean mEnableAsyncReads = false;

    public UsbDataReader(UsbDeviceConnection connection, UsbEndpoint readEndpoint, UsbEndpoint writeEndpoint) {
        mConnection = connection;
        mReadEndpoint = readEndpoint;
        mWriteEndpoint = writeEndpoint;

        mEnableAsyncReads = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1);
        mReadBuffer = new byte[DEFAULT_READ_BUFFER_SIZE];
        mWriteBuffer = new byte[DEFAULT_WRITE_BUFFER_SIZE];

        if (mEnableAsyncReads) {
            Log.d(TAG, "Async reads enabled");
        } else {
            Log.d(TAG, "Async reads disabled.");
        }
    }

    public int write(byte oneChar, int timeoutMillis) throws IOException {
        Log.d( TAG, "write( " + oneChar + " )" );
        byte[] buf = new byte[1];
        buf[0] = oneChar;
        return write(buf, timeoutMillis);
    }

    public int write(byte[] src, int timeoutMillis) throws IOException {
        int offset = 0;

        while (offset < src.length) {
            final int writeLength;
            final int amtWritten;

            synchronized (writeBufferLock) {
                final byte[] buffer;

                writeLength = Math.min(src.length - offset, mWriteBuffer.length);
                if (offset == 0) {
                    buffer = src;
                } else {
                    // bulkTransfer does not support offsets, make a copy.
                    System.arraycopy(src, offset, mWriteBuffer, 0, writeLength);
                    buffer = mWriteBuffer;
                }

                amtWritten = mConnection.bulkTransfer(mWriteEndpoint, buffer, writeLength,
                        timeoutMillis);
            }
            if (amtWritten <= 0) {
                throw new IOException("Error writing " + writeLength
                        + " bytes at offset " + offset + " length=" + src.length);
            }

            Log.d(TAG, "Wrote amt=" + amtWritten + " attempted=" + writeLength);
            offset += amtWritten;
        }
        return offset;
    }

    public int read(byte[] dest, int timeoutMillis) throws IOException {
        if (mEnableAsyncReads) {
            final UsbRequest request = new UsbRequest();
            try {
                request.initialize(mConnection, mReadEndpoint);
                final ByteBuffer buf = ByteBuffer.wrap(dest);
                if (!request.queue(buf, dest.length)) {
                    throw new IOException("Error queueing request.");
                }

                final UsbRequest response = mConnection.requestWait();
                if (response == null) {
                    throw new IOException("Null response");
                }

                final int nread = buf.position();
                if (nread > 0) {
                    //Log.d(TAG, HexDump.dumpHexString(dest, 0, Math.min(32, dest.length)));
                    return nread;
                } else {
                    return 0;
                }
            } finally {
                request.close();
            }
        }

        final int numBytesRead;
        synchronized (readBufferLock) {
            int readAmt = Math.min(dest.length, mReadBuffer.length);
            numBytesRead = mConnection.bulkTransfer(mReadEndpoint, mReadBuffer, readAmt,
                    timeoutMillis);
            if (numBytesRead < 0) {
                // This sucks: we get -1 on timeout, not 0 as preferred.
                // We *should* use UsbRequest, except it has a bug/api oversight
                // where there is no way to determine the number of bytes read
                // in response :\ -- http://b.android.com/28023
                if (timeoutMillis == Integer.MAX_VALUE) {
                    // Hack: Special case "~infinite timeout" as an error.
                    return -1;
                }
                return 0;
            }
            System.arraycopy(mReadBuffer, 0, dest, 0, numBytesRead);
        }
        return numBytesRead;
    }
}
