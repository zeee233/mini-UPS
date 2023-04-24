package org.example.domain;

import javax.persistence.*;

@Entity
@Table(name = "u_go_pickup")
public class UGoPickupD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "truck_id", nullable = false)
    private Integer truckId;

    @Column(name = "wh_id", nullable = false)
    private Integer whId;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    // Getters and setters

    public Integer getTruckId() {
        return truckId;
    }

    public void setTruckId(Integer truckId) {
        this.truckId = truckId;
    }

    public Integer getWhId() {
        return whId;
    }

    public void setWhId(Integer whId) {
        this.whId = whId;
    }

    public Long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(Long seqNum) {
        this.seqNum = seqNum;
    }
}
