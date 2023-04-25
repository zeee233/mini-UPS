package org.example.amazon;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.net.Socket;

public class AmazonSender implements Runnable {
    private Socket amazonSocket;
    SessionFactory sessionFactory;
    public AmazonSender(Socket socket, SessionFactory sessionFac) {
        amazonSocket = socket;
        sessionFactory =sessionFac;
    }
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Session session = sessionFactory.openSession();




        }

    }

}
