package com.tuktak.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDto {

    private Long id;

//    @NotBlank(message = "Purchase number is required")
    private String purchaseNumber;

    private String supplierName;

    private String supplierContact;

    @NotNull(message = "Purchase date is required")
    private LocalDateTime purchaseDate;

    private BigDecimal totalAmount;

    private String status;

    private String notes;

    private List<PurchaseItemDto> purchaseItems;
}
