package org.example;

import java.io.IOException;
import java.util.List;

import org.example.domain.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Main {
    public static void main(String[] args) throws IOException {
        MockAmazon mockAmazon = new MockAmazon("152.3.53.142", 9999);
        mockAmazon.connectToWorld(mockAmazon.createUConnect(null, 10));
        mockAmazon.sendAInformWorld();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mockAmazon.sendABookTruck();
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (true) {
        }
        // mockAmazon.receiveUTruckArrived();
    }
}