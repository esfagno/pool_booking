package com.poolapp.pool.controller;

import com.poolapp.pool.dto.UserUpdateDTO;
import com.poolapp.pool.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/modify")
    public ResponseEntity<Void> modifyUserAsUser(@Valid @RequestBody UserUpdateDTO dto, Principal principal) {
        userService.modifyUser(dto, principal.getName(), false);
        return ResponseEntity.ok().build();
    }

}

