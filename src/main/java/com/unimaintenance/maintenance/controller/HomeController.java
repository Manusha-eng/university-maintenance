
package com.unimaintenance.maintenance.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHomePage(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        boolean loggedIn =
                authentication != null
                && authentication.isAuthenticated()
                && !(authentication
                    instanceof AnonymousAuthenticationToken);

        boolean isAdmin = false;
        boolean isStudent = false;
        boolean isLecturer = false;
        boolean isStaff = false;

        String roleName = "Guest";

        if (loggedIn) {

            isAdmin = hasRole(authentication, "ROLE_ADMIN");

            isStudent = hasRole(
                    authentication,
                    "ROLE_STUDENT"
            );

            isLecturer = hasRole(
                    authentication,
                    "ROLE_LECTURER"
            );

            isStaff = hasRole(
                    authentication,
                    "ROLE_STAFF"
            );

            if (isAdmin) {
                roleName = "Maintenance Administrator";
            } else if (isLecturer) {
                roleName = "Lecturer";
            } else if (isStaff) {
                roleName = "Non-Academic Staff";
            } else if (isStudent) {
                roleName = "Student";
            }

            model.addAttribute(
                    "username",
                    authentication.getName()
            );
        }

        // These three roles can submit and track
        // their own maintenance requests.

        boolean isReporter =
                isStudent || isLecturer || isStaff;

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("isLecturer", isLecturer);
        model.addAttribute("isStaff", isStaff);
        model.addAttribute("isReporter", isReporter);
        model.addAttribute("roleName", roleName);

        return "home";
    }

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(role)
                );
    }
}
