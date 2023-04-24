package org.example.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "u_go_deliver")
//it needs to setTruckId, setSeqNum, addUDeliveryLocation

// 在此处，您可以使用 JPA 持久化 uGoDeliver 实例，这将自动保存关联的 UDeliveryLocation 实例
// 例如：entityManager.persist(uGoDeliver);??????????
public class UGoDeliverD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "truck_id", nullable = false)
    private Integer truckId;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @OneToMany(mappedBy = "uGoDeliver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UDeliveryLocationD> packages = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<UDeliveryLocationD> getPackages() {
        return packages;
    }
    //useless
    public void setPackages(List<UDeliveryLocationD> packages) {
        this.packages = packages;
    }

    public void addUDeliveryLocation(UDeliveryLocationD uDeliveryLocation) {
        packages.add(uDeliveryLocation);
        uDeliveryLocation.setUGoDeliver(this);
    }

    public void removeUDeliveryLocation(UDeliveryLocationD uDeliveryLocation) {
        packages.remove(uDeliveryLocation);
        uDeliveryLocation.setUGoDeliver(null);
    }
}
