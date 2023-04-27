package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.example.amazon.AmazonListener;
import org.example.amazon.AmazonSender;
import org.example.domain.*;
import org.example.protoc.UpsAmazon.*;
import org.example.protoc.WorldUps.*;
import org.example.utils.*;
import org.example.world.WorldListener;
import org.example.world.WorldSender;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class UpsServer {
    private ServerSocket upsServerSocket;
    private Socket worldSocket;
    private Socket amazonSocket;
    private long worldID;

    private WorldListener worldListener;
    private WorldSender worldSender;

    private AmazonListener amazonListener;
    private AmazonSender amazonSender;

    private SessionFactory sessionFactory;

    private ThreadPoolExecutor threadPool;

    private final int WORLD_PORT = 12345;
    private final int AMAZON_PORT = 23456;

    public UpsServer(int port, SessionFactory sessionFactory) throws IOException {
        upsServerSocket = new ServerSocket(port);
        worldSocket = new Socket("vcm-31112.vm.duke.edu", WORLD_PORT);

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(32);
        threadPool = new ThreadPoolExecutor(20, 20, 100, TimeUnit.SECONDS, workQueue);
        upsServerSocket.setSoTimeout(1200000);

        this.sessionFactory = sessionFactory;
    }

    // TODO: may remove to other file
    public UConnect.Builder createUConnect(Long worldID, int truckNum) {
        UConnect.Builder connectToWorld = UConnect.newBuilder();
        if (worldID != null) {
            connectToWorld.setWorldid(worldID);
        }
        connectToWorld.setIsAmazon(false);
        Session session = sessionFactory.openSession();
        for (int i = 0; i < truckNum; ++i) {
            UInitTruck.Builder truck = UInitTruck.newBuilder();
            truck.setId(i + 1).setX(0).setY(0);
            truck.build();
            connectToWorld.addTrucks(truck);

            //insert into the Truck table
            TruckD cur_truck = new TruckD();
            cur_truck.setTruckId(i);
            cur_truck.setX(0);
            cur_truck.setY(0);
            cur_truck.setStatus("idle");
            Transaction transaction=session.beginTransaction();
            session.save(cur_truck);
            transaction.commit();
        }
        session.close();

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
            System.out.println("[DEBUG] Amazon connected");
            // 2. waiting for world id
            AInformWorld.Builder aInformWorld = AInformWorld.newBuilder();
            CommHelper.recvMSG(aInformWorld, amazonSocket);
            worldID = aInformWorld.getWorldid();
            System.out.println("[DEBUG] Received world id: " + worldID);
            // 3. connect to the world
            boolean connectResult = false;
            do {
                connectResult = connectToWorld(createUConnect(worldID, 50));
            } while (!connectResult);
            System.out.println("[DEBUG] Connected to the world");
            // TODO: remember to write the truck to the database

            worldListener = new WorldListener(worldSocket, sessionFactory);
            worldSender = new WorldSender(worldSocket, sessionFactory);
            amazonListener = new AmazonListener(amazonSocket, sessionFactory);
            amazonSender = new AmazonSender(amazonSocket, sessionFactory);

            // start world listener
            threadPool.execute(amazonListener);
            threadPool.execute(worldListener);
            threadPool.execute(worldSender);
            threadPool.execute(amazonSender);

            while (true) {
                // avoid main thread exit
            }

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
