package com.pe.adashkevich.codetransfer;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class QRCodeScanner extends QRCodeUtil {

    private static final String SCREENSHOT_PATH = "./screenshot.jpg";
    private static final int MAX_SCANNING_PAUSE = 10;

    private static void takeScreenshot() throws AWTException, IOException {
        Path screenshotPath = getScreenshotPath();
        Robot robot = new Robot();

        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
        ImageIO.write(screenFullImage, "jpg", screenshotPath.toFile());
    }

    private static String decodeQRCode(Path qrCode) throws IOException {
        //long startTime = System.currentTimeMillis();
        BufferedImage bufferedImage = ImageIO.read(qrCode.toFile());
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType,Object> tmpHintsMap = new EnumMap<>(DecodeHintType.class);
        tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.of(BarcodeFormat.QR_CODE));
        tmpHintsMap.put(DecodeHintType.CHARACTER_SET, CodeTransferCfg.QR_DATA_ENCODING);

        try {
            Result result = new MultiFormatReader().decode(bitmap, tmpHintsMap);
            //System.out.println("QR code decoding take: " + (System.currentTimeMillis() - startTime) + "ms");
            return result.getText();
        } catch (NotFoundException e) {
            //System.out.println("Unsuccessful decoding attempt: " + (System.currentTimeMillis() - startTime) + "ms");
            return null;
        }
    }

    private static void screenCapture(OutputStream os) throws IOException, AWTException, InterruptedException {
        //long startTime = System.currentTimeMillis();
        boolean startScanning = false;
        String lastScannedData = "";
        long lastScannedTime = 0;
        int counter = -1;
        while(!startScanning || (System.currentTimeMillis() - lastScannedTime) < MAX_SCANNING_PAUSE * 1000) {
            takeScreenshot();
            String qrCodeData = decodeQRCode(getScreenshotPath());
            if(qrCodeData != null && !lastScannedData.equals(qrCodeData)) {
                lastScannedData = qrCodeData;
                counter = getQRCodeNumber(qrCodeData);
                qrCodeData = qrCodeData.substring(4, qrCodeData.length() - 3);
                os.write(qrCodeData.getBytes(Charset.forName(CodeTransferCfg.QR_DATA_ENCODING)));
                startScanning = true;
                lastScannedTime = System.currentTimeMillis();
                System.out.println(counter);
            }
            TimeUnit.MILLISECONDS.sleep(50);
            //System.out.println("Iteration of screen capture take: " + (System.currentTimeMillis() - startTime) + "ms");
        }
        os.flush();
    }

    private String lastScannedData = "";
    String qrCodeData = "";

    public String getQrCodeData() {
        return qrCodeData;
    }

    public int getFilePosition() throws UnsupportedEncodingException {
        return getQRCodeNumber(qrCodeData);
    }

    public String getFileChunkData() {
        return qrCodeData.substring(4, qrCodeData.length() - 3);
    }

    public boolean waitQRCode() throws Exception {
        takeScreenshot();
        qrCodeData = decodeQRCode(getScreenshotPath());
        if(qrCodeData != null && !lastScannedData.equals(qrCodeData)) {
            lastScannedData = qrCodeData;
            return true;
        }
        TimeUnit.MILLISECONDS.sleep(50);
        return false;
    }

    private static int getQRCodeNumber(String qrCodeData) throws UnsupportedEncodingException {
        return fromByteArray(qrCodeData.substring(0, 4).getBytes(CodeTransferCfg.QR_DATA_ENCODING));
    }

    private static Path getScreenshotPath() {
        return FileSystems.getDefault().getPath(SCREENSHOT_PATH);
    }

    public static void main(String[] args) {
        try {
            try(OutputStream os = new FileOutputStream(new File(args[0]))) {
                screenCapture(os);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
