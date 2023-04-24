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
        //UpsServer upsServer = new UpsServer(9999);
        //upsServer.connectToWorld();

        //sudo su - postgres
        // psql
        // ALTER USER postgres with encrypted password 'abc123';

        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        // Step 1: Clear data from all tables
        Transaction clearTransaction = session.beginTransaction();
        session.createNativeQuery("DELETE FROM u_delivery_location").executeUpdate();
        session.createNativeQuery("DELETE FROM u_go_deliver").executeUpdate();
        session.createNativeQuery("DELETE FROM u_go_pickup").executeUpdate();
        session.createNativeQuery("DELETE FROM u_query").executeUpdate();
        session.createNativeQuery("DELETE FROM a_book_truck").executeUpdate();
        session.createNativeQuery("DELETE FROM acks").executeUpdate();
        session.createNativeQuery("DELETE FROM a_start_deliver").executeUpdate();
        session.createNativeQuery("DELETE FROM package").executeUpdate();
        session.createNativeQuery("DELETE FROM truck").executeUpdate();
        clearTransaction.commit();

        // Step 2: Execute your program logic
        // 示例：插入一行到 UGoPickup 表中
        Transaction transaction = session.beginTransaction();

        UGoPickupD uGoPickup = new UGoPickupD();
        uGoPickup.setTruckId(1);
        uGoPickup.setWhId(2);
        uGoPickup.setSeqNum(1L);

        session.save(uGoPickup);
        transaction.commit();

        // 示例：根据 truckId 查询 UGoPickup 行
        Integer truckId = 1;
        List<UGoPickupD> uGoPickups = session.createQuery("FROM UGoPickup WHERE truckId = :truckId", UGoPickupD.class)
                .setParameter("truckId", truckId)
                .getResultList();
        for (UGoPickupD uGoPickupResult : uGoPickups) {
            System.out.println("Truck ID: " + uGoPickupResult.getTruckId());
            System.out.println("Warehouse ID: " + uGoPickupResult.getWhId());
            System.out.println("Sequence Number: " + uGoPickupResult.getSeqNum());
            System.out.println();
        }

        // 关闭 session 和 sessionFactory
        session.close();
        sessionFactory.close();

    }
}