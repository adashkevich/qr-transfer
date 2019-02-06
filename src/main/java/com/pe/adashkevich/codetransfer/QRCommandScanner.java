package com.pe.adashkevich.codetransfer;

import com.pe.adashkevich.codetransfer.commands.Command;
import com.pe.adashkevich.codetransfer.commands.FileTransferCommand;
import com.pe.adashkevich.codetransfer.commands.MakeDirectoryCommand;

import java.util.StringTokenizer;

public class QRCommandScanner {

    public QRCodeScanner qrCodeScanner;

    public QRCommandScanner() {
        qrCodeScanner = new QRCodeScanner();
    }

    private boolean isCommand(String qrData) {
        return qrData.startsWith("#cmd");
    }

    private Command scanCommand(String qrData) throws Exception {
        StringTokenizer commandStr = new StringTokenizer(qrData, "|");

        if(!commandStr.hasMoreTokens() || !"#cmd".equals(commandStr.nextToken())) {
            throw new Exception("It is not command!");
        }

        switch (commandStr.nextToken()) {
            case "TF":
                return new FileTransferCommand();
            case "MD":
                return new MakeDirectoryCommand(qrData);
        }
        throw new Exception("Command not recognized!");
    }

    public static void main(String[] args) {
        try {
            QRCommandScanner cs = new QRCommandScanner();

            while (true) {
                if(cs.qrCodeScanner.waitQRCode()) {
                    Command command = cs.scanCommand(cs.qrCodeScanner.getQrCodeData());
                    System.out.println(command.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
