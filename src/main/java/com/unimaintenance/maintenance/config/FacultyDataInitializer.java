
package com.unimaintenance.maintenance.config;

import com.unimaintenance.maintenance.entity.Building;
import com.unimaintenance.maintenance.entity.Room;
import com.unimaintenance.maintenance.repository.BuildingRepository;
import com.unimaintenance.maintenance.repository.RoomRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class FacultyDataInitializer implements CommandLineRunner {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final boolean seedEnabled;

    public FacultyDataInitializer(
            BuildingRepository buildingRepository,
            RoomRepository roomRepository,
            @Value("${SEED_FACULTY_DATA:false}") boolean seedEnabled) {

        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {

        if (!seedEnabled) {
            return;
        }

        Map<String, Building> buildings = new HashMap<>();

        // Keep all existing buildings.
        for (Building building : buildingRepository.findAll()) {
            buildings.put(normalize(building.getName()), building);
        }

        // Department buildings
        addBuilding(buildings,
                "Department of Civil and Environmental Engineering");

        addBuilding(buildings,
                "Department of Mechanical and Manufacturing Engineering");

        addBuilding(buildings,
                "Department of Electrical and Information Engineering");

        addBuilding(buildings,
                "Department of Interdisciplinary Studies");

        // Other faculty facilities
        addBuilding(buildings, "Auditorium Building");
        addBuilding(buildings, "Faculty Cafeteria (Kalderama)");
        addBuilding(buildings, "Helabojun E-Fac");
        addBuilding(buildings, "Faculty Library and Study Area");

        // Student accommodation
        addBuilding(buildings, "Hostel A");
        addBuilding(buildings, "Hostel B");
        addBuilding(buildings, "Hostel C");
        addBuilding(buildings, "Hostel D");

        // Auditorium building
        Building auditorium = buildings.get(
                normalize("Auditorium Building")
        );

        addRoom(auditorium, 0, "Dining and Seating Area", "COMMON_AREA");

        addRoom(auditorium, 1, "Lecture Theatre 1", "LECTURE_THEATRE");
        addRoom(auditorium, 1, "Lecture Theatre 2", "LECTURE_THEATRE");

        addRoom(auditorium, 1, "Lecture Room 1", "CLASSROOM");
        addRoom(auditorium, 1, "Lecture Room 2", "CLASSROOM");

        addRoom(auditorium, 2, "Auditorium Hall", "AUDITORIUM");

        // Faculty cafeteria
        Building cafeteria = buildings.get(
                normalize("Faculty Cafeteria (Kalderama)")
        );

        addRoom(cafeteria, 0, "Main Cafeteria", "CAFETERIA");
        addRoom(cafeteria, 0, "Faculty Medical Room", "MEDICAL");

        addRoom(cafeteria, 1, "Gymnasium", "SPORTS");
        addRoom(cafeteria, 1, "Pool Table Area", "SPORTS");
        addRoom(cafeteria, 1, "Bookshop", "SHOP");
        addRoom(cafeteria, 1, "Art Room", "OTHER");

        // Helabojun cafeteria
        Building helabojun = buildings.get(
                normalize("Helabojun E-Fac")
        );

        addRoom(helabojun, 0, "Ground Floor Dining Area", "CAFETERIA");
        addRoom(helabojun, 0, "Male Washroom", "WASHROOM");
        addRoom(helabojun, 0, "Female Washroom", "WASHROOM");

        addRoom(helabojun, 1, "First Floor Cafeteria", "CAFETERIA");

        // Faculty library
        Building library = buildings.get(
                normalize("Faculty Library and Study Area")
        );

        addRoom(library, 0, "24-Hour Study Area", "STUDY_AREA");
        addRoom(library, 1, "Library and Study Area", "LIBRARY");
        addRoom(library, 2, "Library and Study Area", "LIBRARY");

        // Interdisciplinary Studies / Administration
        Building administration = buildings.get(
                normalize("Department of Interdisciplinary Studies")
        );

        addRoom(administration, 0, "Board Meeting Room", "MEETING_ROOM");
        addRoom(administration, 0, "Dean's Office", "OFFICE");
        addRoom(administration, 0, "Administration Office", "OFFICE");

        addRoom(administration, 0,
                "Engineering Education Centre", "OFFICE");

        addRoom(administration, 3,
                "Old Computer Centre", "COMPUTER_LAB");

        addRoom(administration, 3,
                "New Computer Centre", "COMPUTER_LAB");

        // Hostels have washrooms on every floor.
        for (String hostelName : new String[] {
                "Hostel A", "Hostel B", "Hostel C", "Hostel D"
        }) {

            Building hostel = buildings.get(normalize(hostelName));

            for (int floor = 0; floor <= 3; floor++) {
                addRoom(hostel, floor, "Shared Washroom", "WASHROOM");
            }
        }

        System.out.println(
                "Faculty location records checked successfully."
        );
    }

    private void addBuilding(
            Map<String, Building> buildings,
            String name) {

        String key = normalize(name);

        if (buildings.containsKey(key)) {
            return;
        }

        Building building = new Building();
        building.setName(name);

        Building saved = buildingRepository.save(building);
        buildings.put(key, saved);
    }

    private void addRoom(
            Building building,
            int floor,
            String roomName,
            String roomType) {

        if (building == null) {
            return;
        }

        boolean exists =
                roomRepository
                    .existsByBuilding_IdAndFloorNumberAndNameIgnoreCase(
                        building.getId(),
                        floor,
                        roomName
                    );

        if (exists) {
            return;
        }

        Room room = new Room();

        room.setBuilding(building);
        room.setFloorNumber(floor);
        room.setName(roomName);
        room.setRoomType(roomType);

        roomRepository.save(room);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
