package org.example.amazon;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.example.domain.*;
import org.example.protoc.UpsAmazon.*;
import org.hibernate.Transaction;
import org.example.utils.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class AmazonListener implements Runnable {
    private Socket amazonSocket;
    SessionFactory sessionFactory;
    public AmazonListener(Socket socket, SessionFactory sessionFac) {
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
            // receive response from amazon every 1s
            AUCommunication.Builder auCommunication = AUCommunication.newBuilder();
            CommHelper.recvMSG(auCommunication, amazonSocket);
            // Parse the fields in AUCommunication
            List<ABookTruck> bookTruckList = auCommunication.getBookingsList();
            List<AStartDeliver > startDelieverList = auCommunication.getDeliversList();
            //TODO: have ack to deal with
            for (ABookTruck bookTruck : bookTruckList) {
                long packageId = bookTruck.getPackageid();
                int warehouseId = bookTruck.getWarehouseid();
                int warehouseX = bookTruck.getWarehousex();
                int warehouseY = bookTruck.getWarehousey();
                int destinationX = bookTruck.getDestinationx();
                int destinationY = bookTruck.getDestinationy();
                String upsid = null;
                if (bookTruck.hasUpsid()) {
                    upsid = bookTruck.getUpsid();
                }
                Long seqnum = null;
                if (bookTruck.hasSeqnum()) {
                    seqnum = bookTruck.getSeqnum();
                }
                //Todo: add the detail field
                //String detail=null;
                //if (bookTruck.hasSeqnum()) {
                //    detail = bookTruck.;
                //}

                // Process the fields as needed
                // Query for all trucks with "idle" or "delivering" status
                // todo: There not be any availble trucks
                List<TruckD> trucks = session.createQuery("FROM TruckD WHERE status IN ('idle', 'delivering')", TruckD.class)
                        .getResultList();
                long minDistance = Long.MAX_VALUE;
                TruckD closestTruck = null;
                for (TruckD truck : trucks) {
                    int xDiff = truck.getX() - warehouseX;
                    int yDiff = truck.getY() - warehouseY;
                    long distance = (long)xDiff * xDiff + (long)yDiff * yDiff;

                    if (distance < minDistance) {
                        minDistance = distance;
                        closestTruck = truck;
                    }
                }
                //insert the package table
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
                //if has error, you need to delete the truckID with status "packing"
                newPackage.setStatus("packed");
                Transaction transaction1 = session.beginTransaction();
                session.save(newPackage);
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
            // Iterate through startDeliverList and access the fields
            for (AStartDeliver startDeliver : startDelieverList) {
                long packageId = startDeliver.getPackageid();
                long seqnum;
                if (startDeliver.hasSeqnum()){
                    seqnum= startDeliver.getSeqnum();}

                // select package
                PackageD packageD = session.createQuery("FROM PackageD WHERE packageId = :packageId", PackageD.class)
                        .setParameter("packageId", packageId)
                        .uniqueResult();

                //insert the UGoDeliverD
                UGoDeliverD uGoDeliverD = new UGoDeliverD();
                uGoDeliverD.setTruckId(packageD.getTruckId());

                uGoDeliverD.setSeqNum(SeqNumGenerator.generateSeqNum());


                UDeliveryLocationD uDeliveryLocationD = new UDeliveryLocationD();
                uDeliveryLocationD.setPackageId(packageId);
                uDeliveryLocationD.setX(packageD.getDestinationX());
                uDeliveryLocationD.setY(packageD.getDestinationY());

                //add the subclass into this parent class
                uGoDeliverD.addUDeliveryLocation(uDeliveryLocationD);

                Transaction transaction = session.beginTransaction();
                session.save(uGoDeliverD);
                transaction.commit();

                // update the Truck
                TruckD truck = session.get(TruckD.class, uGoDeliverD.getTruckId());
                truck.setStatus("delivering");
                session.save(truck);

                // update the package, when UGoDeliever send, update the status to delievering
                packageD.setStatus("loaded");
                session.save(packageD);

                transaction.commit();
            }
            session.close();
        }
    }
}
