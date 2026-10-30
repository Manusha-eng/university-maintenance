
package com.unimaintenance.maintenance.exception;

public class ComplaintNotFoundException extends RuntimeException {

    public ComplaintNotFoundException(Long id) {
        super("Complaint with ID " + id + " was not found.");
    }
}
