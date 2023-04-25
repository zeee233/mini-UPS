package org.example.domain;
import javax.persistence.*;

@Entity
@Table(name = "acks")
public class ACKsD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ackId;

    @Column(name = "ack", nullable = false)
    private Long ack;
    // Getters and setters


    public Long getAck() {
        return ack;
    }

    public void setAck(Long ack) {
        this.ack = ack;
    }

}
