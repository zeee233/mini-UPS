package org.example;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.example.protoc.UpsAmazon.*;
import org.example.protoc.WorldAmazon.*;
import org.example.utils.CommHelper;

public class MockAmazon {
    private final int WORLD_PORT = 23456;
    private final int UPS_PORT = 9999;
    private Socket upsSocket;
    private Socket worldSocket;
    private long worldID;

    public MockAmazon(String host, int port) throws UnknownHostException, IOException {
        worldSocket = new Socket(host, WORLD_PORT);
        upsSocket = new Socket(host, port);
    }

    public AConnect.Builder createUConnect(Long worldID, int whNum) {
        AConnect.Builder connectToWorld = AConnect.newBuilder();
        if (worldID != null) {
            connectToWorld.setWorldid(worldID);
        }
        connectToWorld.setIsAmazon(true);
        for (int i = 0; i < whNum; ++i) {
            AInitWarehouse.Builder wh = AInitWarehouse.newBuilder();
            wh.setId(i + 1).setX(i + 2).setY(i + 2);
            wh.build();
            connectToWorld.addInitwh(wh);
        }

        return connectToWorld;
    }

    public boolean connectToWorld(AConnect.Builder connectToWorld) throws IOException {
        CommHelper.sendMSG(connectToWorld, worldSocket);

        AConnected.Builder connectResult = AConnected.newBuilder();
        CommHelper.recvMSG(connectResult, worldSocket);

        System.out.println("world ID: " + connectResult.getWorldid());
        System.out.println("result: " + connectResult.getResult());

        worldID = connectResult.getWorldid();

        return connectResult.getResult().equals("connected!");
    }

    public void sendAInformWorld() {
        UReceivedWorld.Builder aInformWorld = UReceivedWorld.newBuilder();
        aInformWorld.setWorldid(worldID);
        CommHelper.sendMSG(aInformWorld, upsSocket);
        System.out.println("[DEBUG] Send world id to ups");
    }

    public void sendABookTruck() {
        ABookTruck.Builder aBookTruck = ABookTruck.newBuilder();
        aBookTruck.setPackageid(1);
        aBookTruck.setWarehouseid(1);
        aBookTruck.setWarehousex(2);
        aBookTruck.setWarehousey(2);
        aBookTruck.setDestinationx(10);
        aBookTruck.setDestinationy(11);

        aBookTruck.setUpsid("7");
        aBookTruck.setDetail("This is detail");

        AUCommunication.Builder auCommunication = AUCommunication.newBuilder();
        auCommunication.addBookings(aBookTruck);
        CommHelper.sendMSG(auCommunication, upsSocket);
        System.out.println("ups socket: "+upsSocket);
    }

    public void receiveUTruckArrived() {
        // UACommunication.Builder uaCommunication = UACommunication.newBuilder();
        // CommHelper.recvMSG(uaCommunication, upsSocket);
        // System.out.println("Packageid: " + uTruckArrived.getPackageid());
        // System.out.println("Truckid: " + uTruckArrived.getTruckid());
    }

    public void stop() {
        try {
            worldSocket.close();
            upsSocket.close();
        } catch (IOException e) {
            System.out.println("Failed to stop mockAmazon: " + e.getMessage());
        }
    }
}
