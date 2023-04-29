package org.example.domain;

import javax.persistence.*;

@Entity
@Table(name = "seqnum")
public class SeqNumD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(Long seqNum) {
        this.seqNum = seqNum;
    }

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;


}
