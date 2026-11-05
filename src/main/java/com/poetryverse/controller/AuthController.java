package com.poetryverse.controller;

import com.poetryverse.entity.User;
import com.poetryverse.entity.WritingPrompt;
import com.poetryverse.service.PoetryTemplateService;
import com.poetryverse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;
    private final PoetryTemplateService templateService;
    private final UserDetailsService userDetailsService;

    public AuthController(UserService userService,
                          PoetryTemplateService templateService,
                          UserDetailsService userDetailsService) {
        this.userService = userService;
        this.templateService = templateService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping({"/", "/index"})
    public String landingPage(Model model) {
        Optional<User> currentUser = userService.getAuthenticatedUser();
        currentUser.ifPresent(u -> model.addAttribute("currentUser", u));

        List<WritingPrompt> featuredPoems = templateService.getPromptsByCategory("PUBLIC_DOMAIN");
        model.addAttribute("featuredAnthology", featuredPoems);
        model.addAttribute("dailyQuote", templateService.getDailyQuote());
        model.addAttribute("templates", templateService.getAllTemplates());
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        if (userService.getAuthenticatedUser().isPresent()) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        if (userService.getAuthenticatedUser().isPresent()) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam String fullName,
                                     @RequestParam String username,
                                     @RequestParam String email,
                                     @RequestParam String password,
                                     @RequestParam(required = false) String confirmPassword,
                                     @RequestParam(required = false) String penName,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (confirmPassword != null && !password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match. Please verify both fields.");
            }
            User newPoet = userService.registerNewPoet(fullName, username, email, password, penName);

            // Automatically authenticate the newly registered poet
            UserDetails userDetails = userDetailsService.loadUserByUsername(newPoet.getUsername());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            redirectAttributes.addFlashAttribute("toastSuccess",
                    "Welcome to PoetryVerse, " + newPoet.getPenName() + ". Your private sanctuary is ready.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("registerError", e.getMessage());
            redirectAttributes.addFlashAttribute("prevFullName", fullName);
            redirectAttributes.addFlashAttribute("prevUsername", username);
            redirectAttributes.addFlashAttribute("prevEmail", email);
            redirectAttributes.addFlashAttribute("prevPenName", penName);
            return "redirect:/register";
        }
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String identifier,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmNewPassword,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmNewPassword)) {
                throw new IllegalArgumentException("Confirmation password does not match.");
            }
            userService.resetPasswordByIdentifier(identifier, newPassword);
            redirectAttributes.addFlashAttribute("resetSuccess",
                    "Your passphrase has been restored. You may now sign in to your sanctuary.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("resetError", e.getMessage());
            return "redirect:/login?forgot=true";
        }
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        Optional<User> currentUser = userService.getAuthenticatedUser();
        currentUser.ifPresent(u -> {
            model.addAttribute("currentUser", u);
            model.addAttribute("userSettings", userService.getOrCreateSettings(u));
        });
        model.addAttribute("activeNav", "about");
        return "about";
    }
}
