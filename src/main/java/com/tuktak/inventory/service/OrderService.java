package com.tuktak.inventory.service;

import com.tuktak.inventory.dto.OrderDto;
import com.tuktak.inventory.dto.OrderItemDto;
import com.tuktak.inventory.entity.Order;
import com.tuktak.inventory.entity.OrderItem;
import com.tuktak.inventory.entity.Product;
import com.tuktak.inventory.repository.OrderRepository;
import com.tuktak.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
//        if (orderRepository.existsByOrderNumber(orderDto.getOrderNumber())) {
//            throw new IllegalArgumentException("Order number already exists: " + orderDto.getOrderNumber());
//        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerName(orderDto.getCustomerName())
                .customerEmail(orderDto.getCustomerEmail())
                .customerPhone(orderDto.getCustomerPhone())
                .shippingAddress(orderDto.getShippingAddress())
                .orderDate(orderDto.getOrderDate() != null ? orderDto.getOrderDate() : LocalDateTime.now())
                .status(Order.OrderStatus.PENDING)
                .notes(orderDto.getNotes())
                .build();

        if (orderDto.getOrderItems() != null && !orderDto.getOrderItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemDto itemDto : orderDto.getOrderItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemDto.getProductId()));

                OrderItem item = OrderItem.builder()
                        .product(product)
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : product.getPrice())
                        .build();
                item.calculateTotalPrice();
                order.addOrderItem(item);
                totalAmount = totalAmount.add(item.getTotalPrice());
            }
            order.setTotalAmount(totalAmount);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {} (Status: PENDING)", savedOrder.getOrderNumber());
        return mapToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with number: " + orderNumber));
        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(String status) {
        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto updateOrder(Long id, OrderDto orderDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be updated");
        }

        if (orderDto.getCustomerName() != null) {
            order.setCustomerName(orderDto.getCustomerName());
        }
        if (orderDto.getCustomerEmail() != null) {
            order.setCustomerEmail(orderDto.getCustomerEmail());
        }
        if (orderDto.getCustomerPhone() != null) {
            order.setCustomerPhone(orderDto.getCustomerPhone());
        }
        if (orderDto.getShippingAddress() != null) {
            order.setShippingAddress(orderDto.getShippingAddress());
        }
        if (orderDto.getNotes() != null) {
            order.setNotes(orderDto.getNotes());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated: {}", order.getOrderNumber());
        return mapToDto(updatedOrder);
    }

    @Transactional
    public OrderDto confirmOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be confirmed. Current status: " + order.getStatus());
        }

        List<String> insufficientStockItems = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            boolean hasStock = stockService.validateStockAvailability(
                    item.getProduct().getId(), 
                    item.getQuantity()
            );
            if (!hasStock) {
                insufficientStockItems.add(item.getProduct().getName() + " (requested: " + item.getQuantity() + ")");
            }
        }

        if (!insufficientStockItems.isEmpty()) {
            throw new IllegalArgumentException("Insufficient stock for: " + String.join(", ", insufficientStockItems));
        }

        for (OrderItem item : order.getOrderItems()) {
            stockService.decreaseStock(item.getProduct().getId(), item.getQuantity());
            log.info("Stock decreased for product {} by {} units", 
                    item.getProduct().getName(), item.getQuantity());
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order {} confirmed. Stock decreased for {} items.", 
                order.getOrderNumber(), order.getOrderItems().size());
        
        return mapToDto(updatedOrder);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        Order.OrderStatus currentStatus = order.getStatus();
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());

        if (currentStatus == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot change status of a cancelled order");
        }
        if (currentStatus == Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot change status of a delivered order");
        }
        if (newStatus == Order.OrderStatus.CONFIRMED && currentStatus != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be confirmed. Use confirmOrder endpoint.");
        }

        // ✅ If changing to CONFIRMED, decrease stock
        if (newStatus == Order.OrderStatus.CONFIRMED && currentStatus == Order.OrderStatus.PENDING) {
            List<String> insufficientStockItems = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                boolean hasStock = stockService.validateStockAvailability(
                        item.getProduct().getId(), item.getQuantity());
                if (!hasStock) {
                    insufficientStockItems.add(item.getProduct().getName() +
                            " (requested: " + item.getQuantity() + ")");
                }
            }
            if (!insufficientStockItems.isEmpty()) {
                throw new IllegalArgumentException("Insufficient stock for: " +
                        String.join(", ", insufficientStockItems));
            }
            for (OrderItem item : order.getOrderItems()) {
                stockService.decreaseStock(item.getProduct().getId(), item.getQuantity());
                log.info("Stock decreased for product {} by {} units",
                        item.getProduct().getName(), item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated from {} to {}",
                order.getOrderNumber(), currentStatus, newStatus);

        return mapToDto(updatedOrder);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel a delivered order");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order is already cancelled");
        }

        boolean stockWasDecreased = order.getStatus() == Order.OrderStatus.CONFIRMED ||
                                    order.getStatus() == Order.OrderStatus.PROCESSING ||
                                    order.getStatus() == Order.OrderStatus.SHIPPED;

        if (stockWasDecreased) {
            for (OrderItem item : order.getOrderItems()) {
                stockService.increaseStock(item.getProduct().getId(), item.getQuantity());
                log.info("Stock restored for product {} by {} units", 
                        item.getProduct().getName(), item.getQuantity());
            }
            log.info("Stock restored for {} items due to order cancellation", order.getOrderItems().size());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order {} cancelled. Stock restored: {}", order.getOrderNumber(), stockWasDecreased);
        
        return mapToDto(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot delete an order that is not PENDING or CANCELLED");
        }

        orderRepository.deleteById(id);
        log.info("Order {} deleted", order.getOrderNumber());
    }

    private OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .notes(order.getNotes())
                .orderItems(itemDtos)
                .build();
    }

    private OrderItemDto mapItemToDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .orderId(item.getOrder().getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
    private String generateOrderNumber() {
        Long seq = jdbcTemplate.queryForObject(
                "SELECT nextval('order_number_seq')",
                Long.class
        );
        return String.format("CO-%05d", seq);
    }
}
