
package com.unimaintenance.maintenance.service;

import com.unimaintenance.maintenance.entity.Complaint;
import com.unimaintenance.maintenance.entity.ComplaintPhoto;
import com.unimaintenance.maintenance.repository.ComplaintPhotoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class ComplaintPhotoService {

    // Maximum allowed image size: 5 MB
    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final ComplaintPhotoRepository photoRepository;

    public ComplaintPhotoService(
            ComplaintPhotoRepository photoRepository) {

        this.photoRepository = photoRepository;
    }

    // Check whether a photograph was uploaded.

    public boolean hasPhoto(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    // Validate image size, content type and file signature.

    public void validatePhoto(MultipartFile file) {

        if (!hasPhoto(file)) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                "Photograph must be 5 MB or smaller."
            );
        }

        byte[] bytes = readBytes(file);

        String detectedType = detectContentType(bytes);

        if (detectedType == null) {
            throw new IllegalArgumentException(
                "Only valid JPG and PNG images are allowed."
            );
        }

        String declaredType = file.getContentType();

        if (declaredType == null ||
                !declaredType.equalsIgnoreCase(detectedType)) {

            throw new IllegalArgumentException(
                "The uploaded file type does not match " +
                "the image content."
            );
        }
    }

    // Save a new photo or replace an existing photo.

    @Transactional
    public void savePhoto(
            Complaint complaint,
            MultipartFile file) {

        if (!hasPhoto(file)) {
            return;
        }

        validatePhoto(file);

        if (complaint == null || complaint.getId() == null) {
            throw new IllegalArgumentException(
                "The complaint must be saved before " +
                "uploading its photograph."
            );
        }

        byte[] bytes = readBytes(file);
        String contentType = detectContentType(bytes);

        ComplaintPhoto photo = photoRepository
                .findByComplaint_Id(complaint.getId())
                .orElseGet(ComplaintPhoto::new);

        photo.setComplaint(complaint);
        photo.setImageData(bytes);
        photo.setContentType(contentType);

        photo.setUploadedAt(
            LocalDateTime.now(ZoneOffset.UTC)
        );

        photoRepository.save(photo);
    }

    // Retrieve a stored photograph.

    @Transactional(readOnly = true)
    public Optional<ComplaintPhoto> findPhoto(Long complaintId) {

        return photoRepository.findByComplaint_Id(complaintId);
    }

    // Check whether a complaint has a photograph.

    @Transactional(readOnly = true)
    public boolean hasPhotoForComplaint(Long complaintId) {

        return photoRepository.existsByComplaint_Id(complaintId);
    }

    // Delete the photograph when the complaint is deleted.

    @Transactional
    public void deletePhoto(Long complaintId) {

        photoRepository.deleteByComplaint_Id(complaintId);
        photoRepository.flush();
    }

    // Read uploaded image bytes.

    private byte[] readBytes(MultipartFile file) {

        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                "Unable to read the uploaded photograph.",
                exception
            );
        }
    }

    // Identify JPG or PNG images using file signatures.

    private String detectContentType(byte[] bytes) {

        byte[] pngSignature = {
            (byte) 0x89, 0x50, 0x4E, 0x47,
            0x0D, 0x0A, 0x1A, 0x0A
        };

        if (bytes.length >= 8) {

            boolean isPng = true;

            for (int i = 0; i < pngSignature.length; i++) {
                if (bytes[i] != pngSignature[i]) {
                    isPng = false;
                    break;
                }
            }

            if (isPng) {
                return "image/png";
            }
        }

        if (bytes.length >= 3 &&
                (bytes[0] & 0xFF) == 0xFF &&
                (bytes[1] & 0xFF) == 0xD8 &&
                (bytes[2] & 0xFF) == 0xFF) {

            return "image/jpeg";
        }

        return null;
    }
}
