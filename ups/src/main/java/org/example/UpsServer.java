package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.example.WorldUps.UConnect;
import org.example.WorldUps.UConnected;
import org.example.WorldUps.UInitTruck;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.ByteString.Output;

public class UpsServer {
    private ServerSocket upsSocket;
    private Socket worldSocket;

    private final int WORLD_PORT = 12345;
    private final int AMAZON_PORT = 23456;

    // private InputStream in;
    // private OutputStream out;

    public UpsServer(int port) throws IOException {
        // upsSocket = new ServerSocket(port);
        worldSocket = new Socket("127.0.0.1", WORLD_PORT);
    }

    // TODO: move them to other file
    public <T extends GeneratedMessageV3.Builder<?>> boolean sendMSG(T builder, OutputStream out) {
        try {
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(out);
            codedOutputStream.writeUInt32NoTag(builder.build().toByteArray().length);
            builder.build().writeTo(codedOutputStream);
            codedOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T extends GeneratedMessageV3.Builder<?>> boolean recvMSG(T builder, InputStream in) {
        try {
            CodedInputStream codedInputStream = CodedInputStream.newInstance(in);
            int length = codedInputStream.readRawVarint32();
            int parseLimit = codedInputStream.pushLimit(length);
            builder.mergeFrom(codedInputStream);
            codedInputStream.popLimit(parseLimit);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public UConnect.Builder createUConnect(Integer worldID, int truckNum) {
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

    public boolean connectToWorld() throws IOException {
        sendMSG(createUConnect(null, 10), worldSocket.getOutputStream());

        UConnected.Builder connectResult = UConnected.newBuilder();
        recvMSG(connectResult, worldSocket.getInputStream());

        System.out.println("world ID: " + connectResult.getWorldid());
        System.out.println("result: " + connectResult.getResult());

        return connectResult.getResult().equals("connected!");
    }
}
