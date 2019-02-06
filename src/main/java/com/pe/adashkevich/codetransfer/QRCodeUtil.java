package com.pe.adashkevich.codetransfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class QRCodeUtil {

    public static byte[] toByteArray(int value) {
        return  ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte[] concat(byte[]...byteArrays) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for(byte[] array : byteArrays) {
            os.write(array);
        }
        return os.toByteArray();
    }
}
