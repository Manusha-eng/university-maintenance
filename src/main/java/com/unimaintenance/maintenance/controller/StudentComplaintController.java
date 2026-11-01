
package com.unimaintenance.maintenance.controller;

import com.unimaintenance.maintenance.entity.Building;
import com.unimaintenance.maintenance.entity.Complaint;
import com.unimaintenance.maintenance.entity.ComplaintPhoto;
import com.unimaintenance.maintenance.entity.MaintenanceItemType;
import com.unimaintenance.maintenance.entity.Room;

import com.unimaintenance.maintenance.exception.ComplaintNotFoundException;

import com.unimaintenance.maintenance.repository.MaintenanceItemTypeRepository;
import com.unimaintenance.maintenance.repository.RoomRepository;

import com.unimaintenance.maintenance.service.ComplaintPhotoService;
import com.unimaintenance.maintenance.service.ComplaintService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/complaints/my")
public class StudentComplaintController {

    private static final Set<String> VALID_CATEGORIES = Set.of(
            "ELECTRICAL",
            "PLUMBING",
            "FURNITURE",
            "NETWORK",
            "AIR_CONDITIONING",
            "OTHER"
    );

    private static final Set<String> VALID_PRIORITIES = Set.of(
            "LOW", "MEDIUM", "HIGH"
    );

    private final ComplaintService complaintService;
    private final ComplaintPhotoService photoService;
    private final RoomRepository roomRepository;
    private final MaintenanceItemTypeRepository itemTypeRepository;

    public StudentComplaintController(
            ComplaintService complaintService,
            ComplaintPhotoService photoService,
            RoomRepository roomRepository,
            MaintenanceItemTypeRepository itemTypeRepository) {

        this.complaintService = complaintService;
        this.photoService = photoService;
        this.roomRepository = roomRepository;
        this.itemTypeRepository = itemTypeRepository;
    }

    // Students, lecturers and staff can access this
    // only for their own pending complaints.

    @GetMapping("/edit/{id}")
    public String showStudentEditForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        Complaint complaint =
                findOwnedComplaint(id, authentication);

        if (!"PENDING".equals(complaint.getStatus())) {

            redirectAttributes.addFlashAttribute(
                    "editError",
                    "Only pending complaints can be edited."
            );

            return "redirect:/complaints/mine";
        }

        model.addAttribute("complaint", complaint);

        Long buildingId =
                complaint.getBuilding() == null
                        ? null
                        : complaint.getBuilding().getId();

        Long roomId =
                complaint.getRoom() == null
                        ? null
                        : complaint.getRoom().getId();

        Long itemTypeId =
                complaint.getMaintenanceItemType() == null
                        ? null
                        : complaint.getMaintenanceItemType().getId();

        addFormOptions(
                model,
                id,
                buildingId,
                roomId,
                itemTypeId
        );

