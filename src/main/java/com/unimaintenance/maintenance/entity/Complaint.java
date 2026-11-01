
package com.unimaintenance.maintenance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Complaint title is required")
    @Size(max = 150)
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000)
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 150)
    @Column(name = "location", nullable = false, length = 150)
    private String location;

    @Column(name = "category", length = 50)
    private String category = "OTHER";

    @Column(name = "priority", length = 20)
    private String priority = "MEDIUM";

    @Column(name = "status", length = 30)
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    // Ground = 0, First = 1, etc.
    @Min(value = 0, message = "Invalid floor number")
    @Max(value = 3, message = "Invalid floor number")
    @Column(name = "floor_number")
    private Integer floorNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    // New optional equipment / facility item relationship.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_item_type_id")
    private MaintenanceItemType maintenanceItemType;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    public Complaint() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public MaintenanceItemType getMaintenanceItemType() {
        return maintenanceItemType;
    }

    public void setMaintenanceItemType(
            MaintenanceItemType maintenanceItemType) {
        this.maintenanceItemType = maintenanceItemType;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }
}
