
package com.unimaintenance.maintenance.repository;

import com.unimaintenance.maintenance.entity.Complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    @Query("""
        SELECT c FROM Complaint c
        WHERE (
            :keyword = ''
            OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:status = '' OR c.status = :status)
        """)
    List<Complaint> searchComplaints(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Sort sort);



            
    List<Complaint> findBySubmittedByOrderByIdDesc(String submittedBy);

}
