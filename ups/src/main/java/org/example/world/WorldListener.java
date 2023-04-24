package org.example.world;

import java.net.Socket;

public class WorldListener implements Runnable {
    private Socket worldSocket;

    public WorldListener(Socket socket) {
        this.worldSocket = socket;
    }

    @Override
    public void run() {
        // loop to listen for responses from world
        while(true){
            // 
        }
    }
}
