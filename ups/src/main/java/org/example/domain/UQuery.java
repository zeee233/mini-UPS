package org.example.domain;

import javax.persistence.*;

@Entity
@Table(name = "u_query")
public class UQuery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "truck_id", nullable = false)
    private Integer truckId;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    // Getters and setters
    public Integer getTruckId() {
        return truckId;
    }

    public void setTruckId(Integer truckId) {
        this.truckId = truckId;
    }

    public Long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(Long seqNum) {
        this.seqNum = seqNum;
    }
}
