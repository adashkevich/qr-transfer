package com.pe.adashkevich.codetransfer;

import com.google.zxing.WriterException;
import com.pe.adashkevich.codetransfer.commands.FileTransferCommand;
import com.pe.adashkevich.codetransfer.commands.MakeDirectoryCommand;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class QRCommandGenerator {

    private QRCodeGenerator qrCodeGenerator = new QRCodeGenerator();

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
                //qrCodeGenerator.showQRCode();
                System.out.println(entryPoint.toString());
            }
            return true;
        }
        return false;
    }

    public void filesTransfer(File entryPoint, Consumer<Image> update) throws Exception{
        if(entryPoint.isDirectory()) {
            for(File innerFile : entryPoint.listFiles()) {
                filesTransfer(innerFile, update);
            }
        } else {
            qrCodeGenerator.transferFileByQRCodes(Paths.get(entryPoint.getPath()), update);
            System.out.println(entryPoint.toString());
        }
    }

    private FileTransferCommand createFileTransferCommand(File file) {
        return FileTransferCommand.builder()
                .fileName(file.getName())
                .filePath(file.getParent())
                .fileSize((int)file.length())
                .hash("abcde")
                .build();
    }

    public void processTransferPlan(String planPath, Consumer<Image> update) throws Exception {
        try(BufferedReader reader = new BufferedReader(new FileReader(planPath))) {
            //skip header
            reader.readLine();
            String filePath = reader.readLine();
            while (filePath != null) {
                filesTransfer(new File(filePath), update);
                filePath = reader.readLine();
            }
        }
    }

    public static void main(String[] args) {
//        try {
//            QRCommandGenerator cg = new QRCommandGenerator();
//            if(args[0].endsWith("Plan.csv")) {
//                cg.processTransferPlan(args[0]);
//            } else {
//                cg.filesTransfer(new File(args[0]));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
