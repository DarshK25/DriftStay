package com.driftstay.room.service;

import com.driftstay.common.enums.RoomStatus;
import com.driftstay.gallery.service.ImageStorageService;
import com.driftstay.room.entity.Room;
import com.driftstay.room.entity.RoomImage;
import com.driftstay.room.repository.RoomRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ImageStorageService imageStorageService;
    private final EntityManager entityManager;

    @Transactional
    public Room createRoom(Room room) {
        room.setPublicId(UUID.randomUUID().toString().replace("-", "").substring(0, 26));
        room.setStatus(RoomStatus.ACTIVE);
        Room saved = roomRepository.save(room);
        log.info("Room created: id={}, name={}", saved.getId(), saved.getRoomName());
        return saved;
    }

    @Transactional
    @CacheEvict(value = "roomTypes", allEntries = true)
    public Room updateRoom(Long id, Room updates) {
        Room room = getRoom(id);
        if (updates.getRoomName() != null) room.setRoomName(updates.getRoomName());
        if (updates.getBasePrice() != null) room.setBasePrice(updates.getBasePrice());
        if (updates.getCapacity() != null) room.setCapacity(updates.getCapacity());
        if (updates.getDescription() != null) room.setDescription(updates.getDescription());
        if (updates.getStatus() != null) room.setStatus(updates.getStatus());
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = getRoom(id);
        room.setStatus(RoomStatus.DELETED);
        roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Room> getRoomsByProperty(Long propertyId) {
        return roomRepository.findByPropertyIdAndStatus(propertyId, RoomStatus.ACTIVE);
    }

    @Transactional
    public List<String> uploadImages(Long roomId, List<MultipartFile> files) {
        Room room = getRoom(roomId);
        return files.stream().map(file -> {
            String url = imageStorageService.uploadImage(file, "rooms/" + roomId);
            RoomImage image = new RoomImage();
            image.setRoom(room);
            image.setImageUrl(url);
            image.setDisplayOrder(room.getImages().size() + 1);
            entityManager.persist(image);
            return url;
        }).collect(Collectors.toList());
    }
}
