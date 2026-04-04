package com.tuktak.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase extends BaseEntity {

    @Column(name = "purchase_number", nullable = false, unique = true)
    private String purchaseNumber;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "supplier_contact")
    private String supplierContact;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus status = PurchaseStatus.PENDING;

    private String notes;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"purchase", "hibernateLazyInitializer", "handler"})
    @Builder.Default
    private List<PurchaseItem> purchaseItems = new ArrayList<>();

    public enum PurchaseStatus {
        PENDING,
        RECEIVED,
        CANCELLED
    }

    public void addPurchaseItem(PurchaseItem item) {
        purchaseItems.add(item);
        item.setPurchase(this);
    }

    public void removePurchaseItem(PurchaseItem item) {
        purchaseItems.remove(item);
        item.setPurchase(null);
    }
}
