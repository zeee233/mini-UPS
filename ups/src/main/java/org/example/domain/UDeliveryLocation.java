package org.example.domain;

import javax.persistence.*;

@Entity
@Table(name = "u_delivery_location")
// it needs to setPackageId, setX, setY, setUGoDeliver
public class UDeliveryLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "x", nullable = false)
    private Integer x;

    @Column(name = "y", nullable = false)
    private Integer y;

    //'Lazy' means when UDeliveryLocation is loaded, the UGoDeliver
    // will not be loaded until UGoDeliver is visited, to improve the performance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_go_deliver_id", nullable = false)
    private UGoDeliver uGoDeliver;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public UGoDeliver getUGoDeliver() {
        return uGoDeliver;
    }

    public void setUGoDeliver(UGoDeliver uGoDeliver) {
        this.uGoDeliver = uGoDeliver;
    }
}
