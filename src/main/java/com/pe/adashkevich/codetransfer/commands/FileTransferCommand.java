package com.pe.adashkevich.codetransfer.commands;

import com.pe.adashkevich.codetransfer.QRCodeScanner;
import com.pe.adashkevich.codetransfer.QRCommandScanner;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                String chunkData = qrCodeScanner.getQrCodeData();
                int filePosition = chunkSize*chunkNum;
                Arrays.fill(transferResult, filePosition, filePosition + chunkData.length(), Boolean.TRUE);

                targetFile.seek(chunkSize*chunkNum);
                targetFile.writeChars(chunkData);
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
        raf.write(new byte[fileSize]);
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
            try(ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(Arrays.asList(transferResult));
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
        File transferResultFile = Paths.get(filePath, fileName + ".res").toFile();
        if(transferResultFile.exists()) {
            try(FileInputStream fis = new FileInputStream(transferResultFile)) {
                try(ObjectInputStream ois = new ObjectInputStream(fis)) {
                    List<Boolean> transferResultWrapper =  (ArrayList)ois.readObject();
                    transferResult = new boolean[transferResultWrapper.size()];

                    for(int i = 0; i < transferResultWrapper.size(); ++i) {
                        transferResult[i] = transferResultWrapper.get(i);
                    }
                    return transferResult;
                }
            }
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
