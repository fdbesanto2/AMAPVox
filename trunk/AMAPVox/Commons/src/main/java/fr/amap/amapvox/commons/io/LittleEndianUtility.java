/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.io;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author calcul
 */
public class LittleEndianUtility {
    
    public static short bytesToShort(byte b1, byte b2) {

        byte[] bytes = new byte[]{b1, b2};
        short result = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return result;
    }

    public static int bytesToShortInt(byte b1, byte b2) {

        byte b3 = 0, b4 = 0;
        return bytesToInt(b1, b2, b3, b4);
    }

    public static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result += ((256 + bytes[i]) % 256) * (int) Math.pow(256, i);
        }
        return result;
    }

    public static int bytesToInt(byte b1, byte b2, byte b3, byte b4) {

        byte[] bytes = new byte[]{b1, b2, b3, b4};
        int result = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return result;
    }
    
    public static int toInt(byte b1, byte b2, byte b3, byte b4) {

        byte[] bytes = new byte[]{b1, b2, b3, b4};
        ArrayUtils.reverse(bytes);
        int d = ByteBuffer.wrap(bytes).getInt();
        return d;
    }
    
    public static long tolong(byte[] bytes) {

        ArrayUtils.reverse(bytes);
        long d = ByteBuffer.wrap(bytes).getLong();
        return d;
    }

    public static double toDouble(byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6, byte byte7, byte byte8) {

        byte[] bytes = new byte[]{byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8};
        //int[] integers = new int[]{byte1&0xff, byte2&0xff, byte3&0xff, byte4&0xff, byte5&0xff, byte6&0xff, byte7&0xff, byte8&0xff};
        ArrayUtils.reverse(bytes);
        double d = ByteBuffer.wrap(bytes).getDouble() + 0.0;
        return d;
    }
    
    public static float toFloat(byte byte1, byte byte2, byte byte3, byte byte4) {

        byte[] bytes = new byte[]{byte1, byte2, byte3, byte4};        
        ArrayUtils.reverse(bytes);
        float value = ByteBuffer.wrap(bytes).getFloat()+ 0.0f;
        return value;
    }

    public static BigInteger toBigInteger(byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6, byte byte7, byte byte8) {

        byte[] bytes = new byte[]{byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8};
        ArrayUtils.reverse(bytes);
        BigInteger bi = new BigInteger(bytes);
        return bi;
    }
}
