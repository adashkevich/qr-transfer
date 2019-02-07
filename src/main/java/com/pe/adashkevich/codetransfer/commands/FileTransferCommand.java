package com.pe.adashkevich.codetransfer.commands;

import com.pe.adashkevich.codetransfer.CodeTransferCfg;
import com.pe.adashkevich.codetransfer.QRCodeScanner;
import com.pe.adashkevich.codetransfer.QRCommandScanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileTransferCommand implements Command {

    private String fileName;
    private String filePath;
    private int fileSize;
    private int chunkSize;
    private QRCodeScanner qrCodeScanner;
    private RandomAccessFile targetFile;
    private boolean[] transferResult;

    private FileTransferCommand() {
    }

    public FileTransferCommand(String qrCodeData, QRCodeScanner qrCodeScanner) {
        String[] commandCfg = qrCodeData.split("\\|");
        fileName = commandCfg[2];
        filePath = commandCfg[3];
        fileSize = Integer.parseInt(commandCfg[4]);
        chunkSize = Integer.parseInt(commandCfg[5]);
        this.qrCodeScanner = qrCodeScanner;
    }

    @Override
    public String toString() {
        return String.format("#cmd|TF|%s|%s|%d|%d", fileName, filePath, fileSize, chunkSize);
    }

    @Override
    public void exec() throws Exception {
        readTransferResult();

        targetFile = getFile();
        while (true) {
            if(qrCodeScanner.waitQRCode()) {
                if(QRCommandScanner.isCommand(qrCodeScanner.getQrCodeData())) {
                    break;
                }
                int chunkNum = qrCodeScanner.getFileChunkNumber();
                String chunkData = qrCodeScanner.getFileChunkData();
                int filePosition = chunkSize*chunkNum;
                System.out.println(filePosition + "|" + filePosition + chunkData.length());
                Arrays.fill(transferResult, filePosition, filePosition + chunkData.length(), Boolean.TRUE);

                targetFile.seek(chunkSize*chunkNum);
                targetFile.writeBytes(chunkData);
            }
        }
        targetFile.close();

        int missedBytesCount = missedBytes(transferResult);
        if(missedBytesCount == 0) {
            System.out.println(String.format("File %s was successfully transferred", fileName));
            removeTransferResult();
        } else {
            System.err.println(String.format("File %s was transferred with missing of %d bytes.", fileName, missedBytesCount));
            saveTransferResult();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String fileName;
        private String filePath;
        private int fileSize;
        private int chunkSize;
        private QRCodeScanner qrCodeScanner;

        private Builder() {
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder fileSize(int fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder chunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public Builder qrCodeScanner(QRCodeScanner qrCodeScanner) {
            this.qrCodeScanner = qrCodeScanner;
            return this;
        }

        public FileTransferCommand build() {
            FileTransferCommand fileTransferCommand = new FileTransferCommand();
            fileTransferCommand.filePath = this.filePath;
            fileTransferCommand.fileName = this.fileName;
            fileTransferCommand.fileSize = this.fileSize;
            fileTransferCommand.chunkSize = this.chunkSize;
            fileTransferCommand.qrCodeScanner = this.qrCodeScanner;
            return fileTransferCommand;
        }
    }

    public RandomAccessFile getFile() throws IOException {
        mkdirs();
        File file = Paths.get(filePath, fileName).toFile();
        System.out.println();
        if(!file.exists()) {
            createEmptyFile(file);
        }
        return new RandomAccessFile(file, "rw");
    }

    private void mkdirs() {
        File file = Paths.get(filePath).toFile();
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    private void createEmptyFile(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(fileSize);
        //raf.write(new byte[fileSize]);
        raf.close();
    }

    private int missedBytes(boolean[] transferResult) {
        int missedBytes = 0;
        for(int i = 0; i < transferResult.length; ++i) {
            if(!transferResult[i]) {
                ++missedBytes;
            }
        }
        return missedBytes;
    }

    private void saveTransferResult() throws IOException {
        File transferResultFile = Paths.get(filePath, fileName + ".res").toFile();
        try(FileOutputStream fos = new FileOutputStream(transferResultFile)) {
            for(boolean isMissedByte : transferResult) {
                String booleanStr = "F";
                if (isMissedByte) {
                    booleanStr = "T";
                }
                fos.write(booleanStr.getBytes(CodeTransferCfg.QR_DATA_ENCODING));
            }
        }
    }

    private void removeTransferResult() {
        File transferResultFile = Paths.get(filePath, fileName + ".res").toFile();
        if(transferResultFile.exists()) {
            transferResultFile.delete();
        }
    }

    private boolean[] readTransferResult() throws IOException, ClassNotFoundException {
        Path transferResultPath = Paths.get(filePath, fileName + ".res");
        if(transferResultPath.toFile().exists()) {
            byte[] fileContent = Files.readAllBytes(transferResultPath);
            transferResult = new boolean[fileContent.length];

            for(int i = 0; i < fileContent.length; ++i) {
                if((char)fileContent[i] == 'T') {
                    transferResult[i] = true;
                } else {
                    transferResult[i] = false;
                }
            }
            return transferResult;
        } else {
            transferResult = new boolean[fileSize];
            Arrays.fill(transferResult, Boolean.FALSE);
            return transferResult;
        }
    }

    public static void main(String[] args) {
        try {
            Command command = FileTransferCommand.builder()
                    .fileName("Bot.java")
                    .filePath("src/main/java/com.pg.code")
                    .fileSize(17000)
                    .chunkSize(1000)
                    .qrCodeScanner(new QRCodeScanner())
                    .build();

            command.exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
