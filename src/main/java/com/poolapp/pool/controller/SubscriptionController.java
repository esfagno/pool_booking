package com.poolapp.pool.controller;


import com.poolapp.pool.dto.SubscriptionDTO;
import com.poolapp.pool.dto.SubscriptionTypeDTO;
import com.poolapp.pool.dto.requestDTO.RequestSubscriptionDTO;
import com.poolapp.pool.dto.requestDTO.RequestSubscriptionTypeDTO;
import com.poolapp.pool.dto.validation.UpdateValidation;
import com.poolapp.pool.service.SubscriptionService;
import com.poolapp.pool.service.SubscriptionTypeService;
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
@RequestMapping("/api/subs")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionTypeService subscriptionTypeService;

    @PostMapping
    public ResponseEntity<SubscriptionDTO> createSubscription(@Valid @RequestBody SubscriptionDTO dto) {
        return ResponseEntity.ok(subscriptionService.createSubscription(dto));
    }

    @PostMapping("/search")
    public ResponseEntity<List<SubscriptionDTO>> searchSessions(@Valid @RequestBody RequestSubscriptionDTO filterDto) {
        return ResponseEntity.ok(subscriptionService.findAllSubscriptionsByFilter(filterDto));
    }

    @PostMapping("/types")
    public ResponseEntity<SubscriptionTypeDTO> createSubscriptionType(@Valid @RequestBody SubscriptionTypeDTO dto) {
        return ResponseEntity.ok(subscriptionTypeService.createSubscriptionType(dto));
    }

    @PatchMapping("/types")
    public ResponseEntity<SubscriptionTypeDTO> update(@Validated(UpdateValidation.class) @RequestBody RequestSubscriptionTypeDTO dto) {
        return ResponseEntity.ok(subscriptionTypeService.updateSubscriptionType(dto));
    }

    @DeleteMapping("/types")
    public ResponseEntity<Void> delete(@Valid @RequestBody RequestSubscriptionTypeDTO dto) {
        subscriptionTypeService.deleteSubscriptionType(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/types/search")
    public ResponseEntity<List<SubscriptionTypeDTO>> searchSubscription(@Valid @RequestBody RequestSubscriptionTypeDTO filterDto) {
        return ResponseEntity.ok(subscriptionTypeService.findSubscriptionTypesByFilter(filterDto));
    }
}

