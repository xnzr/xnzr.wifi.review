package com.airtago.xnzrw24breview.data;

import java.util.ArrayList;

/**
 * Created by alexe on 04.07.2017.
 */

public class WiFiPacketCreator {
    private static final String TAG = WiFiPacketCreator.class.getSimpleName();

    private String buffer = "";

    public void putData(byte[] data, int len) {
        buffer += new String(data, 0, len);// "ASCII").substring(0, len);
    }

    public ArrayList<WiFiPacket> getPackets() {

        ArrayList<WiFiPacket> packets = new ArrayList<>();

        int termPos1 = 0, termPos2 = 0;

        do {
            termPos1 = buffer.indexOf(13);
            termPos2 = buffer.indexOf(10);

            if (termPos1 > -1 && termPos2 > -1) {
                String packetString = buffer.substring(0, termPos2).trim();
                buffer = buffer.substring(termPos2 + 1);
                try {
                    packets.add(new WiFiPacket(packetString));
                } catch (WFParseException e) {
                    e.printStackTrace();
                }
            }
        } while (termPos1 > -1 && termPos2 > -1);

        return packets;
    }
}
