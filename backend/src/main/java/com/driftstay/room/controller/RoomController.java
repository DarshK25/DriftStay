package com.driftstay.room.controller;

import com.driftstay.common.enums.AvailabilityStatus;
import com.driftstay.booking.service.AvailabilityCalendarService;
import com.driftstay.room.entity.Room;
import com.driftstay.room.entity.RoomAvailability;
import com.driftstay.room.service.RoomService;
import com.driftstay.security.filter.JwtUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final AvailabilityCalendarService calendarService;

    /** Create a room. POST /api/rooms */
    @PostMapping
    public ResponseEntity<Room> createRoom(
            @RequestBody Room room,
            @AuthenticationPrincipal JwtUser currentUser) {
        Room created = roomService.createRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Update a room. PUT /api/rooms/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(
            @PathVariable Long id,
            @RequestBody Room room,
            @AuthenticationPrincipal JwtUser currentUser) {
        Room updated = roomService.updateRoom(id, room);
        return ResponseEntity.ok(updated);
    }

    /** Delete a room. DELETE /api/rooms/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    /** Get room details. GET /api/rooms/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoom(id));
    }

    /** Get rooms by property. GET /api/rooms?propertyId=X */
    @GetMapping
    public ResponseEntity<List<Room>> getRoomsByProperty(
            @RequestParam Long propertyId) {
        return ResponseEntity.ok(roomService.getRoomsByProperty(propertyId));
    }

    /** Get room availability calendar. GET /api/rooms/{id}/calendar */
    @GetMapping("/{id}/calendar")
    public ResponseEntity<Map<LocalDate, AvailabilityStatus>> getCalendar(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        Map<LocalDate, AvailabilityStatus> calendar = calendarService.generateCalendar(id, from);
        return ResponseEntity.ok(calendar);
    }

    /** Block a room for dates. POST /api/rooms/{id}/block */
    @PostMapping("/{id}/block")
    public ResponseEntity<RoomAvailability> blockRoom(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "BLOCKED") AvailabilityStatus status,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal JwtUser currentUser) {
        RoomAvailability block = calendarService.blockRoom(
                id, startDate, endDate, status, reason, "USER:" + currentUser.getUserId());
        return ResponseEntity.ok(block);
    }

    /** Upload room images. POST /api/rooms/{id}/images */
    @PostMapping("/{id}/images")
    public ResponseEntity<List<String>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal JwtUser currentUser) {
        List<String> urls = roomService.uploadImages(id, files);
        return ResponseEntity.ok(urls);
    }
}
