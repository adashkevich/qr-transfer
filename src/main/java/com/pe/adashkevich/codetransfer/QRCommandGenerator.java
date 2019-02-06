package com.pe.adashkevich.codetransfer;

import com.google.zxing.WriterException;
import com.pe.adashkevich.codetransfer.commands.MakeDirectoryCommand;

import java.io.File;
import java.io.IOException;

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

    public static void main(String[] args) {
        try {
            QRCommandGenerator cg = new QRCommandGenerator();
            cg.skeletonTransfer(new File("qrtransfer"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
