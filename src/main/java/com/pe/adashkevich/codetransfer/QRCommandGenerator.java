package com.pe.adashkevich.codetransfer;

import com.google.zxing.WriterException;
import com.pe.adashkevich.codetransfer.commands.FileTransferCommand;
import com.pe.adashkevich.codetransfer.commands.MakeDirectoryCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class QRCommandGenerator {

    private QRCodeGenerator qrCodeGenerator;

    public QRCommandGenerator() {
        qrCodeGenerator = new QRCodeGenerator();
    }

    public boolean skeletonTransfer(File entryPoint) throws IOException, WriterException, InterruptedException {
        boolean alreadyCreate = false;
        if(entryPoint.isDirectory()) {
            //generate qr code
            for(File innerFile : entryPoint.listFiles()) {
                if(skeletonTransfer(innerFile)) {
                    alreadyCreate = true;
                }
            }

            if(!alreadyCreate) {
                qrCodeGenerator.generateQRCodeImage(MakeDirectoryCommand.Builder.getBuilder()
                        .folderPath(entryPoint.toString()).build().toString());
                qrCodeGenerator.showQRCode();
                System.out.println(entryPoint.toString());
            }
            return true;
        }
        return false;
    }

    public void filesTransfer(File entryPoint) throws Exception{
        if(entryPoint.isDirectory()) {
            for(File innerFile : entryPoint.listFiles()) {
                filesTransfer(innerFile);
            }
        } else {
            qrCodeGenerator.transferFileByQRCodes(Paths.get(entryPoint.getPath()));
            System.out.println(entryPoint.toString());
        }
    }

    private FileTransferCommand createFileTransferCommand(File file) {
        return FileTransferCommand.builder()
                .fileName(file.getName())
                .filePath(file.getParent())
                .fileSize((int)file.length())
                .chunkSize(CodeTransferCfg.MAX_QR_CODE_DATA_SIZE)
                .build();
    }

    private void processTransferPlan(String planPath) throws Exception {
        try(BufferedReader reader = new BufferedReader(new FileReader(planPath))) {
            //skip header
            reader.readLine();
            String filePath = reader.readLine();
            while (filePath != null) {
                filesTransfer(new File(filePath));
                filePath = reader.readLine();
            }
        }
    }

    public static void main(String[] args) {
        try {
            QRCommandGenerator cg = new QRCommandGenerator();
            if(args[0].endsWith("Plan.csv")) {
                cg.processTransferPlan(args[0]);
            } else {
                cg.filesTransfer(new File(args[0]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
