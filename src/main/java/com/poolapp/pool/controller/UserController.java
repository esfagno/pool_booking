package com.poolapp.pool.controller;

import com.poolapp.pool.dto.UserDTO;
import com.poolapp.pool.dto.UserUpdateDTO;
import com.poolapp.pool.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/modify")
    public ResponseEntity<UserDTO> modifyUser(@Valid @RequestBody UserUpdateDTO dto) {
        UserDTO updatedUser = userService.modifyUser(dto);
        return ResponseEntity.ok(updatedUser);
    }
}