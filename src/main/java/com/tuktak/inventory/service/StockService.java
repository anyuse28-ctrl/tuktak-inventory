package com.tuktak.inventory.service;

import com.tuktak.inventory.dto.StockDto;
import com.tuktak.inventory.entity.Product;
import com.tuktak.inventory.entity.Stock;
import com.tuktak.inventory.repository.ProductRepository;
import com.tuktak.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<StockDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockDto getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + id));
        return mapToDto(stock);
    }

    @Transactional(readOnly = true)
    public StockDto getStockByProductId(Long productId) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product id: " + productId));
        return mapToDto(stock);
    }

    @Transactional(readOnly = true)
    public List<StockDto> getLowStockItems() {
        return stockRepository.findLowStockItems().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockDto> getItemsNeedingReorder() {
        return stockRepository.findItemsNeedingReorder().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockDto> getOutOfStockItems() {
        return stockRepository.findOutOfStockItems().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockDto updateStock(Long id, StockDto stockDto) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + id));

        if (stockDto.getQuantityAvailable() != null) {
            stock.setQuantityAvailable(stockDto.getQuantityAvailable());
        }
        if (stockDto.getMinStockLevel() != null) {
            stock.setMinStockLevel(stockDto.getMinStockLevel());
        }
        if (stockDto.getMaxStockLevel() != null) {
            stock.setMaxStockLevel(stockDto.getMaxStockLevel());
        }
        if (stockDto.getReorderPoint() != null) {
            stock.setReorderPoint(stockDto.getReorderPoint());
        }

        Stock updatedStock = stockRepository.save(stock);
        log.info("Stock updated for product: {}, new quantity: {}", 
                updatedStock.getProduct().getName(), updatedStock.getQuantityAvailable());
        return mapToDto(updatedStock);
    }

    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProductIdWithLock(productId)
                .orElseGet(() -> {
                    // Auto-create stock record if it doesn't exist
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
                    log.info("No stock record found for product: {}. Creating one.", product.getName());
                    Stock newStock = Stock.builder()
                            .product(product)
                            .quantityAvailable(0)
                            .minStockLevel(10)
                            .maxStockLevel(1000)
                            .reorderPoint(20)
                            .build();
                    return stockRepository.save(newStock);
                });

        stock.increaseStock(quantity);
        stockRepository.save(stock);
        log.info("Stock increased for product: {}, added: {}, new total: {}",
                stock.getProduct().getName(), quantity, stock.getQuantityAvailable());
    }

    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product id: " + productId));

        stock.decreaseStock(quantity);
        stockRepository.save(stock);
        log.info("Stock decreased for product: {}, removed: {}, new total: {}", 
                stock.getProduct().getName(), quantity, stock.getQuantityAvailable());
    }

    @Transactional(readOnly = true)
    public boolean validateStockAvailability(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product id: " + productId));
        return stock.hasEnoughStock(quantity);
    }

    @Transactional
    public StockDto createStockForProduct(Long productId) {
        if (stockRepository.existsByProductId(productId)) {
            throw new IllegalArgumentException("Stock already exists for product id: " + productId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        Stock stock = Stock.builder()
                .product(product)
                .quantityAvailable(0)
                .minStockLevel(10)
                .maxStockLevel(1000)
                .reorderPoint(20)
                .build();

        Stock savedStock = stockRepository.save(stock);
        log.info("Stock created for product: {}", product.getName());
        return mapToDto(savedStock);
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
