
package com.unimaintenance.maintenance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
    name = "complaint_photos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_complaint_photo_complaint",
        columnNames = "complaint_id"
    )
)
public class ComplaintPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each photograph belongs to exactly one complaint.
    // Each complaint can have at most one photograph.

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "complaint_id",
        nullable = false,
        unique = true
    )
    private Complaint complaint;

    @Column(
        name = "content_type",
        nullable = false,
        length = 30
    )
    private String contentType;

    // The actual image is stored in MySQL.
    @Lob
    @Column(
        name = "image_data",
        nullable = false,
        columnDefinition = "LONGBLOB"
    )
    private byte[] imageData;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    public ComplaintPhoto() {
    }

    @PrePersist
    public void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
