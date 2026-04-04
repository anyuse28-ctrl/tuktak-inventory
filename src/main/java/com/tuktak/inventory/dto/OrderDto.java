package com.tuktak.inventory.dto;

import jakarta.validation.constraints.Email;
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
public class OrderDto {

    private Long id;

    private String orderNumber;

    private String customerName;

    @Email(message = "Invalid email format")
    private String customerEmail;

    private String customerPhone;

    private String shippingAddress;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private String status;

    private String notes;

    private List<OrderItemDto> orderItems;
}
