package org.example.world;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.protoc.WorldUps.*;
import org.example.utils.CommHelper;
import org.example.utils.SeqNumGenerator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
            List<UGoPickupD> allUGoPickups = session.createQuery("FROM UGoPickupD", UGoPickupD.class).getResultList();
            // for every record in UGoPickUp table, make it as uGoPickUp and add it to
            // uCommands
            for (UGoPickupD pickUpRequest : allUGoPickups) {
                UGoPickup.Builder uGoPickUp = UGoPickup.newBuilder();
                uGoPickUp.setTruckid(pickUpRequest.getTruckId()).setWhid(pickUpRequest.getWhId())
                        .setSeqnum(pickUpRequest.getSeqNum());
                uCommands.addPickups(uGoPickUp);
            }

            // then search UGoDeliver
            List<UGoDeliverD> allUGoDelivers = session.createQuery("FROM UGoDeliverD", UGoDeliverD.class)
                    .getResultList();
            // for every record in UGoDeliver table, make it as UGoDeliver and add it to
            // uCommands
            for (UGoDeliverD deliverRequest : allUGoDelivers) {
                UGoDeliver.Builder uGoDeliver = UGoDeliver.newBuilder();
                for (UDeliveryLocationD location : deliverRequest.getPackages()) {
                    UDeliveryLocation.Builder uLocation = UDeliveryLocation.newBuilder();
                    uLocation.setPackageid(location.getPackageId()).setX(location.getX()).setY(location.getY());
                    uGoDeliver.addPackages(uLocation);
                }
                uGoDeliver.setTruckid(deliverRequest.getTruckId()).setSeqnum(deliverRequest.getSeqNum());
                uCommands.addDeliveries(uGoDeliver);
            }

            // add acks from Resend table
            List<ResendACKsD> allResendAcks = session.createQuery("FROM ResendACKsD", ResendACKsD.class)
                    .getResultList();

            // TODO: change ack to long!!!
            Iterable<Long> allAcks = allResendAcks.stream().map(ResendACKsD::getAck).collect(Collectors.toList());
            uCommands.addAllAcks(allAcks);

            // send uCommands to world
            CommHelper.sendMSG(uCommands, worldSocket);

            // TODO: maybe move to other file to only deal with table
            // delete acks from table
            Transaction transaction = session.beginTransaction();
            for (ResendACKsD record : allResendAcks) {
                session.delete(record);
            }
            transaction.commit();

            // TODO: optional: UQuery??

            session.close();
        }
    }
}
