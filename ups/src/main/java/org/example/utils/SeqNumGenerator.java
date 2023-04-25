package org.example.utils;

public class SeqNumGenerator {
    public static long seqNum = 0;

    public static synchronized Long generateSeqNum(){
        ++seqNum;
        return seqNum;
    }
}
