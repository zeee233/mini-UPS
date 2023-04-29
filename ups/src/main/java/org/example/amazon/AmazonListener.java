package org.example.amazon;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.domain.*;
import org.example.protoc.UpsAmazon.*;
import org.hibernate.Transaction;

import com.google.protobuf.Parser;

import org.example.utils.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class AmazonListener implements Runnable {
    private Socket amazonSocket;
    SessionFactory sessionFactory;

    public AmazonListener(Socket socket, SessionFactory sessionFac) {
        amazonSocket = socket;
        sessionFactory = sessionFac;
    }

    @Override
    public void run() {
        Session session = sessionFactory.openSession();
        while (true) {
            // receive response from amazon every 1s
            AUCommunication.Builder auCommunication = AUCommunication.newBuilder();
            CommHelper.recvMSG(auCommunication, amazonSocket);
            // Parser<AUCommunication> parser = AUCommunication.parser();
            // CommHelper.recvMSG2(auCommunication, amazonSocket, parser);
            // AUCommunication.Builder auCommunication = AUCommunication.newBuilder();

            // Parse the fields in AUCommunication
            List<ABookTruck> bookTruckList = auCommunication.getBookingsList();
            List<AStartDeliver> startDelieverList = auCommunication.getDeliversList();
            if (bookTruckList.size() != 0 || startDelieverList.size() != 0) {
            }
            System.out.println("recv one time AU communication");

            // TODO: have ack to deal with
            if (bookTruckList.size() != 0) {
                System.out.println("----------ABookTruck----------------");
            }
            for (ABookTruck bookTruck : bookTruckList) {
                long packageId = bookTruck.getPackageid();
                int warehouseId = bookTruck.getWarehouseid();
                int warehouseX = bookTruck.getWarehousex();
                int warehouseY = bookTruck.getWarehousey();
                int destinationX = bookTruck.getDestinationx();
                int destinationY = bookTruck.getDestinationy();
                String upsid = null;

                System.out.println("[DEBUG] deal with ABookTruck");
                System.out.println("[DEBUG] ABookTruck packageId: " + packageId);
                System.out.println("[DEBUG] ABookTruck warehouseId: " + warehouseId);
                System.out.println("[DEBUG] ABookTruck warehouseX: " + warehouseX);
                System.out.println("[DEBUG] ABookTruck warehouseY: " + warehouseY);
                System.out.println("[DEBUG] ABookTruck destinationX: " + destinationX);
                System.out.println("[DEBUG] ABookTruck destinationY: " + destinationY);

                if (bookTruck.hasUpsid()) {
                    upsid = bookTruck.getUpsid();
                }
                Long seqnum = null;
                if (bookTruck.hasSeqnum()) {
                    seqnum = bookTruck.getSeqnum();
                }

                String detail = null;
                if (bookTruck.hasSeqnum()) {
                    detail = bookTruck.getDetail();
                }

                // Process the fields as needed
                // Query for all trucks with "idle" or "delivering" status
                // todo: There not be any availble trucks
                List<TruckD> trucks = session
                        .createQuery("FROM TruckD WHERE status IN ('idle', 'delivering','arrive warehouse')",
                                TruckD.class)
                        .getResultList();
                long minDistance = Long.MAX_VALUE;
                TruckD closestTruck = null;
                for (TruckD truck : trucks) {
                    int xDiff = truck.getX() - warehouseX;
                    int yDiff = truck.getY() - warehouseY;
                    long distance = (long) xDiff * xDiff + (long) yDiff * yDiff;

                    if (distance < minDistance) {
                        minDistance = distance;
                        closestTruck = truck;
                    }
                }
                // insert the package table
                PackageD newPackage = new PackageD();
                newPackage.setPackageId(packageId);
                newPackage.setWarehouseId(warehouseId);
                newPackage.setWarehouseX(warehouseX);
                newPackage.setWarehouseY(warehouseY);
                newPackage.setDestinationX(destinationX);
                newPackage.setDestinationY(destinationY);
                newPackage.setTruckId(closestTruck.getTruckId());
                if (upsid != null) {
                    newPackage.setUpsId(upsid);
                }
                if (detail != null) {
                    newPackage.setDetail(detail);
                }
                // if has error, you need to delete the truckID with status "packing"
                newPackage.setStatus("packed");
                Transaction transaction1 = session.beginTransaction();
                session.save(newPackage);
                closestTruck.setStatus("traveling");
                session.save(closestTruck);
                transaction1.commit();

                // insert the uGoPickup table
                UGoPickupD newGoPickup = new UGoPickupD();
                // setTruck ID、WhId、 sequenceNumber
                newGoPickup.setTruckId(closestTruck.getTruckId());
                newGoPickup.setWhId(warehouseId);

                newGoPickup.setSeqNum(SeqNumGenerator.generateSeqNum());

                Transaction transaction2 = session.beginTransaction();
                session.save(newGoPickup);
                transaction2.commit();

            }

            // ***********************************
            // handle the AStartDeliever part
            // ***********************************
            // Create a HashMap to store the packages with the same truckId
            Map<Integer, List<PackageD>> truckPackagesMap = new HashMap<>();

            // Group the packages by truckId in the HashMap
            if (startDelieverList.size() != 0) {
                System.out.println("----------Astart Deliever----------------");
            }

            for (AStartDeliver startDeliver : startDelieverList) {
                long packageId = startDeliver.getPackageid();
                // System.out.println("Package ID List: "+startDeliver.getPackageid());
                // Select package
                PackageD packageD = session.createQuery("FROM PackageD WHERE packageId = :packageId", PackageD.class)
                        .setParameter("packageId", packageId)
                        .uniqueResult();

                int truckId = packageD.getTruckId();
                truckPackagesMap.computeIfAbsent(truckId, k -> new ArrayList<>()).add(packageD);

                System.out.println("[DEBUG] AStartDeliver packageId: " + packageId);
            }

            // Iterate through the HashMap and create a UGoDeliverD for each truckId
            for (Map.Entry<Integer, List<PackageD>> entry : truckPackagesMap.entrySet()) {
                int truckId = entry.getKey();
                List<PackageD> packages = entry.getValue();

                // Create UGoDeliverD
                UGoDeliverD uGoDeliverD = new UGoDeliverD();
                uGoDeliverD.setTruckId(truckId);
                uGoDeliverD.setSeqNum(SeqNumGenerator.generateSeqNum());

                Transaction transaction = session.beginTransaction();

                // Iterate through the packages and add the corresponding UDeliveryLocationD
                for (PackageD packageD : packages) {
                    UDeliveryLocationD uDeliveryLocationD = new UDeliveryLocationD();
                    uDeliveryLocationD.setPackageId(packageD.getPackageId());
                    uDeliveryLocationD.setX(packageD.getDestinationX());
                    uDeliveryLocationD.setY(packageD.getDestinationY());

                    // Add the subclass into this parent class
                    uGoDeliverD.addUDeliveryLocation(uDeliveryLocationD);

                    // Update the package status
                    packageD.setStatus("loaded");
                    session.save(packageD);
                }

                session.save(uGoDeliverD);

                // Update the truck status
                // TruckD truck = session.get(TruckD.class, truckId);
                TruckD truck = session.createQuery("FROM TruckD WHERE truckId = :truckId", TruckD.class)
                        .setParameter("truckId", truckId).uniqueResult();
                truck.setStatus("arrive warehouse");
                session.save(truck);

                transaction.commit();
            }

            // session.close();
        }
    }
}
