package com.tuktak.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    private Long totalProducts;
    private Long totalStock;
    private Long totalOrders;
    private BigDecimal inventoryValue;
    private Long lowStockCount;
    private Long outOfStockCount;
    private List<StockDto> lowStockItems;
    private List<StockDto> outOfStockItems;
}
