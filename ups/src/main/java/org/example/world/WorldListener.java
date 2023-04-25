package org.example.world;

import java.net.Socket;

import org.example.protoc.WorldUps.*;
import org.example.utils.CommHelper;


public class WorldListener implements Runnable {
    private Socket worldSocket;

    public WorldListener(Socket socket) {
        this.worldSocket = socket;
    }

    @Override
    public void run() {
        // loop to listen for responses from world
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            UResponses.Builder uResponses = UResponses.newBuilder();
            CommHelper.recvMSG(uResponses, worldSocket);

            
            // 1. Deal UFinished
            for(UFinished uFinished: uResponses.getCompletionsList()){
                // update truck table
            }

            // 2. Deal UDeliveryMade

        }
    }
}
