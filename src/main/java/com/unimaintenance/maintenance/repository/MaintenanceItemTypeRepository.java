
package com.unimaintenance.maintenance.repository;

import com.unimaintenance.maintenance.entity.MaintenanceItemType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceItemTypeRepository
        extends JpaRepository<MaintenanceItemType, Long> {

    // Display selectable maintenance items alphabetically.
    List<MaintenanceItemType> findByActiveTrueOrderByNameAsc();

    // Prevent duplicate catalogue records.
    boolean existsByNameIgnoreCase(String name);

    // Find a specific equipment or facility type.
    Optional<MaintenanceItemType> findByNameIgnoreCase(
            String name
    );
}
