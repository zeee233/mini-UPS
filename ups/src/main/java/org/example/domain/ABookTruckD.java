package org.example.domain;

import javax.persistence.*;

@Entity
@Table(name = "a_book_truck")
public class ABookTruckD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Integer getWarehouseX() {
        return warehouseX;
    }

    public void setWarehouseX(Integer warehouseX) {
        this.warehouseX = warehouseX;
    }

    public Integer getWarehouseY() {
        return warehouseY;
    }

    public void setWarehouseY(Integer warehouseY) {
        this.warehouseY = warehouseY;
    }

    public Integer getDestinationX() {
        return destinationX;
    }

    public void setDestinationX(Integer destinationX) {
        this.destinationX = destinationX;
    }

    public Integer getDestinationY() {
        return destinationY;
    }

    public void setDestinationY(Integer destinationY) {
        this.destinationY = destinationY;
    }

    public String getUpsId() {
        return upsId;
    }

    public void setUpsId(String upsId) {
        this.upsId = upsId;
    }

    public Long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(Long seqNum) {
        this.seqNum = seqNum;
    }

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    @Column(name = "warehouse_x", nullable = false)
    private Integer warehouseX;

    @Column(name = "warehouse_y", nullable = false)
    private Integer warehouseY;

    @Column(name = "destination_x", nullable = false)
    private Integer destinationX;

    @Column(name = "destination_y", nullable = false)
    private Integer destinationY;

    @Column(name = "ups_id")
    private String upsId;

    @Column(name = "seq_num")
    private Long seqNum;


}
