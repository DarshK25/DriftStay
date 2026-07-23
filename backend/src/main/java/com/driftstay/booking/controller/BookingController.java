package com.driftstay.booking.controller;

import com.driftstay.auth.service.AuthService;
import com.driftstay.booking.dto.request.BookingCreateRequest;
import com.driftstay.booking.dto.response.BookingResponse;
import com.driftstay.booking.mapper.BookingMapper;
import com.driftstay.booking.service.BookingService;
import com.driftstay.common.dto.ApiResponse;
import com.driftstay.common.dto.PagedResponse;
import com.driftstay.common.validation.CreateValidation;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
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
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Validated(CreateValidation.class) @RequestBody BookingCreateRequest request) {
        var user = authService.getCurrentUser(userDetails);
        var booking = bookingMapper.toEntity(request);
        booking.setUserId(user.getId());
        booking.setPropertyId(request.getPropertyId());
        booking.setRoomId(request.getRoomId());
        var saved = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(bookingMapper.toResponse(saved)));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get booking details")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable String publicId) {
        var booking = bookingService.getBookingByPublicId(publicId);
        return ResponseEntity.ok(ApiResponse.success("Booking found", bookingMapper.toResponse(booking)));
    }

    @GetMapping
    @Operation(summary = "Get current user bookings")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var user = authService.getCurrentUser(userDetails);
        var result = bookingService.getUserBookings(user.getId(),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(bookingMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.paginated("Bookings found", PagedResponse.of(result)));
    }

    @PostMapping("/{publicId}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable String publicId) {
        var booking = bookingService.cancelBooking(publicId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", bookingMapper.toResponse(booking)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get bookings for a specific user (admin)")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getUserBookings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = bookingService.getUserBookings(userId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(bookingMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.paginated("Bookings found", PagedResponse.of(result)));
    }
}
