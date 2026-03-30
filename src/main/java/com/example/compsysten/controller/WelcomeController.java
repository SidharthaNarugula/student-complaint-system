package com.example.compsysten.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class WelcomeController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Student Complaint Tracking System");
        response.put("version", "1.0.0");
        response.put("status", "Running");
        response.put("auth", new String[]{
            "POST /api/auth/register - Register as student",
            "POST /api/auth/login - Login",
            "GET /api/auth/user - Get current user",
            "POST /api/auth/logout - Logout"
        });
        response.put("studentEndpoints", new String[]{
            "POST /api/complaints - Create complaint",
            "GET /api/complaints/user - Get my complaints"
        });
        response.put("adminEndpoints", new String[]{
            "GET /api/complaints - Get all complaints",
            "PUT /api/complaints/{id} - Update complaint status",
            "DELETE /api/complaints/{id} - Delete complaint"
        });
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
