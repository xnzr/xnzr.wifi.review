package com.airtago.xnzrw24breview.data;

/**
 * Created by alexe on 04.07.2017.
 */

public class WiFiPacket {
    private int mAntenna = 0;
    private int mChannel = 0;
    private double mPower = -100.0;
    private String mAccessPoint = "";
    private String mMAC = "";
    private long mTime = 0;
    private String mRaw = "";

    public WiFiPacket(String data) throws WFParseException {
        String delimiters = "[ ]+";
        String[] tokens = data.split(delimiters);

        if ( tokens.length < 6 ) {
            throw new WFParseException("Have " + tokens.length + " tokens in string '" + data + "'");
        }

        try {
            mAntenna = Integer.parseInt(tokens[0], 10) - 1;
            mChannel = Integer.parseInt(tokens[1], 10);
            mMAC = tokens[2];
            mTime = Long.parseLong(tokens[3], 16);
            mPower = Double.parseDouble(tokens[4]);

            for (int i = 5; i < tokens.length; i++) {
                mAccessPoint += tokens[i];
                if (i != tokens.length - 1) {
                    mAccessPoint += " ";
                }
            }

            mRaw = data;
        } catch (Exception ex) {
            throw new WFParseException("Parce exception in string '" + data + "'", ex);
        }
    }

    public String toString() {
        return String.format("%16s %2d   %1d %5.1f", mAccessPoint, mChannel, mAntenna, mPower);
    }

    public int getAntenna() { return mAntenna; }
    public int getChannel() { return mChannel; }
    public double getPower() { return mPower; }
    public String getAccessPoint() { return mAccessPoint; }
    public String getMAC() { return mMAC; }
    public long getTime() { return mTime; }
    public String getRaw() {return mRaw; }
}
