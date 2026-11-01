
package com.unimaintenance.maintenance.repository;

import com.unimaintenance.maintenance.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // Get all rooms belonging to a building.
    List<Room> findByBuilding_IdOrderByFloorNumberAscNameAsc(
            Long buildingId
    );

    // Prevent duplicate names on the same floor of a building.
    boolean existsByBuilding_IdAndFloorNumberAndNameIgnoreCase(
            Long buildingId,
            int floorNumber,
            String name
    );
}
