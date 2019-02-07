package com.pe.adashkevich.codetransfer;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pe.adashkevich.codetransfer.commands.Command;
import com.pe.adashkevich.codetransfer.commands.EndFileTransferCommand;
import com.pe.adashkevich.codetransfer.commands.FileTransferCommand;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class QRCodeGenerator extends QRCodeUtil {

    private static final Path qrCodePath = FileSystems.getDefault().getPath("./qr-code.png");
    private static final byte[] end = {69, 78, 68}; // "END".getBytes(CodeTransferCfg.QR_DATA_ENCODING);


    public void generateQRCodeImage(String text)
            throws WriterException, IOException {
        //System.out.println("========================");
        //System.out.println(text);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, CodeTransferCfg.QR_CODE_IMAGE_SIZE,
                CodeTransferCfg.QR_CODE_IMAGE_SIZE);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrCodePath);
    }

    private String fileContent(Path path) throws IOException {
        return encode(Files.readAllBytes(path));
    }

    private String encode(byte[] bytes) {
        return new String(bytes, Charset.forName(CodeTransferCfg.QR_DATA_ENCODING));
    }

    public void showQRCode() throws IOException, InterruptedException {
        openQRCode(qrCodePath);
        TimeUnit.SECONDS.sleep(5);
        closeQRCode();
    }

    private void openQRCode(Path path) throws IOException {
        File f = path.toFile();
        Desktop dt = Desktop.getDesktop();
        dt.open(f);
    }

    private void closeQRCode() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("Taskkill /IM Microsoft.Photos.exe /F");
        process.waitFor(); // TODO check process finish result
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
                .chunkSize(CodeTransferCfg.MAX_QR_CODE_DATA_SIZE)
                .build();
    }

    public void transferFileByQRCodes(Path path) throws IOException, InterruptedException, WriterException {
        generateQRCodeImage(createFileTransferCommand(path.toFile()).toString());
        showQRCode();

        int counter = 0;
        InputStream is = new FileInputStream(path.toFile());

        byte[] fileContent;
        while ((fileContent = readBytes(is, CodeTransferCfg.MAX_QR_CODE_DATA_SIZE)).length != 0) {
            fileContent = concat(toByteArray(counter), fileContent, end);
            generateQRCodeImage(encode(fileContent));
            showQRCode();
            ++counter;
        }
        System.out.println(String.format("File split to %d QR codes", counter));

        generateQRCodeImage(new EndFileTransferCommand().toString());
        showQRCode();
    }

    private void createFileNameQRCode(Path path) throws IOException, WriterException, InterruptedException {
        byte[] fileContent = concat(toByteArray(0), path.getFileName().toString().getBytes(CodeTransferCfg.QR_DATA_ENCODING));
        generateQRCodeImage(encode(fileContent));
        showQRCode();
    }

    public static void main(String[] args) {
        try {
            QRCodeGenerator generator = new QRCodeGenerator();
            Path archivePath = Paths.get(args[0]);
            generator.transferFileByQRCodes(archivePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
