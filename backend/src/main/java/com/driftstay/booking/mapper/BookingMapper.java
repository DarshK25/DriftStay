package com.driftstay.booking.mapper;

import com.driftstay.booking.dto.request.BookingCreateRequest;
import com.driftstay.booking.dto.response.BookingResponse;
import com.driftstay.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "bookingReference", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "propertyId", ignore = true)
    @Mapping(target = "roomId", ignore = true)
    @Mapping(target = "bookingStatus", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "basePriceSnapshot", ignore = true)
    @Mapping(target = "cleaningFeeSnapshot", ignore = true)
    @Mapping(target = "taxSnapshot", ignore = true)
    @Mapping(target = "discountSnapshot", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Booking toEntity(BookingCreateRequest request);

    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);
}
