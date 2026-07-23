package com.driftstay.room.controller;

import com.driftstay.room.dto.request.RoomCreateRequest;
import com.driftstay.room.dto.request.RoomUpdateRequest;
import com.driftstay.room.dto.response.RoomDetailResponse;
import com.driftstay.room.dto.response.RoomSummaryResponse;
import com.driftstay.room.entity.Room;
import com.driftstay.room.mapper.RoomMapper;
import com.driftstay.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/properties/{propertyId}/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Room management endpoints")
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a room under a property")
    public ResponseEntity<RoomDetailResponse> createRoom(@PathVariable Long propertyId,
                                                          @Valid @RequestBody RoomCreateRequest request) {
        var room = roomMapper.toEntity(request);
        var property = new com.driftstay.property.entity.Property();
        property.setId(propertyId);
        room.setProperty(property);
        var saved = roomService.createRoom(room);
        var response = enrichDetail(roomMapper.toDetail(saved), saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a room")
    public ResponseEntity<RoomDetailResponse> updateRoom(@PathVariable Long propertyId,
                                                          @PathVariable String publicId,
                                                          @Valid @RequestBody RoomUpdateRequest request) {
        var existing = roomService.getRoomByPublicId(publicId);
        roomMapper.updateEntity(request, existing);
        var saved = roomService.updateRoom(publicId, existing);
        var response = enrichDetail(roomMapper.toDetail(saved), saved);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get room details")
    public ResponseEntity<RoomDetailResponse> getRoom(@PathVariable Long propertyId,
                                                       @PathVariable String publicId) {
        var room = roomService.getRoomByPublicId(publicId);
        var response = enrichDetail(roomMapper.toDetail(room), room);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all rooms for a property")
    public ResponseEntity<List<RoomSummaryResponse>> getRooms(@PathVariable Long propertyId) {
        var rooms = roomService.getRoomsByPropertyId(propertyId);
        return ResponseEntity.ok(roomMapper.toSummaryList(rooms));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a room")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long propertyId,
                                            @PathVariable String publicId) {
        roomService.deleteRoom(publicId);
        return ResponseEntity.noContent().build();
    }

    private RoomDetailResponse enrichDetail(RoomDetailResponse detail, Room room) {
        var imageUrls = room.getImages().stream()
                .map(img -> img.getImageUrl())
                .toList();
        return RoomDetailResponse.builder()
                .id(detail.getId())
                .publicId(detail.getPublicId())
                .roomName(detail.getRoomName())
                .roomNumber(detail.getRoomNumber())
                .description(detail.getDescription())
                .roomType(detail.getRoomType())
                .capacity(detail.getCapacity())
                .bedCount(detail.getBedCount())
                .bedType(detail.getBedType())
                .bathroomCount(detail.getBathroomCount())
                .basePrice(detail.getBasePrice())
                .weekendPrice(detail.getWeekendPrice())
                .cleaningFee(detail.getCleaningFee())
                .extraGuestFee(detail.getExtraGuestFee())
                .areaSqft(detail.getAreaSqft())
                .floorNumber(detail.getFloorNumber())
                .status(detail.getStatus())
                .imageUrls(imageUrls)
                .build();
    }
}
