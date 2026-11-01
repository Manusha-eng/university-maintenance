
package com.unimaintenance.maintenance.controller;

import com.unimaintenance.maintenance.entity.Building;
import com.unimaintenance.maintenance.entity.Complaint;
import com.unimaintenance.maintenance.entity.ComplaintPhoto;
import com.unimaintenance.maintenance.entity.ComplaintStatusHistory;
import com.unimaintenance.maintenance.entity.MaintenanceItemType;
import com.unimaintenance.maintenance.entity.Room;

import com.unimaintenance.maintenance.exception.ComplaintNotFoundException;

import com.unimaintenance.maintenance.repository.ComplaintStatusHistoryRepository;
import com.unimaintenance.maintenance.repository.MaintenanceItemTypeRepository;
import com.unimaintenance.maintenance.repository.RoomRepository;

import com.unimaintenance.maintenance.service.ComplaintPhotoService;
import com.unimaintenance.maintenance.service.ComplaintService;
import com.unimaintenance.maintenance.service.GeminiService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final GeminiService geminiService;
    private final RoomRepository roomRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final ComplaintPhotoService photoService;
    private final MaintenanceItemTypeRepository itemTypeRepository;

    private static final DateTimeFormatter UTC_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    public ComplaintController(
            ComplaintService complaintService,
            GeminiService geminiService,
            RoomRepository roomRepository,
            ComplaintStatusHistoryRepository historyRepository,
            ComplaintPhotoService photoService,
            MaintenanceItemTypeRepository itemTypeRepository) {

        this.complaintService = complaintService;
        this.geminiService = geminiService;
        this.roomRepository = roomRepository;
        this.historyRepository = historyRepository;
        this.photoService = photoService;
        this.itemTypeRepository = itemTypeRepository;
    }

    // Administrator complaint list

    @GetMapping
    public String listComplaints(
            @RequestParam(name = "keyword", defaultValue = "")
            String keyword,

            @RequestParam(name = "status", defaultValue = "")
            String status,

            @RequestParam(name = "sortBy", defaultValue = "newest")
            String sortBy,

            Model model) {

        addComplaintList(model, keyword, status, sortBy);
        return "complaints/list";
    }

    // Collective Gemini analysis

    @PostMapping("/analyze-pending")
    public String analyzePendingComplaints(Model model) {

        List<Complaint> pendingComplaints =
                complaintService.searchComplaints(
                        "", "PENDING", "oldest"
                );

        Map<Long, String> submissionTimes = new HashMap<>();

        for (Complaint complaint : pendingComplaints) {

            String recordedTime = "Unknown";

            List<ComplaintStatusHistory> history =
                    historyRepository
                            .findByComplaint_IdOrderByChangedAtDesc(
                                    complaint.getId()
                            );

            for (ComplaintStatusHistory entry : history) {

                if (entry.getOldStatus() == null
                        && "PENDING".equals(entry.getNewStatus())
                        && entry.getChangedAt() != null) {

                    recordedTime =
                            entry.getChangedAt().format(UTC_FORMAT)
                                    + " UTC";
                    break;
                }
            }

            submissionTimes.put(
                    complaint.getId(),
                    recordedTime
            );
        }

        String analysis =
                geminiService.analyzePendingComplaints(
                        pendingComplaints,
                        submissionTimes
                );

        addComplaintList(model, "", "", "newest");
        model.addAttribute("priorityAnalysis", analysis);

        return "complaints/list";
    }

    private void addComplaintList(
            Model model,
            String keyword,
            String status,
            String sortBy) {

        model.addAttribute(
                "complaints",
                complaintService.searchComplaints(
                        keyword, status, sortBy
                )
        );

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("sortBy", sortBy);

        model.addAttribute(
                "pendingCount",
                complaintService.searchComplaints(
                        "", "PENDING", "oldest"
                ).size()
        );
    }

    // Complaint details

    @GetMapping("/{id}")
    public String viewComplaint(
            @PathVariable Long id,
            Model model) {

        Complaint complaint =
                complaintService.getComplaintById(id);

        addComplaintDetails(model, complaint);

        return "complaints/details";
    }

    // Protected photograph endpoint

    @GetMapping("/{id}/photo")
    @ResponseBody
    public ResponseEntity<byte[]> viewComplaintPhoto(
            @PathVariable Long id) {

        complaintService.getComplaintById(id);

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

    // New complaint form

    @GetMapping("/new")
    public String showAddForm(Model model) {

        model.addAttribute("complaint", new Complaint());

        addFormOptions(
                model, null, null, null, null
        );

        return "complaints/form";
    }

    // Submit complaint

    @Transactional
    @PostMapping("/save")
    public String saveComplaint(
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

            Model model,
            Authentication authentication) {

        Building building =
                complaintService.findBuildingById(buildingId);

        Room room = findRoomById(roomId);

        validateLocation(
                buildingId,
                building,
                form.getFloorNumber(),
                roomId,
                room,
                result
        );

        validateClassification(form, result);

        MaintenanceItemType itemType = validateItemType(
                itemTypeId,
                form.getCategory(),
                result
        );

        validatePhoto(photo, result);

        if (result.hasErrors()) {

            addFormOptions(
                    model,
                    buildingId,
                    form.getFloorNumber(),
                    roomId,
                    itemTypeId
            );

            return "complaints/form";
        }

        // Set owner, ID and initial status on the server.

        form.setId(null);
        form.setStatus("PENDING");
        form.setSubmittedBy(authentication.getName());

        form.setBuilding(building);
        form.setRoom(room);
        form.setMaintenanceItemType(itemType);

        complaintService.saveComplaint(form);

        if (form.getId() == null) {
            throw new IllegalStateException(
                    "Complaint was not saved correctly."
            );
        }

        if (photoService.hasPhoto(photo)) {
            photoService.savePhoto(form, photo);
        }

        recordStatusChange(
                form,
                null,
                "PENDING",
                authentication.getName(),
                "Complaint submitted"
        );

        return "redirect:/complaints/submitted";
    }

    // Administrator edit form

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Complaint complaint =
                complaintService.getComplaintById(id);

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
                buildingId,
                complaint.getFloorNumber(),
                roomId,
                itemTypeId
        );

        model.addAttribute(
                "hasPhoto",
                photoService.hasPhotoForComplaint(id)
        );

        return "complaints/form";
    }

    // Administrator updates complaint

    @Transactional
    @PostMapping("/update/{id}")
    public String updateComplaint(
            @PathVariable Long id,

            @Valid @ModelAttribute("complaint") Complaint form,
            BindingResult result,

            @RequestParam(name = "buildingId", required = false)
            Long buildingId,

            @RequestParam(name = "roomId", required = false)
            Long roomId,

            @RequestParam(name = "itemTypeId", required = false)
            Long itemTypeId,

            @RequestParam(name = "resolutionNote", required = false)
            String resolutionNote,

            @RequestParam(name = "photo", required = false)
            MultipartFile photo,

            Model model,
            Authentication authentication) {

        Complaint existing =
                complaintService.getComplaintById(id);

        form.setId(id);

        Building building =
                complaintService.findBuildingById(buildingId);

        Room room = findRoomById(roomId);

        validateLocation(
                buildingId,
                building,
                form.getFloorNumber(),
                roomId,
                room,
                result
        );

        validateClassification(form, result);

        MaintenanceItemType itemType = validateItemType(
                itemTypeId,
                form.getCategory(),
                result
        );

        validatePhoto(photo, result);

        String newStatus = form.getStatus();

        if (!"PENDING".equals(newStatus)
                && !"IN_PROGRESS".equals(newStatus)
                && !"COMPLETED".equals(newStatus)) {

            result.reject(
                    "status.invalid",
                    "Please select a valid complaint status."
            );
        }

        String note = resolutionNote == null
                ? ""
                : resolutionNote.trim();

        if (note.length() > 1000) {

            result.reject(
                    "note.invalid",
                    "Maintenance note cannot exceed 1000 characters."
            );
        }

        if (result.hasErrors()) {

            addFormOptions(
                    model,
                    buildingId,
                    form.getFloorNumber(),
                    roomId,
                    itemTypeId
            );

            model.addAttribute(
                    "hasPhoto",
                    photoService.hasPhotoForComplaint(id)
            );

            model.addAttribute("resolutionNote", note);

            return "complaints/form";
        }

        String oldStatus = existing.getStatus();

        existing.setTitle(form.getTitle());
        existing.setDescription(form.getDescription());
        existing.setLocation(form.getLocation());

        existing.setCategory(form.getCategory());
        existing.setPriority(form.getPriority());
        existing.setStatus(newStatus);

        existing.setBuilding(building);
        existing.setFloorNumber(form.getFloorNumber());
        existing.setRoom(room);
        existing.setMaintenanceItemType(itemType);

        // Original submittedBy is preserved.

        complaintService.saveComplaint(existing);

        // An empty photo field keeps the previous photo.

        if (photoService.hasPhoto(photo)) {
            photoService.savePhoto(existing, photo);
        }

        if (!Objects.equals(oldStatus, newStatus)
                || !note.isBlank()) {

            recordStatusChange(
                    existing,
                    oldStatus,
                    newStatus,
                    authentication.getName(),
                    note.isBlank() ? null : note
            );
        }

        return "redirect:/complaints";
    }

    // Delete complaint and related records

    @Transactional
    @PostMapping("/delete/{id}")
    public String deleteComplaint(@PathVariable Long id) {

        complaintService.getComplaintById(id);

        photoService.deletePhoto(id);

        historyRepository.deleteByComplaint_Id(id);
        historyRepository.flush();

        complaintService.deleteComplaint(id);

        return "redirect:/complaints";
    }

    // Existing individual Gemini analysis

    @PostMapping("/{id}/analyze")
    public String analyzeComplaint(
            @PathVariable Long id,
            Model model) {

        Complaint complaint =
                complaintService.getComplaintById(id);

        String suggestion =
                geminiService.analyzeComplaint(complaint);

        addComplaintDetails(model, complaint);
        model.addAttribute("aiSuggestion", suggestion);

        return "complaints/details";
    }

    @GetMapping("/submitted")
    public String complaintSubmitted() {
        return "complaints/submitted";
    }

    // Personal complaint list for reporters

    @GetMapping("/mine")
    public String showMyComplaints(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "complaints",
                complaintService.getMyComplaints(
                        authentication.getName()
                )
        );

        return "complaints/my-complaints";
    }

    private void addComplaintDetails(
            Model model,
            Complaint complaint) {

        model.addAttribute("complaint", complaint);

        model.addAttribute(
                "statusHistory",
                historyRepository
                    .findByComplaint_IdOrderByChangedAtDesc(
                            complaint.getId()
                    )
        );

        model.addAttribute(
                "hasPhoto",
                photoService.hasPhotoForComplaint(
                        complaint.getId()
                )
        );
    }

    // Validate category and priority values

    private void validateClassification(
            Complaint form,
            BindingResult result) {

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
    }

    // Ensure selected equipment exists, is active,
    // and belongs to the chosen issue category.

    private MaintenanceItemType validateItemType(
            Long itemTypeId,
            String category,
            BindingResult result) {

        if (itemTypeId == null) {
            return null;
        }

        MaintenanceItemType itemType =
                itemTypeRepository.findById(itemTypeId)
                        .orElse(null);

        if (itemType == null || !itemType.isActive()) {

            result.reject(
                    "item.invalid",
                    "Please select a valid maintenance item."
            );

            return null;
        }

        if (!itemType.getCategory().equals(category)) {

            result.reject(
                    "item.category.invalid",
                    "The affected item does not match " +
                    "the selected issue category."
            );

            return null;
        }

        return itemType;
    }

    // Validate optional photograph

    private void validatePhoto(
            MultipartFile photo,
            BindingResult result) {

        if (!photoService.hasPhoto(photo)) {
            return;
        }

        try {
            photoService.validatePhoto(photo);

        } catch (IllegalArgumentException exception) {

            result.reject(
                    "photo.invalid",
                    exception.getMessage()
            );
        }
    }

    private void recordStatusChange(
            Complaint complaint,
            String previousStatus,
            String newStatus,
            String username,
            String note) {

        ComplaintStatusHistory entry =
                new ComplaintStatusHistory();

        entry.setComplaint(complaint);
        entry.setOldStatus(previousStatus);
        entry.setNewStatus(newStatus);
        entry.setChangedBy(username);
        entry.setNote(note);

        historyRepository.save(entry);
    }

    private Room findRoomById(Long roomId) {

        if (roomId == null) {
            return null;
        }

        return roomRepository.findById(roomId)
                .orElse(null);
    }

    // Validate building-floor-room relationship

    private void validateLocation(
            Long buildingId,
            Building building,
            Integer floorNumber,
            Long roomId,
            Room room,
            BindingResult result) {

        if (buildingId != null && building == null) {

            result.reject(
                    "building.invalid",
                    "Please select a valid building."
            );
        }

        if (buildingId == null && floorNumber != null) {

            result.reject(
                    "floor.invalid",
                    "Select a building before selecting a floor."
            );
        }

        if (roomId != null && room == null) {

            result.reject(
                    "room.invalid",
                    "Please select a valid room."
            );

            return;
        }

        if (room != null) {

            if (building == null
                    || !room.getBuilding().getId()
                            .equals(building.getId())) {

                result.reject(
                        "room.building.invalid",
                        "Room does not belong to selected building."
                );
            }

            if (floorNumber == null
                    || room.getFloorNumber() != floorNumber) {

                result.reject(
                        "room.floor.invalid",
                        "Room does not belong to selected floor."
                );
            }
        }
    }

    // Dropdown data for create and edit forms

    private void addFormOptions(
            Model model,
            Long buildingId,
            Integer floorNumber,
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
                                .and(Sort.by("name").ascending())
                )
        );

        model.addAttribute(
                "itemTypes",
                itemTypeRepository.findByActiveTrueOrderByNameAsc()
        );

        model.addAttribute("selectedBuildingId", buildingId);
        model.addAttribute("selectedFloor", floorNumber);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("selectedItemTypeId", itemTypeId);
    }

    @ExceptionHandler(ComplaintNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleComplaintNotFound(
            ComplaintNotFoundException exception,
            Model model) {

        model.addAttribute("message", exception.getMessage());

        return "complaints/not-found";
    }
}
