package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.example.protoc.UpsAmazon.*;
import org.example.protoc.WorldUps.*;
import org.example.utils.*;

public class UpsServer {
    private ServerSocket upsServerSocket;
    private Socket worldSocket;
    private Socket amazonSocket;
    private long worldID;

    private ThreadPoolExecutor threadPool;

    private final int WORLD_PORT = 12345;
    private final int AMAZON_PORT = 23456;

    public UpsServer(int port) throws IOException {
        upsServerSocket = new ServerSocket(port);
        worldSocket = new Socket("127.0.0.1", WORLD_PORT);

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(32);
        threadPool = new ThreadPoolExecutor(20, 20, 100, TimeUnit.SECONDS, workQueue);
        upsServerSocket.setSoTimeout(1200000);
    }

    // TODO: may remove to other file
    public UConnect.Builder createUConnect(Long worldID, int truckNum) {
        UConnect.Builder connectToWorld = UConnect.newBuilder();
        if (worldID != null) {
            connectToWorld.setWorldid(worldID);
        }
        connectToWorld.setIsAmazon(false);
        for (int i = 0; i < truckNum; ++i) {
            UInitTruck.Builder truck = UInitTruck.newBuilder();
            truck.setId(i + 1).setX(0).setY(0);
            truck.build();
            connectToWorld.addTrucks(truck);
        }

        return connectToWorld;
    }

    public boolean connectToWorld(UConnect.Builder connectToWorld) throws IOException {
        CommHelper.sendMSG(connectToWorld, worldSocket);

        UConnected.Builder connectResult = UConnected.newBuilder();
        CommHelper.recvMSG(connectResult, worldSocket);

        System.out.println("world ID: " + connectResult.getWorldid());
        System.out.println("result: " + connectResult.getResult());

        return connectResult.getResult().equals("connected!");
    }

    public void start() {
        try {
            // 1. waiting for amazon to connect
            amazonSocket = upsServerSocket.accept();
            // 2. waiting for world id
            AInformWorld.Builder aInformWorld = AInformWorld.newBuilder();
            CommHelper.recvMSG(aInformWorld, worldSocket);
            worldID = aInformWorld.getWorldid();
            // 3. connect to the world
            boolean connectResult = false;
            do {
                connectResult = connectToWorld(createUConnect(worldID, 50));
            } while (!connectResult);
            // TODO: remember to write the truck to the database

            // start world listener
            // start world receiver
        } catch (IOException e) {
            stop();
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            worldSocket.close();
            amazonSocket.close();
            upsServerSocket.close();
        } catch (IOException e) {
            System.out.println("Failed to stop server: " + e.getMessage());
        }
    }

}
