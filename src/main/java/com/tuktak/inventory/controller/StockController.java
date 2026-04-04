package com.tuktak.inventory.controller;

import com.tuktak.inventory.dto.ApiResponse;
import com.tuktak.inventory.dto.StockDto;
import com.tuktak.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockDto>>> getAllStocks() {
        List<StockDto> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(ApiResponse.success(stocks));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockDto>> getStockById(@PathVariable Long id) {
        StockDto stock = stockService.getStockById(id);
        return ResponseEntity.ok(ApiResponse.success(stock));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<StockDto>> getStockByProductId(@PathVariable Long productId) {
        StockDto stock = stockService.getStockByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(stock));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<StockDto>>> getLowStockItems() {
        List<StockDto> stocks = stockService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success(stocks));
    }

    @GetMapping("/reorder")
    public ResponseEntity<ApiResponse<List<StockDto>>> getItemsNeedingReorder() {
        List<StockDto> stocks = stockService.getItemsNeedingReorder();
        return ResponseEntity.ok(ApiResponse.success(stocks));
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<List<StockDto>>> getOutOfStockItems() {
        List<StockDto> stocks = stockService.getOutOfStockItems();
        return ResponseEntity.ok(ApiResponse.success(stocks));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StockDto>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockDto stockDto) {
        StockDto updatedStock = stockService.updateStock(id, stockDto);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", updatedStock));
    }

    @PatchMapping("/product/{productId}/add")
    public ResponseEntity<ApiResponse<StockDto>> addStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        stockService.increaseStock(productId, quantity);
        StockDto stock = stockService.getStockByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success("Stock added successfully", stock));
    }

    @PatchMapping("/product/{productId}/reduce")
    public ResponseEntity<ApiResponse<StockDto>> reduceStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        stockService.decreaseStock(productId, quantity);
        StockDto stock = stockService.getStockByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success("Stock reduced successfully", stock));
    }

    @PatchMapping("/product/{productId}/set")
    public ResponseEntity<ApiResponse<StockDto>> setStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        StockDto stock = stockService.getStockByProductId(productId);
        stock.setQuantityAvailable(quantity);
        StockDto updatedStock = stockService.updateStock(stock.getId(), stock);
        return ResponseEntity.ok(ApiResponse.success("Stock set successfully", updatedStock));
    }
}
