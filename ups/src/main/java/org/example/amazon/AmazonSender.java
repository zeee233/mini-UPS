package org.example.amazon;

import org.example.domain.PackageD;
import org.example.domain.TruckD;
import org.example.protoc.UpsAmazon;
import org.example.utils.CommHelper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.example.protoc.UpsAmazon.*;
import org.hibernate.Transaction;

import java.net.Socket;
import java.util.List;

public class AmazonSender implements Runnable {
    private Socket amazonSocket;
    SessionFactory sessionFactory;

    public AmazonSender(Socket socket, SessionFactory sessionFac) {
        amazonSocket = socket;
        sessionFactory = sessionFac;
    }

    @Override
    public void run() {
        Session session = sessionFactory.openSession();
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            UACommunication.Builder uaCommunication = UACommunication.newBuilder();
            // CommHelper.recvMSG(uaCommunication,amazonSocket);
            Transaction transaction = session.beginTransaction();
            List<TruckD> trucksArrived = session
                    .createQuery("FROM TruckD WHERE status = 'arrive warehouse'", TruckD.class)
                    .getResultList();
            if (trucksArrived.size() != 0) {
                System.out.println("---------------UTruck Arrived---------------------");
                System.out.println("arrived trucks list: " + trucksArrived);
            }
            for (TruckD truck : trucksArrived) {
                // Find packages with status "packed" and the truckId of the current truck
                List<PackageD> packagesToLoad = session
                        .createQuery("FROM PackageD WHERE status = 'packed' AND truckId = :truckId", PackageD.class)
                        .setParameter("truckId", truck.getTruckId())
                        .getResultList();

                // Update the status of the truck to "loading" and packages' status to "loading"
                // System.out.println("arrived truck: " + truck);
                truck.setStatus("loading");
                session.save(truck);

                for (PackageD packageD : packagesToLoad) {
                    packageD.setStatus("loading");
                    session.save(packageD);

                    // Create and store a UTruckArrived message for the current package
                    // todo: may need to add the sequencenumber
                    UTruckArrived.Builder uTruckArrived = UTruckArrived.newBuilder()
                            .setPackageid(packageD.getPackageId())
                            .setTruckid(truck.getTruckId());
                    // if (packageD.getSeqNum() != null) {
                    // uTruckArrived.setSeqnum(packageD.getSeqNum());
                    // }
                    // Add the UTruckArrived message to the UACommunication builder
                    uaCommunication.addArrived(uTruckArrived);
                    System.out.println("package in this truck: " + packageD);

                    System.out.println("---------------Utruck Arrived for one package--------------------- ");
                    System.out.println("[DEBUG] uTruckArrived truck id: " + uTruckArrived.getTruckid());
                    System.out.println("[DEBUG] uTruckArrived package id: " + uTruckArrived.getPackageid());
                    System.out.println("--------------------------------------------------- ");
                }

                // transaction.commit();
            }
            
            List<PackageD> deliveredPackages = session
                    .createQuery("FROM PackageD WHERE status = 'delivered'", PackageD.class)
                    .getResultList();
                    if (deliveredPackages.size()!=0){
                    System.out.println("-----------------U Delievered------------------------------");}
            // Create and store UDelivered messages for the delivered packages
            for (PackageD packageD : deliveredPackages) {
                UDelivered.Builder uDelivered = UDelivered.newBuilder()
                        .setPackageid(packageD.getPackageId());
                packageD.setStatus("finished");
                session.save(packageD);
                // if (packageD.getSeqNum() != null) {
                // uDelivered.setSeqnum(packageD.getSeqNum());
                // }
                // Add the UDelivered message to the UACommunication builder
                uaCommunication.addDelivered(uDelivered);

                System.out.println("---------------U Delievered for one package---------------------- ");
                System.out.println("[DEBUG] uDelivered package id: " + uDelivered.getPackageid());
                System.out.println("--------------------------------------------------- ");
            }
            if (uaCommunication.getDeliveredCount() <= 0 && uaCommunication.getArrivedCount() <= 0) {
                transaction.commit();// useless
                continue;
            }
            // System.out.println("arrived truck sended: " + uaCommunication.getArrivedList());
            CommHelper.sendMSG(uaCommunication, amazonSocket);
            transaction.commit();
            // session.close();
        }

    }
}
