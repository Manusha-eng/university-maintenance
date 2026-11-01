
package com.unimaintenance.maintenance.config;

import com.unimaintenance.maintenance.entity.MaintenanceItemType;
import com.unimaintenance.maintenance.repository.MaintenanceItemTypeRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MaintenanceItemTypeInitializer
        implements CommandLineRunner {

    private final MaintenanceItemTypeRepository repository;
    private final boolean seedEnabled;

    public MaintenanceItemTypeInitializer(
            MaintenanceItemTypeRepository repository,
            @Value("${SEED_MAINTENANCE_TYPES:false}")
            boolean seedEnabled) {

        this.repository = repository;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (!seedEnabled) {
            return;
        }

        // Electrical equipment

        addItem("Ceiling Fan", "ELECTRICAL");
        addItem("Wall Fan", "ELECTRICAL");
        addItem("Light / Lighting Fixture", "ELECTRICAL");
        addItem("Electrical Switch", "ELECTRICAL");
        addItem("Power Socket / Plug Point", "ELECTRICAL");
        addItem("Electrical Wiring", "ELECTRICAL");

        // Teaching equipment

        addItem("Projector", "ELECTRICAL");
        addItem("Microphone", "ELECTRICAL");
        addItem("Speaker / Sound System", "ELECTRICAL");
        addItem("Smart Board", "ELECTRICAL");
        addItem("Movable Chalkboard", "OTHER");

        // Computer and network facilities

        addItem("Desktop Computer", "NETWORK");
        addItem("Computer Monitor", "NETWORK");
        addItem("Keyboard / Mouse", "NETWORK");
        addItem("Network Connection", "NETWORK");
        addItem("Wi-Fi Equipment", "NETWORK");
        addItem("Printer", "NETWORK");

        // Air conditioning

        addItem("Air Conditioner", "AIR_CONDITIONING");
        addItem("Air Conditioning Remote", "AIR_CONDITIONING");

        // Plumbing and washrooms

        addItem("Water Tap", "PLUMBING");
        addItem("Sink / Wash Basin", "PLUMBING");
        addItem("Water Pipe", "PLUMBING");
        addItem("Toilet / Flush System", "PLUMBING");
        addItem("Water Supply", "PLUMBING");
        addItem("Drainage System", "PLUMBING");

        // Furniture

        addItem("Chair", "FURNITURE");
        addItem("Desk / Table", "FURNITURE");
        addItem("Cupboard / Cabinet", "FURNITURE");
        addItem("Door / Door Lock", "OTHER");
        addItem("Window", "OTHER");

        // Laboratories

        addItem("Laboratory Equipment", "OTHER");
        addItem("Laboratory Workbench", "FURNITURE");
        addItem("Laboratory Power Supply", "ELECTRICAL");

        // Library and study facilities

        addItem("Study Table", "FURNITURE");
        addItem("Library Furniture", "FURNITURE");

        // Sports and cafeteria facilities

        addItem("Gym Equipment", "OTHER");
        addItem("Pool Table", "OTHER");
        addItem("Cafeteria Equipment", "OTHER");

        // Accommodation and other facilities

        addItem("Bed / Hostel Furniture", "FURNITURE");
        addItem("Ceiling / Wall", "OTHER");
        addItem("Other / Not Listed", "OTHER");

        System.out.println(
            "Maintenance item catalogue checked successfully."
        );
    }

    private void addItem(
            String name,
            String category) {

        // Do not insert the same item twice.

        if (repository.existsByNameIgnoreCase(name)) {
            return;
        }

        MaintenanceItemType item =
                new MaintenanceItemType(name, category);

        repository.save(item);
    }
}
