package com.pe.adashkevich.codetransfer;

import java.util.concurrent.TimeUnit;

public class ThreadTest extends Thread {

    @Override
    public void run() {
        try {
            while (true) {
                TimeUnit.MILLISECONDS.sleep(100);
                System.out.println("notify");
                this.notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
