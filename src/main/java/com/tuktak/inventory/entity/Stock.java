package com.tuktak.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"stock", "purchaseItems", "orderItems", "hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable = 0;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel = 1000;

    @Column(name = "reorder_point")
    private Integer reorderPoint = 20;

    public void increaseStock(Integer quantity) {
        this.quantityAvailable += quantity;
    }

    public void decreaseStock(Integer quantity) {
        if (this.quantityAvailable < quantity) {
            throw new IllegalStateException("Insufficient stock. Available: " + this.quantityAvailable + ", Requested: " + quantity);
        }
        this.quantityAvailable -= quantity;
    }

    public boolean hasEnoughStock(Integer quantity) {
        return this.quantityAvailable >= quantity;
    }
}
