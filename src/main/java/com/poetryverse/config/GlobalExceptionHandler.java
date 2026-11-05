package com.poetryverse.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(HttpServletRequest req, Exception ex) {
        String uri = req.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred."));
        }
        ModelAndView mav = new ModelAndView("redirect:/dashboard");
        return mav;
    }
}
