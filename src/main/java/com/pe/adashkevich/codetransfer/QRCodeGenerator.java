package com.pe.adashkevich.codetransfer;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pe.adashkevich.codetransfer.commands.Command;
import com.pe.adashkevich.codetransfer.commands.EndFileTransferCommand;
import com.pe.adashkevich.codetransfer.commands.FileTransferCommand;
import org.apache.commons.lang3.RandomStringUtils;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

import static com.pe.adashkevich.codetransfer.CodeTransferCfg.*;

public class QRCodeGenerator extends QRCodeUtil {

    private static final Path qrCodePath = FileSystems.getDefault().getPath("./qr-code.png");
    private static final byte[] end = {69, 78, 68}; // "END".getBytes(CodeTransferCfg.QR_DATA_ENCODING);
    private Random random = new Random();

    public void generateQRCodeImage(String text)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_CODE_IMAGE_SIZE,
                QR_CODE_IMAGE_SIZE);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrCodePath);
    }

    private String fileContent(Path path) throws IOException {
        return encode(Files.readAllBytes(path));
    }

    private String encode(byte[] bytes) {
        return new String(bytes, Charset.forName(QR_DATA_ENCODING));
    }

    public void showQRCode(Consumer<javafx.scene.image.Image> update) throws InterruptedException {
//        System.out.println("QRCodeGenerator.showQRCode()");
        update.accept(new javafx.scene.image.Image(qrCodePath.toUri().toString()));
        Thread.sleep(QR_SHOW_TIME);
    }


    public byte[] readBytes(InputStream is, int bufferSize) throws IOException {
        byte[] readBytes = new byte[bufferSize];
        int bytesReadCount = is.read(readBytes, 0, bufferSize);
        if (bytesReadCount == -1) {
            return new byte[0];
        } else if (bytesReadCount != bufferSize) {
            return Arrays.copyOf(readBytes, bytesReadCount);
        }
        return readBytes;
    }

    private Command createFileTransferCommand(File file) {
        return FileTransferCommand.builder()
                .fileName(file.getName())
                .filePath(file.getParent())
                .fileSize((int)file.length())
                .hash(generateHash())
                .build();
    }

    public void transferFileByQRCodes(Path path, Consumer<javafx.scene.image.Image> update) throws IOException, InterruptedException, WriterException {
        for(int i = 0; i < 5; ++i) {
            generateQRCodeImage(createFileTransferCommand(path.toFile()).toString());
        }
        showQRCode(update);

        int position = 0;
        int counter = 0;
        InputStream is = new FileInputStream(path.toFile());

        byte[] fileContent;
        while ((fileContent = readBytes(is, getChunkSize())).length != 0) {
            fileContent = concat(toByteArray(position), fileContent, end);
            generateQRCodeImage(encode(fileContent));
            showQRCode(update);
            position += fileContent.length - 7;
            counter ++;
        }
        System.out.println(String.format("File split to %d QR codes", counter));

        generateQRCodeImage(new EndFileTransferCommand().toString());
        showQRCode(update);
    }

    public int getChunkSize() {
        return random.ints(QR_CODE_MIN_SIZE, QR_CODE_MAX_SIZE).findFirst().getAsInt();
    }

    private String generateHash() {
        int length = random.ints(4, 8).findFirst().getAsInt();
        return RandomStringUtils.random(length, true, false);
    }

//    private void createFileNameQRCode(Path path) throws IOException, WriterException, InterruptedException {
//        byte[] fileContent = concat(toByteArray(0), path.getFileName().toString().getBytes(CodeTransferCfg.QR_DATA_ENCODING));
//        generateQRCodeImage(encode(fileContent));
//        showQRCode();
//    }

    public static void main(String[] args) {
//        try {
//            QRCodeGenerator generator = new QRCodeGenerator();
//            Path archivePath = Paths.get(args[0]);
//            generator.transferFileByQRCodes(archivePath);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
