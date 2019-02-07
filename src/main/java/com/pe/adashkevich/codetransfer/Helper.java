package com.pe.adashkevich.codetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Helper {

    String planCSV = "";

    public void generateTransferPlan(String entryPoint) throws FileNotFoundException {
        planCSV = createPlaneTemplate();
        addFilesToPlan(new File(entryPoint));

        try (PrintWriter out = new PrintWriter("fileTransferPlan.csv")) {
            out.println(planCSV);
        }

    }

    private void addFilesToPlan(File entryPoint) {
        if(entryPoint.isDirectory()) {
            for(File innerFile : entryPoint.listFiles()) {
                addFilesToPlan(innerFile);
            }
        } else {
            addFileToPlan(entryPoint);
        }
    }

    private void addFileToPlan(File entryPoint) {
        planCSV += "\n" + entryPoint.getPath();
    }

    private String createPlaneTemplate() {
        return "file";
    }

    public static void main(String[] args) {
        try {
            Helper helper = new Helper();
            helper.generateTransferPlan(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
