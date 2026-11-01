
package com.unimaintenance.maintenance;

import com.unimaintenance.maintenance.entity.Building;
import com.unimaintenance.maintenance.entity.Complaint;
import com.unimaintenance.maintenance.entity.ComplaintPhoto;
import com.unimaintenance.maintenance.entity.ComplaintStatusHistory;
import com.unimaintenance.maintenance.entity.MaintenanceItemType;
import com.unimaintenance.maintenance.entity.Room;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class UniversityMaintenanceApplicationTests {

    @Test
    void newComplaintHasExpectedDefaultValues() {

        Complaint complaint = new Complaint();

        assertNull(complaint.getId());
        assertEquals("PENDING", complaint.getStatus());
        assertEquals("MEDIUM", complaint.getPriority());
        assertEquals("OTHER", complaint.getCategory());
    }

    @Test
    void complaintCanReferenceBuildingAndRoom() {

        Building building = new Building();
        building.setId(1L);
        building.setName("Auditorium Building");

        Room room = new Room();
        room.setId(5L);
        room.setName("Lecture Theatre 1");
        room.setFloorNumber(1);
        room.setRoomType("LECTURE_THEATRE");
        room.setBuilding(building);

        Complaint complaint = new Complaint();
        complaint.setTitle("Projector not working");
        complaint.setBuilding(building);
        complaint.setFloorNumber(1);
        complaint.setRoom(room);

        assertEquals("Auditorium Building",
                complaint.getBuilding().getName());

        assertEquals(1, complaint.getFloorNumber());

        assertEquals("Lecture Theatre 1",
                complaint.getRoom().getName());

        assertEquals(building.getId(),
                complaint.getRoom().getBuilding().getId());
    }

    @Test
    void complaintCanReferenceMaintenanceItem() {

        MaintenanceItemType item =
                new MaintenanceItemType(
                        "Ceiling Fan",
                        "ELECTRICAL"
                );

        Complaint complaint = new Complaint();

        complaint.setCategory("ELECTRICAL");
        complaint.setMaintenanceItemType(item);

        assertEquals(
                "Ceiling Fan",
                complaint.getMaintenanceItemType().getName()
        );

        assertEquals(
                "ELECTRICAL",
                complaint.getMaintenanceItemType().getCategory()
        );

        assertTrue(item.isActive());
    }

    @Test
    void statusHistoryRecordsChangesAndTimestamp() {

        Complaint complaint = new Complaint();
        complaint.setId(10L);

        ComplaintStatusHistory history =
                new ComplaintStatusHistory();

        history.setComplaint(complaint);
        history.setOldStatus("PENDING");
        history.setNewStatus("IN_PROGRESS");
        history.setChangedBy("admin");
        history.setNote("Maintenance work started");

        LocalDateTime before =
                LocalDateTime.now(ZoneOffset.UTC)
                        .minusSeconds(2);

        history.beforeInsert();

        assertEquals(complaint, history.getComplaint());
        assertEquals("PENDING", history.getOldStatus());
        assertEquals("IN_PROGRESS", history.getNewStatus());
        assertEquals("admin", history.getChangedBy());

        assertEquals(
                "Maintenance work started",
                history.getNote()
        );

        assertNotNull(history.getChangedAt());
        assertFalse(history.getChangedAt().isBefore(before));
    }

    @Test
    void complaintPhotoStoresImageInformation() {

        Complaint complaint = new Complaint();
        complaint.setId(12L);

        byte[] imageData = {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF
        };

        ComplaintPhoto photo = new ComplaintPhoto();

        photo.setComplaint(complaint);
        photo.setContentType("image/jpeg");
        photo.setImageData(imageData);

        photo.onCreate();

        assertEquals(complaint, photo.getComplaint());

        assertEquals(
                "image/jpeg",
                photo.getContentType()
        );

        assertArrayEquals(
                imageData,
                photo.getImageData()
        );

        assertNotNull(photo.getUploadedAt());
    }
}
