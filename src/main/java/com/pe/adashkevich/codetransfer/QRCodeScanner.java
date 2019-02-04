package com.pe.adashkevich.codetransfer;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class QRCodeScanner {

    private static final String SCREENSHOT_PATH = "./screenshot.jpg";
    private static final int MAX_SCANNING_PAUSE = 30;

    private static void takeScreenshot() throws AWTException, IOException {
        Path screenshotPath = getScreenshotPath();
        Robot robot = new Robot();

        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
        ImageIO.write(screenFullImage, "jpg", screenshotPath.toFile());
    }

    private static String decodeQRCode(Path qrCode) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(qrCode.toFile());
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType,Object> tmpHintsMap = new EnumMap<>(DecodeHintType.class);
        tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.FALSE);
        tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.of(BarcodeFormat.QR_CODE));
        tmpHintsMap.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");

        try {
            Result result = new MultiFormatReader().decode(bitmap, tmpHintsMap);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        }
    }

    private static void screenCapture(OutputStream os) throws IOException, AWTException, InterruptedException {
        boolean startScanning = false;
        String lastScannedData = "";
        long lastScannedTime = 0;
        int counter = 0;
        while(!startScanning || (System.currentTimeMillis() - lastScannedTime) < MAX_SCANNING_PAUSE * 1000) {
            takeScreenshot();
            String qrCodeData = decodeQRCode(getScreenshotPath());
            if(qrCodeData != null && !lastScannedData.equals(qrCodeData)) {
                os.write(qrCodeData.getBytes(Charset.forName("ISO-8859-1")));
                lastScannedData = qrCodeData;
                startScanning = true;
                lastScannedTime = System.currentTimeMillis();
                System.out.println(++counter);
            }
            TimeUnit.MILLISECONDS.sleep(50);
        }
        os.flush();
    }

    private static Path getScreenshotPath() {
        return FileSystems.getDefault().getPath(SCREENSHOT_PATH);
    }
    public static void main(String[] args) {
        try {
            try(OutputStream os = new FileOutputStream(new File("VatComplianceEmailReport.java"))) {
                screenCapture(os);
            }

        } catch (Exception ex) {
            System.err.println("Unknown exception :: " + ex.getMessage());
        }
    }
}
