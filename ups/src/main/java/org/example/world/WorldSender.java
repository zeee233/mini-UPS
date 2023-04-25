package org.example.world;

import java.net.Socket;
import java.util.List;

import org.example.protoc.WorldUps.*;
import org.example.utils.SeqNumGenerator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.example.domain.*;



public class WorldSender implements Runnable {
    private Socket worldSocket;
    private SessionFactory sessionFactory;

    public WorldSender(Socket socket, SessionFactory sessionFactory) {
        this.worldSocket = socket;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // create new Ucommands
            UCommands.Builder uCommands = UCommands.newBuilder();

            Session session = sessionFactory.openSession();
            // first search UGoPickup
            List<UGoPickupD> allUGoPickups = session.createQuery("FROM UGoPickup", UGoPickupD.class).getResultList();
            for(UGoPickupD pickUpRequest: allUGoPickups){
                UGoPickup.Builder uGoPickUp = UGoPickup.newBuilder();
                uGoPickUp.setTruckid(pickUpRequest.getTruckId()).setWhid(pickUpRequest.getWhId()).setSeqnum(SeqNumGenerator.generateSeqNum());
            }

            // Fetch all rows from the UGoPickup table 
            // List<UGoPickup> allUGoPickups =
            // session.createQuery("FROM UGoPickup", UGoPickup.class).getResultList(); //
            // Print each UGoPickup tuple for (UGoPickup uGoPickupResult : allUGoPickups) {
            // System.out.println("ID: " + uGoPickupResult.getId());
            // System.out.println("Truck ID: " + uGoPickupResult.getTruckId());
            // System.out.println("Warehouse ID: " + uGoPickupResult.getWhId());
            // System.out.println("Sequence Number: " + uGoPickupResult.getSeqNum());
            // System.out.println(); }

            // then search UGoDeliver

            // optional: UQuery??

            session.close();
        }
    }
}
