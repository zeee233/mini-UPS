package org.example.domain;
import javax.persistence.*;

@Entity
@Table(name = "acks")
public class ACKs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ackId;

    @Column(name = "ack", nullable = false)
    private Integer ack;
    // Getters and setters


    public Integer getAck() {
        return ack;
    }

    public void setAck(Integer ack) {
        this.ack = ack;
    }

}
