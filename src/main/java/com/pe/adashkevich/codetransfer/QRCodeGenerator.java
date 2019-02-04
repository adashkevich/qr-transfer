package com.pe.adashkevich.codetransfer;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class QRCodeGenerator {

    private static final String QR_CODE_IMAGE_PATH = "./qr-code.png";
    private static final int MAX_QR_CODE_DATA_SIZE = 300;
    private static final int QR_CODE_IMAGE_SIZE = 1024;

    private static void generateQRCodeImage(String text, int size, Path path)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    private static String fileContent(Path path) throws IOException {
        return encode(Files.readAllBytes(path));
    }

    private static String encode(byte[] bytes) {
        return new String(bytes, Charset.forName("ISO-8859-1"));
    }

    private static void openQRCode(Path path) throws IOException {
        File f = path.toFile();
        Desktop dt = Desktop.getDesktop();
        dt.open(f);
    }

    private static void closeQRCode() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("Taskkill /IM Microsoft.Photos.exe /F"); //Process process =
    }

    public static byte[] readBytes(InputStream is, int bufferSize) throws IOException {
        byte[] readBytes = new byte[bufferSize];
        int bytesReadCount = is.read(readBytes, 0, bufferSize);
        if (bytesReadCount == -1) {
            return new byte[0];
        } else if (bytesReadCount != bufferSize) {
            return Arrays.copyOf(readBytes, bytesReadCount);
        }
        return readBytes;
    }

    private static void transferFileByQRCodes(Path path) throws IOException, InterruptedException, WriterException {
        InputStream is = new FileInputStream(path.toFile());
        Path qrCodePath = FileSystems.getDefault().getPath(QR_CODE_IMAGE_PATH);
        byte[] fileContent;
        while ((fileContent = readBytes(is, MAX_QR_CODE_DATA_SIZE)).length != 0) {
            generateQRCodeImage(encode(fileContent), QR_CODE_IMAGE_SIZE, qrCodePath);
            openQRCode(qrCodePath);
            TimeUnit.SECONDS.sleep(5);
            closeQRCode();
        }
    }

    public static void main(String[] args) {
        try {
            Path archivePath = Paths.get(args[0]);
            transferFileByQRCodes(archivePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
