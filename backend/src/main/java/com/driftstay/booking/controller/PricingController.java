package com.driftstay.booking.controller;

import com.driftstay.booking.dto.response.PriceBreakdown;
import com.driftstay.booking.service.PricingService;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    /**
     * Get a price breakdown for a potential booking.
     * Always calculated server-side - frontend totals are never trusted.
     * GET /api/pricing/calculate?roomId=X&checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD&guestCount=N&coupon=OPTIONAL
     */
    @GetMapping("/calculate")
    public ResponseEntity<PriceBreakdown> calculatePrice(
            @RequestParam @NotNull Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Future LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Future LocalDate checkOut,
            @RequestParam(defaultValue = "1") @Min(1) int guestCount,
            @RequestParam(required = false) String coupon) {

        PriceBreakdown breakdown = pricingService.calculatePrice(
                com.driftstay.booking.dto.request.PriceCalculationRequest.builder()
                        .roomId(roomId)
                        .checkIn(checkIn)
                        .checkOut(checkOut)
                        .guestCount(guestCount)
                        .couponCode(coupon)
                        .build()
        );

        return ResponseEntity.ok(breakdown);
    }
}
