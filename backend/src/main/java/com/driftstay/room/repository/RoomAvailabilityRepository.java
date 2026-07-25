package com.driftstay.room.repository;

import com.driftstay.common.enums.AvailabilityStatus;
import com.driftstay.room.entity.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {

    List<RoomAvailability> findByRoomId(Long roomId);

    @Query("""
            SELECT ra FROM RoomAvailability ra
            WHERE ra.room.id = :roomId
              AND ra.startDate < :endDate
              AND ra.endDate > :startDate
            ORDER BY ra.startDate ASC
            """)
    List<RoomAvailability> findOverlappingAvailability(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT ra FROM RoomAvailability ra
            WHERE ra.room.id = :roomId
              AND ra.status IN :statuses
              AND ra.startDate < :endDate
              AND ra.endDate > :startDate
            ORDER BY ra.startDate ASC
            """)
    List<RoomAvailability> findBlockedAvailability(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<AvailabilityStatus> statuses
    );

    @Query("""
            SELECT ra FROM RoomAvailability ra
            WHERE ra.room.id = :roomId
              AND ra.startDate >= :fromDate
              AND ra.endDate <= :toDate
            ORDER BY ra.startDate ASC
            """)
    List<RoomAvailability> findAvailabilityInRange(
            @Param("roomId") Long roomId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    boolean existsByRoomIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long roomId, LocalDate endDate, LocalDate startDate
    );
}
