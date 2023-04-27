package org.example.world;

import java.net.Socket;
import java.util.List;

import org.example.domain.*;
import org.example.protoc.WorldUps.*;
import org.example.utils.CommHelper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class WorldListener implements Runnable {
    private Socket worldSocket;
    private SessionFactory sessionFactory;

    public WorldListener(Socket socket, SessionFactory sessionFactory) {
        this.worldSocket = socket;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void run() {
        // loop to listen for responses from world
        Session session = sessionFactory.openSession();
        while (true) {
            UResponses.Builder uResponses = UResponses.newBuilder();
            CommHelper.recvMSG(uResponses, worldSocket);

            System.out.println("Receive something!!!!!!!!!!!!!!!!!!1");
            System.out.println("completionList: "+uResponses.getCompletionsList());
            System.out.println("compldtion number:"+uResponses.getCompletionsCount());
            System.out.println("Ack:"+uResponses.getAcksList());
            System.out.println("Error: "+uResponses.getErrorList());

            Transaction transaction = session.beginTransaction();

            // 1. Deal UFinished
            for (UFinished uFinished : uResponses.getCompletionsList()) {

                // TODO: abstract to a funtion
                // 1.1 write seq nums into acks, resendacks
                // check whether in ack table
                Long seqNum = uFinished.getSeqnum();
                List<ACKsD> allAcks = session.createQuery("FROM ACKsD WHERE ack = :ack", ACKsD.class)
                        .setParameter("ack", seqNum).getResultList();
                List<ResendACKsD> allResendACKsDs = session
                        .createQuery("FROM ResendACKsD WHERE ack = :ack", ResendACKsD.class).setParameter("ack", seqNum)
                        .getResultList();

                // empty => if not in resend acks table, save it
                if (allResendACKsDs.isEmpty()) {
                    ResendACKsD resendACKsD = new ResendACKsD();
                    resendACKsD.setAck(seqNum);
                    session.save(resendACKsD);
                }
                // not empty => if already exists, do not handle again
                if (!allAcks.isEmpty()) {
                    break;
                }

                // save seq num into acks table
                ACKsD acKsD = new ACKsD();
                acKsD.setAck(seqNum);
                session.save(acKsD);

                // 1.2 update truck table
                int truckid = uFinished.getTruckid();
                TruckD truckD = session.createQuery("FROM TruckD WHERE truckId = :truckId", TruckD.class)
                        .setParameter("truckId", truckid).uniqueResult();
                System.out.println("---------------World Listener---------------------- ");
                System.out.println("[DEBUG] UFinished truck id: " + uFinished.getTruckid());
                System.out.println("[DEBUG] UFinished truck x: " + uFinished.getX());
                System.out.println("[DEBUG] UFinished truck y: " + uFinished.getY());
                System.out.println("[DEBUG] UFinished truck status: " + uFinished.getStatus());
                System.out.println("[DEBUG] UFinished truck seqnum: " + uFinished.getSeqnum());
                System.out.println("--------------------------------------------------- ");
                truckD.setStatus(uFinished.getStatus());
                truckD.setX(uFinished.getX());
                truckD.setY(uFinished.getY());
                session.save(truckD);
            }

            // 2. Deal UDeliveryMade
            for (UDeliveryMade uDeliveryMade : uResponses.getDeliveredList()) {
                // 2.1 write seq nums into acks, resendacks
                // check whether in ack table
                Long seqNum = uDeliveryMade.getSeqnum();
                List<ACKsD> allAcks = session.createQuery("FROM ACKsD WHERE ack = :ack", ACKsD.class)
                        .setParameter("ack", seqNum).getResultList();
                List<ResendACKsD> allResendACKsDs = session
                        .createQuery("FROM ResendACKsD WHERE ack = :ack", ResendACKsD.class).setParameter("ack", seqNum)
                        .getResultList();
                // if not in resend acks table, save it
                if (!allResendACKsDs.isEmpty()) {
                    ResendACKsD resendACKsD = new ResendACKsD();
                    resendACKsD.setAck(seqNum);
                    session.save(resendACKsD);
                }
                // if already exists, do not handle again
                if (!allAcks.isEmpty()) {
                    break;
                }

                // save seq num into acks table
                ACKsD acKsD = new ACKsD();
                acKsD.setAck(seqNum);
                session.save(acKsD);

                // 2.2 update package table
                long packageid = uDeliveryMade.getPackageid();
                PackageD packageD = session.createQuery("FROM PackageD WHERE packageId = :packageid", PackageD.class)
                        .setParameter("packageid", packageid).uniqueResult();
                packageD.setStatus("delivered");
                session.save(packageD);

                // 2.3 update truck table
                int truckid = uDeliveryMade.getTruckid();
                TruckD truckD = session.createQuery("FROM TruckD WHERE truckId = :truckId", TruckD.class)
                        .setParameter("truckId", truckid).uniqueResult();
                truckD.setStatus("delivering");
                truckD.setX(packageD.getDestinationX());
                truckD.setY(packageD.getDestinationY());
                session.save(truckD);
            }

            //3. delete the UGoPickupD and UGoDeliverD by ack in uResponses
            for(Long ack : uResponses.getAcksList()){
                UGoPickupD pickupToDelete = session.createQuery("FROM UGoPickupD WHERE seqNum = :seqNum", UGoPickupD.class)
                        .setParameter("seqNum", ack).uniqueResult();
                if (pickupToDelete != null) {
                    session.delete(pickupToDelete);
                }

                // Find and delete UGoDeliver record with the same seqnumber
                UGoDeliverD deliverToDelete = session.createQuery("FROM UGoDeliverD WHERE seqNum = :seqNum", UGoDeliverD.class)
                        .setParameter("seqNum", ack).uniqueResult();
                if (deliverToDelete != null) {
                    session.delete(deliverToDelete);
                }

            }

            transaction.commit();
            //session.close();
        }
    }
}
