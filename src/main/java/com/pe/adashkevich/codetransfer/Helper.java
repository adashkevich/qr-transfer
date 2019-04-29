package com.pe.adashkevich.codetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Helper {

    public final static String PLAN_FILE_NAME = "fileTransferPlan.csv";
    private String planCSV = "";

    public void generateTransferPlan(String entryPoint) throws FileNotFoundException {
        planCSV = createPlaneTemplate();
        File planFile = new File(PLAN_FILE_NAME);
        addFilesToPlan(new File(entryPoint));

        try (PrintWriter out = new PrintWriter(planFile)) {
            out.println(planCSV);
        }

        System.out.println(planFile.getAbsolutePath());
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
