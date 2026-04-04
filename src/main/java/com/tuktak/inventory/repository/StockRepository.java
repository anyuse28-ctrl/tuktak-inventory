package com.tuktak.inventory.repository;

import com.tuktak.inventory.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId")
    Optional<Stock> findByProductIdWithLock(@Param("productId") Long productId);

    @Query("SELECT s FROM Stock s WHERE s.quantityAvailable <= s.minStockLevel")
    List<Stock> findLowStockItems();

    @Query("SELECT s FROM Stock s WHERE s.quantityAvailable <= s.reorderPoint")
    List<Stock> findItemsNeedingReorder();

    @Query("SELECT s FROM Stock s WHERE s.quantityAvailable = 0")
    List<Stock> findOutOfStockItems();

    boolean existsByProductId(Long productId);
}
