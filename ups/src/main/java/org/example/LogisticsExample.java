package org.example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.example.WorldUps.UTruck;

// import WorldUps.UTruck;

public class LogisticsExample {
    public static void main(String[] args) {
        // 创建一个 UTruck 消息实例
        UTruck.Builder truckBuilder = UTruck.newBuilder();
        truckBuilder.setTruckid(42)
                    .setStatus("On the way")
                    .setX(5)
                    .setY(10)
                    .setSeqnum(1);

        UTruck truck = truckBuilder.build();

        // 序列化 UTruck 消息
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            truck.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] serializedData = outputStream.toByteArray();

        // 反序列化 UTruck 消息
        ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedData);
        try {
            UTruck deserializedTruck = UTruck.parseFrom(inputStream);
            System.out.println("Truck ID: " + deserializedTruck.getTruckid());
            System.out.println("Status: " + deserializedTruck.getStatus());
            System.out.println("X: " + deserializedTruck.getX());
            System.out.println("Y: " + deserializedTruck.getY());
            System.out.println("Sequence Number: " + deserializedTruck.getSeqnum());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}