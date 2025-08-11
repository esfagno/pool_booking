package com.poolapp.pool.controller;

import com.poolapp.pool.dto.UserSubscriptionDTO;
import com.poolapp.pool.dto.requestDTO.RequestUserSubscriptionDTO;
import com.poolapp.pool.dto.validation.UpdateValidation;
import com.poolapp.pool.service.UserSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-subs")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @PostMapping
    public ResponseEntity<UserSubscriptionDTO> createUserSubscription(@Valid @RequestBody UserSubscriptionDTO dto) {
        UserSubscriptionDTO created = userSubscriptionService.createUserSubscription(dto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping
    public ResponseEntity<UserSubscriptionDTO> updateUserSubscription(
            @Validated(UpdateValidation.class) @RequestBody RequestUserSubscriptionDTO dto) {
        UserSubscriptionDTO updated = userSubscriptionService.updateUserSubscription(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserSubscription(@Valid @RequestBody RequestUserSubscriptionDTO dto) {
        userSubscriptionService.deleteUserSubscription(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<UserSubscriptionDTO>> searchUserSubscriptions(
            @Valid @RequestBody RequestUserSubscriptionDTO filterDto) {
        List<UserSubscriptionDTO> results = userSubscriptionService.findUserSubscriptionsByFilter(filterDto);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/check-expired")
    public ResponseEntity<Boolean> isUserSubscriptionExpired(@Valid @RequestBody UserSubscriptionDTO dto) {
        boolean isExpired = userSubscriptionService.isUserSubscriptionExpired(dto, java.time.LocalDateTime.now());
        return ResponseEntity.ok(isExpired);
    }

}
