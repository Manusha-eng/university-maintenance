
package com.unimaintenance.maintenance.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "rooms",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_room_building_floor_name",
        columnNames = {"building_id", "floor_number", "name"}
    )
)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    // Ground floor = 0, first floor = 1, and so on.
    @Column(name = "floor_number", nullable = false)
    private int floorNumber;

    @Column(name = "room_type", nullable = false, length = 40)
    private String roomType = "OTHER";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }
}
