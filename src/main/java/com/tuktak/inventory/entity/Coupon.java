package com.tuktak.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer usageLimit = 0; // 0 = unlimited

    @Column(nullable = false)
    private Integer usageCount = 0;

    public enum DiscountType {
        PERCENTAGE,  // e.g. 10% off
        FIXED        // e.g. ৳50 off
    }

    public boolean isValid() {
        return active &&
                LocalDateTime.now().isBefore(expiryDate) &&
                (usageLimit == 0 || usageCount < usageLimit);
    }
}