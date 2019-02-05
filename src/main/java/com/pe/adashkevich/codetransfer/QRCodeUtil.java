package com.pe.adashkevich.codetransfer;

import java.nio.ByteBuffer;

public class QRCodeUtil {

    static public byte[] toByteArray(int value) {
        return  ByteBuffer.allocate(4).putInt(value).array();
    }

    static public int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
