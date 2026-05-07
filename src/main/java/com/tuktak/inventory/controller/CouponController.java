package com.tuktak.inventory.controller;

import com.tuktak.inventory.dto.ApiResponse;
import com.tuktak.inventory.dto.CouponDto;
import com.tuktak.inventory.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // Admin endpoints
    @PostMapping("/api/coupons")
    public ResponseEntity<ApiResponse<CouponDto>> createCoupon(@RequestBody CouponDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Coupon created", couponService.createCoupon(dto)));
    }

    @GetMapping("/api/coupons")
    public ResponseEntity<ApiResponse<List<CouponDto>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.success(couponService.getAllCoupons()));
    }

    @GetMapping("/api/coupons/{id}")
    public ResponseEntity<ApiResponse<CouponDto>> getCoupon(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCouponById(id)));
    }

    @PutMapping("/api/coupons/{id}")
    public ResponseEntity<ApiResponse<CouponDto>> updateCoupon(@PathVariable Long id, @RequestBody CouponDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Coupon updated", couponService.updateCoupon(id, dto)));
    }

    @DeleteMapping("/api/coupons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted", null));
    }

    // Customer endpoint - validate coupon
    @GetMapping("/api/public/coupons/validate")
    public ResponseEntity<ApiResponse<BigDecimal>> validateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        BigDecimal discount = couponService.validateCoupon(code, orderAmount);
        return ResponseEntity.ok(ApiResponse.success("Coupon valid! Discount: ৳" + discount, discount));
    }
}