
package com.unimaintenance.maintenance.repository;

import com.unimaintenance.maintenance.entity.ComplaintStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintStatusHistoryRepository
        extends JpaRepository<ComplaintStatusHistory, Long> {

    // Get the complete history for one complaint,
    // newest changes first.

    List<ComplaintStatusHistory>
        findByComplaint_IdOrderByChangedAtDesc(Long complaintId);

    // Delete history before deleting its complaint.

    void deleteByComplaint_Id(Long complaintId);
}
