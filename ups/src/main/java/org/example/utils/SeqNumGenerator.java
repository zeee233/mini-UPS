package org.example.utils;

import org.example.domain.ResendACKsD;
import org.example.domain.SeqNumD;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.validation.constraints.Null;
import java.util.List;


public class SeqNumGenerator {
    public static long seqNum = 0;

    public static synchronized Long generateSeqNum(SessionFactory sessionFactory){
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        boolean seqNumExists;
        do{
            ++seqNum;
            List<SeqNumD> sql_res=session.createQuery("FROM SeqNumD WHERE seqNum = :seqNum", SeqNumD.class).setParameter("seqNum", seqNum).getResultList();
            seqNumExists=(sql_res.size()!=0);
        }while(seqNumExists);
        SeqNumD newSeqNumD = new SeqNumD();
        newSeqNumD.setSeqNum(seqNum);
        session.save(newSeqNumD);
        transaction.commit();
        return seqNum;
    }
}
