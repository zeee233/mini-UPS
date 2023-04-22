package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Helo world!");
        UpsServer upsServer = new UpsServer(9999);
        upsServer.connectToWorld();
    }
}