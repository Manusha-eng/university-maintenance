
package com.unimaintenance.maintenance.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "maintenance_item_types",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_maintenance_item_name",
        columnNames = "name"
    )
)
public class MaintenanceItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public MaintenanceItemType() {
    }

    public MaintenanceItemType(String name, String category) {
        this.name = name;
        this.category = category;
        this.active = true;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
