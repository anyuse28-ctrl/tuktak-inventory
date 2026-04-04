package com.tuktak.inventory.service;

import com.tuktak.inventory.dto.PurchaseDto;
import com.tuktak.inventory.dto.PurchaseItemDto;
import com.tuktak.inventory.entity.Product;
import com.tuktak.inventory.entity.Purchase;
import com.tuktak.inventory.entity.PurchaseItem;
import com.tuktak.inventory.repository.ProductRepository;
import com.tuktak.inventory.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public PurchaseDto createPurchase(PurchaseDto purchaseDto) {
//        if (purchaseRepository.existsByPurchaseNumber(purchaseDto.getPurchaseNumber())) {
//            throw new IllegalArgumentException("Purchase number already exists: " + purchaseDto.getPurchaseNumber());
//        }

        Purchase purchase = Purchase.builder()
                .purchaseNumber(generatePurchaseNumber())
                .supplierName(purchaseDto.getSupplierName())
                .supplierContact(purchaseDto.getSupplierContact())
                .purchaseDate(purchaseDto.getPurchaseDate() != null ? purchaseDto.getPurchaseDate() : LocalDateTime.now())
                .status(Purchase.PurchaseStatus.RECEIVED)  // ✅ changed from PENDING
                .notes(purchaseDto.getNotes())
                .build();

        if (purchaseDto.getPurchaseItems() != null && !purchaseDto.getPurchaseItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (PurchaseItemDto itemDto : purchaseDto.getPurchaseItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemDto.getProductId()));

                PurchaseItem item = PurchaseItem.builder()
                        .product(product)
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .build();
                item.calculateTotalPrice();
                purchase.addPurchaseItem(item);
                totalAmount = totalAmount.add(item.getTotalPrice());

                // ✅ ADD THESE TWO LINES
                stockService.increaseStock(product.getId(), itemDto.getQuantity());
                log.info("Stock increased for product {} by {} units", product.getName(), itemDto.getQuantity());
            }
            purchase.setTotalAmount(totalAmount);
        }

        Purchase savedPurchase = purchaseRepository.save(purchase);
        log.info("Purchase created: {}", savedPurchase.getPurchaseNumber());
        return mapToDto(savedPurchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseDto> getAllPurchases() {
        return purchaseRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseDto getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + id));
        return mapToDto(purchase);
    }

    @Transactional(readOnly = true)
    public PurchaseDto getPurchaseByNumber(String purchaseNumber) {
        Purchase purchase = purchaseRepository.findByPurchaseNumber(purchaseNumber)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with number: " + purchaseNumber));
        return mapToDto(purchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseDto> getPurchasesByStatus(String status) {
        Purchase.PurchaseStatus purchaseStatus = Purchase.PurchaseStatus.valueOf(status.toUpperCase());
        return purchaseRepository.findByStatus(purchaseStatus).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PurchaseDto> getPurchasesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return purchaseRepository.findByPurchaseDateBetween(startDate, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseDto updatePurchase(Long id, PurchaseDto purchaseDto) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + id));

        if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING purchases can be updated");
        }

        if (purchaseDto.getSupplierName() != null) {
            purchase.setSupplierName(purchaseDto.getSupplierName());
        }
        if (purchaseDto.getSupplierContact() != null) {
            purchase.setSupplierContact(purchaseDto.getSupplierContact());
        }
        if (purchaseDto.getNotes() != null) {
            purchase.setNotes(purchaseDto.getNotes());
        }
        if (purchaseDto.getPurchaseDate() != null) {
            purchase.setPurchaseDate(purchaseDto.getPurchaseDate());
        }

        Purchase updatedPurchase = purchaseRepository.save(purchase);
        log.info("Purchase updated: {}", purchase.getPurchaseNumber());
        return mapToDto(updatedPurchase);
    }

    @Transactional
    public PurchaseDto receivePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + id));

        if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING) {
            throw new IllegalArgumentException("Purchase is not in PENDING status. Current status: " + purchase.getStatus());
        }

        for (PurchaseItem item : purchase.getPurchaseItems()) {
            stockService.increaseStock(item.getProduct().getId(), item.getQuantity());
            log.info("Stock increased for product {} by {} units", 
                    item.getProduct().getName(), item.getQuantity());
        }

        purchase.setStatus(Purchase.PurchaseStatus.RECEIVED);
        Purchase updatedPurchase = purchaseRepository.save(purchase);
        
        log.info("Purchase {} received successfully. Stock updated for {} items.", 
                purchase.getPurchaseNumber(), purchase.getPurchaseItems().size());
        
        return mapToDto(updatedPurchase);
    }

    @Transactional
    public PurchaseDto cancelPurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + id));

        if (purchase.getStatus() == Purchase.PurchaseStatus.RECEIVED) {
            throw new IllegalArgumentException("Cannot cancel a received purchase. Stock has already been updated.");
        }

        purchase.setStatus(Purchase.PurchaseStatus.CANCELLED);
        Purchase updatedPurchase = purchaseRepository.save(purchase);
        
        log.info("Purchase {} cancelled", purchase.getPurchaseNumber());
        return mapToDto(updatedPurchase);
    }

    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + id));

        if (purchase.getStatus() == Purchase.PurchaseStatus.RECEIVED) {
            throw new IllegalArgumentException("Cannot delete a received purchase");
        }

        purchaseRepository.deleteById(id);
        log.info("Purchase {} deleted", purchase.getPurchaseNumber());
    }

    private PurchaseDto mapToDto(Purchase purchase) {
        List<PurchaseItemDto> itemDtos = purchase.getPurchaseItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());

        return PurchaseDto.builder()
                .id(purchase.getId())
                .purchaseNumber(purchase.getPurchaseNumber())
                .supplierName(purchase.getSupplierName())
                .supplierContact(purchase.getSupplierContact())
                .purchaseDate(purchase.getPurchaseDate())
                .totalAmount(purchase.getTotalAmount())
                .status(purchase.getStatus().name())
                .notes(purchase.getNotes())
                .purchaseItems(itemDtos)
                .build();
    }

    private PurchaseItemDto mapItemToDto(PurchaseItem item) {
        return PurchaseItemDto.builder()
                .id(item.getId())
                .purchaseId(item.getPurchase().getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private String generatePurchaseNumber() {
        Long seq = jdbcTemplate.queryForObject(
                "SELECT nextval('purchase_number_seq')",
                Long.class
        );
        return String.format("PO-%05d", seq);
    }

}
