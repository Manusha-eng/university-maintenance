
package com.unimaintenance.maintenance.repository;

import com.unimaintenance.maintenance.entity.ComplaintPhoto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComplaintPhotoRepository
        extends JpaRepository<ComplaintPhoto, Long> {

    // Find the photograph belonging to a complaint.
    Optional<ComplaintPhoto> findByComplaint_Id(Long complaintId);

    // Check whether a complaint already has a photograph.
    boolean existsByComplaint_Id(Long complaintId);

    // Remove the photograph before deleting its complaint.
    void deleteByComplaint_Id(Long complaintId);
}
