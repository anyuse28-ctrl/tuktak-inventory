package com.tuktak.inventory.controller;

import com.tuktak.inventory.dto.ApiResponse;
import com.tuktak.inventory.dto.PurchaseDto;
import com.tuktak.inventory.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseDto>> createPurchase(@Valid @RequestBody PurchaseDto purchaseDto) {


        PurchaseDto createdPurchase = purchaseService.createPurchase(purchaseDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase created successfully", createdPurchase));
    }



    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseDto>>> getAllPurchases() {
        List<PurchaseDto> purchases = purchaseService.getAllPurchases();
        return ResponseEntity.ok(ApiResponse.success(purchases));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseDto>> getPurchaseById(@PathVariable Long id) {
        PurchaseDto purchase = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(ApiResponse.success(purchase));
    }

    @GetMapping("/number/{purchaseNumber}")
    public ResponseEntity<ApiResponse<PurchaseDto>> getPurchaseByNumber(@PathVariable String purchaseNumber) {
        PurchaseDto purchase = purchaseService.getPurchaseByNumber(purchaseNumber);
        return ResponseEntity.ok(ApiResponse.success(purchase));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PurchaseDto>>> getPurchasesByStatus(@PathVariable String status) {
        List<PurchaseDto> purchases = purchaseService.getPurchasesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(purchases));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<PurchaseDto>>> getPurchasesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PurchaseDto> purchases = purchaseService.getPurchasesByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(purchases));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseDto>> updatePurchase(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseDto purchaseDto) {
        PurchaseDto updatedPurchase = purchaseService.updatePurchase(id, purchaseDto);
        return ResponseEntity.ok(ApiResponse.success("Purchase updated successfully", updatedPurchase));
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<PurchaseDto>> receivePurchase(@PathVariable Long id) {
        PurchaseDto receivedPurchase = purchaseService.receivePurchase(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase received successfully", receivedPurchase));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseDto>> cancelPurchase(@PathVariable Long id) {
        PurchaseDto cancelledPurchase = purchaseService.cancelPurchase(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase cancelled successfully", cancelledPurchase));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase deleted successfully", null));
    }
}
