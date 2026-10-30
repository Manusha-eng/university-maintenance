
package com.unimaintenance.maintenance.service;

import com.unimaintenance.maintenance.entity.Complaint;
import com.unimaintenance.maintenance.repository.ComplaintRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import com.unimaintenance.maintenance.exception.ComplaintNotFoundException;

import com.unimaintenance.maintenance.entity.Building;
import com.unimaintenance.maintenance.repository.BuildingRepository;


@Service
public class ComplaintService {

    
    private final ComplaintRepository complaintRepository;
    private final BuildingRepository buildingRepository;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            BuildingRepository buildingRepository) {

        this.complaintRepository = complaintRepository;
        this.buildingRepository = buildingRepository;
    }


    // Get all complaints
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    // Get one complaint using its ID
    
public Complaint getComplaintById(Long id) {
    return complaintRepository.findById(id)
            .orElseThrow(() -> new ComplaintNotFoundException(id));
}


    // Save a new or updated complaint
    public void saveComplaint(Complaint complaint) {
        complaintRepository.save(complaint);
    }

    // Delete a complaint
    public void deleteComplaint(Long id) {
        Complaint complaint = getComplaintById(id);
        complaintRepository.delete(complaint);
    }



    
    public List<Complaint> searchComplaints(
            String keyword, String status, String sortBy) {

        // Handle empty search inputs
        String searchText = keyword == null ? "" : keyword.trim();
        String selectedStatus = status == null ? "" : status.trim();

        // Decide how to sort the results
        Sort sort;

        if ("oldest".equals(sortBy)) {
            sort = Sort.by("id").ascending();
        } else if ("title_az".equals(sortBy)) {
            sort = Sort.by("title").ascending();
        } else if ("title_za".equals(sortBy)) {
            sort = Sort.by("title").descending();
        } else {
            sort = Sort.by("id").descending();
        }

        return complaintRepository.searchComplaints(
                searchText, selectedStatus, sort);
    }

    // Get all buildings for the dropdown
    public List<Building> getAllBuildings() {
        return buildingRepository.findAll(Sort.by("name").ascending());
    }

    // Find a building by its ID
    public Building findBuildingById(Long id) {
        if (id == null) {
            return null;
        }

        return buildingRepository.findById(id).orElse(null);
    }
    
    // Get complaints submitted by one user
    public List<Complaint> getMyComplaints(String username) {
        return complaintRepository
                .findBySubmittedByOrderByIdDesc(username);
    }

}
