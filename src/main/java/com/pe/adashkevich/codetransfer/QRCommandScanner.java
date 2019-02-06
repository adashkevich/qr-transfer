package com.pe.adashkevich.codetransfer;

import com.pe.adashkevich.codetransfer.commands.Command;
import com.pe.adashkevich.codetransfer.commands.EndFileTransferCommand;
import com.pe.adashkevich.codetransfer.commands.FileTransferCommand;
import com.pe.adashkevich.codetransfer.commands.MakeDirectoryCommand;

import java.util.StringTokenizer;

public class QRCommandScanner {

    public QRCodeScanner qrCodeScanner;

    public QRCommandScanner() {
        qrCodeScanner = new QRCodeScanner();
    }

    public static boolean isCommand(String qrData) {
        return qrData.startsWith("#cmd");
    }

    private Command scanCommand(String qrData) throws Exception {
        StringTokenizer commandStr = new StringTokenizer(qrData, "|");

        if(!commandStr.hasMoreTokens() || !"#cmd".equals(commandStr.nextToken())) {
            throw new Exception("It is not command!");
        }

        switch (commandStr.nextToken()) {
            case "TF":
                return new FileTransferCommand(qrData, qrCodeScanner);
            case "MD":
                return new MakeDirectoryCommand(qrData);
            case "END":
                return new EndFileTransferCommand();

        }
        throw new Exception("Command not recognized!");
    }

    public void scan() throws Exception {

        while (true) {
            if (qrCodeScanner.waitQRCode()) {
                if(isCommand(qrCodeScanner.getQrCodeData())) {
                    Command command = scanCommand(qrCodeScanner.getQrCodeData());
                    System.out.println(command.toString());
                    command.exec();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            QRCommandScanner scanner = new QRCommandScanner();
            scanner.scan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
