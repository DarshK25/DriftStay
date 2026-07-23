package com.driftstay.room.service;

import com.driftstay.common.enums.RoomStatus;
import com.driftstay.common.exception.ResourceNotFoundException;
import com.driftstay.room.entity.Room;
import com.driftstay.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional
    public Room createRoom(Room room) {
        Room saved = roomRepository.save(room);
        log.info("Created room: {} (publicId: {})", saved.getRoomName(), saved.getPublicId());
        return saved;
    }

    @Transactional
    public Room updateRoom(String publicId, Room updated) {
        Room room = roomRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + publicId));
        updated.setId(room.getId());
        updated.setPublicId(room.getPublicId());
        updated.setProperty(room.getProperty());
        Room saved = roomRepository.save(updated);
        log.info("Updated room: {} (publicId: {})", saved.getRoomName(), publicId);
        return saved;
    }

    public Room getRoomByPublicId(String publicId) {
        return roomRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + publicId));
    }

    public List<Room> getRoomsByPropertyId(Long propertyId) {
        return roomRepository.findByPropertyId(propertyId);
    }

    public List<Room> getActiveRoomsByPropertyId(Long propertyId) {
        return roomRepository.findByPropertyIdAndStatus(propertyId, RoomStatus.ACTIVE);
    }

    @Transactional
    public void deleteRoom(String publicId) {
        Room room = roomRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + publicId));
        roomRepository.delete(room);
        log.info("Deleted room: {} (publicId: {})", room.getRoomName(), publicId);
    }
}
