
package com.unimaintenance.maintenance.controller;

import com.unimaintenance.maintenance.entity.Complaint;
import com.unimaintenance.maintenance.service.ComplaintService;
import com.unimaintenance.maintenance.service.GeminiService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.unimaintenance.maintenance.exception.ComplaintNotFoundException;
import org.springframework.http.HttpStatus;
import com.unimaintenance.maintenance.entity.Building;
import org.springframework.security.core.Authentication;


@Controller
@RequestMapping("/complaints")
public class ComplaintController {

    
private final ComplaintService complaintService;
private final GeminiService geminiService;

public ComplaintController(
        ComplaintService complaintService,
        GeminiService geminiService) {

    this.complaintService = complaintService;
    this.geminiService = geminiService;
}


    // Show all complaints
    
@GetMapping
public String listComplaints(
        @RequestParam(name = "keyword", defaultValue = "") String keyword,
        @RequestParam(name = "status", defaultValue = "") String status,
        @RequestParam(name = "sortBy", defaultValue = "newest") String sortBy,
        Model model) {

    model.addAttribute("complaints",
            complaintService.searchComplaints(keyword, status, sortBy));

    model.addAttribute("keyword", keyword);
    model.addAttribute("selectedStatus", status);
    model.addAttribute("sortBy", sortBy);

    return "complaints/list";
}


    // Show complaint details
    @GetMapping("/{id}")
    public String viewComplaint(@PathVariable Long id, Model model) {
        model.addAttribute("complaint",
                complaintService.getComplaintById(id));

        return "complaints/details";
    }

    // Show add form
    
    @GetMapping("/new")
    public String showAddForm(Model model) {

        model.addAttribute("complaint", new Complaint());

        addBuildingOptions(model, null);

        return "complaints/form";
    }


    // Save new complaint with validation
   
    
    @PostMapping("/save")
    public String saveComplaint(
            @Valid @ModelAttribute("complaint") Complaint complaint,
            BindingResult result,
            @RequestParam(name = "buildingId", required = false) Long buildingId,
            Model model,
            Authentication authentication) {


        Building building = complaintService.findBuildingById(buildingId);

        if (buildingId != null && building == null) {
            result.reject("building.invalid", "Please select a valid building.");
        }

        if (result.hasErrors()) {
            addBuildingOptions(model, buildingId);
            return "complaints/form";
        }

        
        complaint.setId(null);
        complaint.setStatus("PENDING");
        complaint.setBuilding(building);

        // Save the logged-in user's username
        complaint.setSubmittedBy(authentication.getName());


        complaintService.saveComplaint(complaint);

       return "redirect:/complaints/submitted";
    }

    // Show edit form
        
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {

        Complaint complaint = complaintService.getComplaintById(id);

        model.addAttribute("complaint", complaint);

        Long buildingId = complaint.getBuilding() == null
                ? null
                : complaint.getBuilding().getId();

        addBuildingOptions(model, buildingId);

        return "complaints/form";
    }


    // Update complaint with validation
    
@PostMapping("/update/{id}")
public String updateComplaint(
        @PathVariable Long id,
        @Valid @ModelAttribute("complaint") Complaint form,
        BindingResult result,
        @RequestParam(name = "buildingId", required = false) Long buildingId,
        Model model) {

    Complaint complaint = complaintService.getComplaintById(id);

    form.setId(id);

    Building building = complaintService.findBuildingById(buildingId);

    if (buildingId != null && building == null) {
        result.reject("building.invalid", "Please select a valid building.");
    }

    if (result.hasErrors()) {
        addBuildingOptions(model, buildingId);
        return "complaints/form";
    }

    complaint.setTitle(form.getTitle());
    complaint.setDescription(form.getDescription());
    complaint.setLocation(form.getLocation());
    complaint.setCategory(form.getCategory());
    complaint.setPriority(form.getPriority());
    complaint.setStatus(form.getStatus());

    complaint.setBuilding(building);

    complaintService.saveComplaint(complaint);

    return "redirect:/complaints";
}

    // Delete complaint
    @PostMapping("/delete/{id}")
    public String deleteComplaint(@PathVariable Long id) {

        complaintService.deleteComplaint(id);

        return "redirect:/complaints";
    }
    

    
    // Handle complaints that do not exist
    @ExceptionHandler(ComplaintNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleComplaintNotFound(
            ComplaintNotFoundException exception,
            Model model) {

        model.addAttribute("message", exception.getMessage());

        return "complaints/not-found";
    }
    
// Add building choices to the form
private void addBuildingOptions(Model model, Long selectedBuildingId) {

    model.addAttribute("buildings",
            complaintService.getAllBuildings());

    model.addAttribute("selectedBuildingId", selectedBuildingId);
}
        
    @PostMapping("/{id}/analyze")
    public String analyzeComplaint(
            @PathVariable Long id,
            Model model) {

        Complaint complaint = complaintService.getComplaintById(id);

        String suggestion = geminiService.analyzeComplaint(complaint);

        model.addAttribute("complaint", complaint);
        model.addAttribute("aiSuggestion", suggestion);

        return "complaints/details";
    }

    @GetMapping("/submitted")
    public String complaintSubmitted() {
        return "complaints/submitted";
    }

        
    // Show complaints belonging to the logged-in user
    @GetMapping("/mine")
    public String showMyComplaints(
            Authentication authentication,
            Model model) {

        String username = authentication.getName();

        model.addAttribute(
                "complaints",
                complaintService.getMyComplaints(username));

        return "complaints/my-complaints";
    }

}
