package com.tuktak.inventory.service;

import com.tuktak.inventory.dto.DashboardDto;
import com.tuktak.inventory.dto.StockDto;
import com.tuktak.inventory.entity.Stock;
import com.tuktak.inventory.repository.OrderRepository;
import com.tuktak.inventory.repository.ProductRepository;
import com.tuktak.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public DashboardDto getDashboardSummary() {
        // Total products
        long totalProducts = productRepository.count();

        // Total stock (sum of all quantityAvailable)
        List<Stock> allStocks = stockRepository.findAll();
        long totalStock = allStocks.stream()
                .mapToLong(Stock::getQuantityAvailable)
                .sum();

        // Total orders
        long totalOrders = orderRepository.count();

        // Inventory value (sum of price * quantityAvailable)
        BigDecimal inventoryValue = allStocks.stream()
                .map(stock -> stock.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(stock.getQuantityAvailable())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Low stock items
        List<Stock> lowStockItems = stockRepository.findLowStockItems();
        List<StockDto> lowStockDtos = lowStockItems.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // Out of stock items
        List<Stock> outOfStockItems = stockRepository.findOutOfStockItems();
        List<StockDto> outOfStockDtos = outOfStockItems.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        log.info("Dashboard summary generated - Products: {}, Stock: {}, Orders: {}, Value: {}",
                totalProducts, totalStock, totalOrders, inventoryValue);

        return DashboardDto.builder()
                .totalProducts(totalProducts)
                .totalStock(totalStock)
                .totalOrders(totalOrders)
                .inventoryValue(inventoryValue)
                .lowStockCount((long) lowStockItems.size())
                .outOfStockCount((long) outOfStockItems.size())
                .lowStockItems(lowStockDtos)
                .outOfStockItems(outOfStockDtos)
                .build();
    }

    private StockDto mapToDto(Stock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .productId(stock.getProduct().getId())
                .productName(stock.getProduct().getName())
                .productSku(stock.getProduct().getSku())
                .quantityAvailable(stock.getQuantityAvailable())
                .minStockLevel(stock.getMinStockLevel())
                .maxStockLevel(stock.getMaxStockLevel())
                .reorderPoint(stock.getReorderPoint())
                .build();
    }
}