        return "complaints/student-edit";
    }

    // Update only allowed fields on an owned,
    // still-pending complaint.

    @Transactional
    @PostMapping("/update/{id}")
    public String updateStudentComplaint(
            @PathVariable Long id,

            @Valid @ModelAttribute("complaint") Complaint form,
            BindingResult result,

            @RequestParam(name = "buildingId", required = false)
            Long buildingId,

            @RequestParam(name = "roomId", required = false)
            Long roomId,

            @RequestParam(name = "itemTypeId", required = false)
            Long itemTypeId,

            @RequestParam(name = "photo", required = false)
            MultipartFile photo,

            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        Complaint existing =
                findOwnedComplaint(id, authentication);

        // Refuse editing after processing has started.

        if (!"PENDING".equals(existing.getStatus())) {

            redirectAttributes.addFlashAttribute(
                    "editError",
                    "This complaint is no longer pending."
            );

            return "redirect:/complaints/mine";
        }

        // Keep the original database record ID.

        form.setId(existing.getId());

        Building building =
                complaintService.findBuildingById(buildingId);

        Room room =
                roomId == null
                        ? null
                        : roomRepository.findById(roomId).orElse(null);

        // Building and floor validation

        if (buildingId != null && building == null) {

            result.reject(
                    "building.invalid",
                    "Please select a valid building."
            );
        }

        if (buildingId == null &&
                form.getFloorNumber() != null) {

            result.reject(
                    "floor.invalid",
                    "Select a building before selecting a floor."
            );
        }

        // Room validation

        if (roomId != null && room == null) {

            result.reject(
                    "room.invalid",
                    "Please select a valid room."
            );
        }

        if (room != null) {

            if (building == null ||
                    !room.getBuilding().getId()
                            .equals(building.getId())) {

                result.reject(
                        "room.building.invalid",
                        "The room does not belong to the selected building."
                );
            }

            if (form.getFloorNumber() == null ||
                    room.getFloorNumber() != form.getFloorNumber()) {

                result.reject(
                        "room.floor.invalid",
                        "The room does not belong to the selected floor."
                );
            }
        }

        // Validate category and reported priority

        if (!VALID_CATEGORIES.contains(
                String.valueOf(form.getCategory()))) {

            result.reject(
                    "category.invalid",
                    "Please select a valid issue category."
            );
        }

        if (!VALID_PRIORITIES.contains(
                String.valueOf(form.getPriority()))) {

            result.reject(
                    "priority.invalid",
                    "Please select a valid priority."
            );
        }

        // New: validate the affected maintenance item.

        MaintenanceItemType selectedItem = null;

        if (itemTypeId != null) {

            selectedItem = itemTypeRepository
                    .findById(itemTypeId)
                    .orElse(null);

            if (selectedItem == null ||
                    !selectedItem.isActive()) {

                result.reject(
                        "item.invalid",
                        "Please select a valid maintenance item."
                );

            } else if (!selectedItem.getCategory()
                    .equals(form.getCategory())) {

                result.reject(
                        "item.category.invalid",
                        "The selected item does not match the issue category."
                );
            }
        }

        // Validate optional photograph.

        if (photoService.hasPhoto(photo)) {

            try {
                photoService.validatePhoto(photo);

            } catch (IllegalArgumentException exception) {

                result.reject(
                        "photo.invalid",
                        exception.getMessage()
                );
            }
        }

        // Redisplay the form if anything is invalid.

        if (result.hasErrors()) {

            addFormOptions(
                    model,
                    existing.getId(),
                    buildingId,
                    roomId,
                    itemTypeId
            );

            return "complaints/student-edit";
        }

        // Copy editable information only.
        // The student cannot change ID, submittedBy
        // or maintenance status.

        existing.setTitle(form.getTitle());
        existing.setDescription(form.getDescription());
        existing.setLocation(form.getLocation());

        existing.setCategory(form.getCategory());
        existing.setPriority(form.getPriority());

        existing.setBuilding(building);
        existing.setFloorNumber(form.getFloorNumber());
        existing.setRoom(room);

        // New affected item relationship.

        existing.setMaintenanceItemType(selectedItem);

        complaintService.saveComplaint(existing);

        // Keep existing photo if none was uploaded.

        if (photoService.hasPhoto(photo)) {
            photoService.savePhoto(existing, photo);
        }

        redirectAttributes.addFlashAttribute(
                "editSuccess",
                "Your complaint was updated successfully."
        );

        return "redirect:/complaints/mine";
    }

    // A reporter may see their own photograph,
    // but not someone else's.

    @GetMapping("/photo/{id}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> viewOwnPhoto(
            @PathVariable Long id,
            Authentication authentication) {

        findOwnedComplaint(id, authentication);

        Optional<ComplaintPhoto> optionalPhoto =
                photoService.findPhoto(id);

        if (optionalPhoto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ComplaintPhoto photo = optionalPhoto.get();

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                photo.getContentType()
                        )
                )
                .cacheControl(CacheControl.noStore())
                .body(photo.getImageData());
    }

    // Security check: the complaint must exist
    // and belong to the logged-in reporter.

    private Complaint findOwnedComplaint(
            Long id,
            Authentication authentication) {

        Complaint complaint;

        try {

            complaint = complaintService.getComplaintById(id);

        } catch (ComplaintNotFoundException exception) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Complaint not found"
            );
        }

        if (authentication == null ||
                !authentication.getName()
                        .equals(complaint.getSubmittedBy())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Complaint not found"
            );
        }

        return complaint;
    }

    // Supply all dropdown data on new/edit errors.

    private void addFormOptions(
            Model model,
            Long complaintId,
            Long buildingId,
            Long roomId,
            Long itemTypeId) {

        model.addAttribute(
                "buildings",
                complaintService.getAllBuildings()
        );

        model.addAttribute(
                "rooms",
                roomRepository.findAll(
                        Sort.by("floorNumber")
                                .ascending()
                                .and(
                                    Sort.by("name").ascending()
                                )
                )
        );

        model.addAttribute(
                "itemTypes",
                itemTypeRepository.findByActiveTrueOrderByNameAsc()
        );

        model.addAttribute(
                "selectedBuildingId",
                buildingId
        );

        model.addAttribute(
                "selectedRoomId",
                roomId
        );

        model.addAttribute(
                "selectedItemTypeId",
                itemTypeId
        );

        model.addAttribute(
                "hasPhoto",
                photoService.hasPhotoForComplaint(complaintId)
        );
    }
}
