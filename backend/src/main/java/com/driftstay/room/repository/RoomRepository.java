package com.driftstay.room.repository;

import com.driftstay.common.enums.RoomStatus;
import com.driftstay.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByPublicId(String publicId);
    List<Room> findByPropertyId(Long propertyId);
    List<Room> findByPropertyIdAndStatus(Long propertyId, RoomStatus status);
    boolean existsByPropertyIdAndRoomNumber(Long propertyId, String roomNumber);
}
