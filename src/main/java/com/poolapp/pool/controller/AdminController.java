package com.poolapp.pool.controller;

import com.poolapp.pool.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/promote")
    public ResponseEntity<Void> promoteUserToAdmin(@RequestParam String email) {
        adminService.promoteToAdmin(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/demote")
    public ResponseEntity<Void> demoteAdminToUser(@RequestParam String email) {
        adminService.demoteToUser(email);
        return ResponseEntity.ok().build();
    }
}

