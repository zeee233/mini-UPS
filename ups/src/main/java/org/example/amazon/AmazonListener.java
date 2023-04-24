package org.example.amazon;

import java.net.Socket;

import org.example.protoc.UpsAmazon.*;
import org.example.utils.*;

public class AmazonListener implements Runnable {
    private Socket amazonSocket;

    public AmazonListener(Socket socket) {
        amazonSocket = socket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // receive response from amazon every 1s
            AUCommunication.Builder auCommunication = AUCommunication.newBuilder();
            CommHelper.recvMSG(auCommunication, amazonSocket);
            
        }
    }
}
