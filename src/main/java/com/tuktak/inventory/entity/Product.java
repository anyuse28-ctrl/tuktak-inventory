package com.tuktak.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"products", "hibernateLazyInitializer", "handler"})
    private Category category;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"product", "hibernateLazyInitializer", "handler"})
    private Stock stock;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    @Builder.Default
    private List<PurchaseItem> purchaseItems = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "discount_percent")
    private Integer discountPercent = 0;  // ✅ added
}