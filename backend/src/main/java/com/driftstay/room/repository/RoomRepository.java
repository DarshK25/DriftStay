package com.driftstay.room.repository;

import com.driftstay.common.enums.RoomStatus;
import com.driftstay.room.entity.Room;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByPublicId(String publicId);

    List<Room> findByPropertyId(Long propertyId);

    List<Room> findByPropertyIdAndStatus(Long propertyId, RoomStatus status);

    List<Room> findByStatus(RoomStatus status);

    boolean existsByPropertyIdAndRoomNumber(Long propertyId, String roomNumber);

    /**
     * Find a room with PESSIMISTIC_WRITE lock.
     * Used by BookingService.createBooking() to prevent race conditions.
     * The lock is held for the duration of the transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT r FROM Room r WHERE r.id = :roomId")
    Optional<Room> findRoomWithLock(@Param("roomId") Long roomId);
}
