package org.example;

import java.io.IOException;
import java.util.List;

import org.example.domain.UGoPickup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Helo world!");
        UpsServer upsServer = new UpsServer(9999);
        upsServer.connectToWorld();


        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        // 示例：插入一行到 UGoPickup 表中
        Transaction transaction = session.beginTransaction();

        UGoPickup uGoPickup = new UGoPickup();
        uGoPickup.setTruckId(1);
        uGoPickup.setWhId(2);
        uGoPickup.setSeqNum(1L);

        session.save(uGoPickup);
        transaction.commit();

        // 示例：根据 truckId 查询 UGoPickup 行
        Integer truckId = 1;
        List<UGoPickup> uGoPickups = session.createQuery("FROM UGoPickup WHERE truckId = :truckId", UGoPickup.class)
                .setParameter("truckId", truckId)
                .getResultList();
        for (UGoPickup uGoPickupResult : uGoPickups) {
            System.out.println("ID: " + uGoPickupResult.getId());
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