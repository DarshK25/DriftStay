package com.driftstay.booking.controller;

import com.driftstay.auth.service.AuthService;
import com.driftstay.booking.dto.request.BookingCreateRequest;
import com.driftstay.booking.dto.response.BookingResponse;
import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.mapper.BookingMapper;
import com.driftstay.booking.service.BookingService;
import com.driftstay.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<BookingResponse> createBooking(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                                          @Valid @RequestBody BookingCreateRequest request) {
        var user = authService.getCurrentUser(userDetails);
        var booking = bookingMapper.toEntity(request);
        booking.setUserId(user.getId());
        booking.setPropertyId(request.getPropertyId());
        booking.setRoomId(request.getRoomId());
        var saved = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingMapper.toResponse(saved));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get booking details")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String publicId) {
        var booking = bookingService.getBookingByPublicId(publicId);
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @GetMapping
    @Operation(summary = "Get current user bookings")
    public ResponseEntity<PagedResponse<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var user = authService.getCurrentUser(userDetails);
        var result = bookingService.getUserBookings(user.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(bookingMapper::toResponse);
        return ResponseEntity.ok(PagedResponse.of(result));
    }

    @PostMapping("/{publicId}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String publicId) {
        var booking = bookingService.cancelBooking(publicId);
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get bookings for a specific user (admin)")
    public ResponseEntity<PagedResponse<BookingResponse>> getUserBookings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = bookingService.getUserBookings(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(bookingMapper::toResponse);
        return ResponseEntity.ok(PagedResponse.of(result));
    }
}
