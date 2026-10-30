
package com.unimaintenance.maintenance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHomePage(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        boolean isAdmin = false;
        boolean isStudent = false;

        if (loggedIn) {

            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            isStudent = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

            model.addAttribute("username", authentication.getName());
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStudent", isStudent);

        return "home";
    }
}
