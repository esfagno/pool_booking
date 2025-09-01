package com.poolapp.pool.controller;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.dto.PoolScheduleDTO;
import com.poolapp.pool.dto.RequestPoolDTO;
import com.poolapp.pool.dto.requestDTO.RequestPoolScheduleDTO;
import com.poolapp.pool.dto.validation.UpdateValidation;
import com.poolapp.pool.service.PoolService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/pools")
@RequiredArgsConstructor
@Validated
public class PoolController {
    private final PoolService poolService;

    @PostMapping
    public ResponseEntity<PoolDTO> create(@Valid @RequestBody PoolDTO dto) {
        return ResponseEntity.ok(poolService.createPool(dto));
    }

    @PostMapping("/search")
    public ResponseEntity<List<PoolDTO>> search(@Valid @RequestBody RequestPoolDTO dto) {
        return ResponseEntity.ok(poolService.searchPools(dto));
    }

    @PatchMapping
    public ResponseEntity<PoolDTO> update(@Validated(UpdateValidation.class) @RequestBody RequestPoolDTO dto) {
        return ResponseEntity.ok(poolService.updatePool(dto));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@Valid @RequestBody RequestPoolDTO dto) {
        poolService.deletePool(dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/capacity")
    public ResponseEntity<PoolDTO> updateCapacity(@Validated(UpdateValidation.class) @RequestBody RequestPoolDTO dto) {
        return ResponseEntity.ok(poolService.updateCapacity(dto));
    }

    @PutMapping("/schedule")
    public ResponseEntity<PoolScheduleDTO> createOrUpdateSchedule(@Valid @RequestBody PoolScheduleDTO dto) {
        return ResponseEntity.ok(poolService.createOrUpdateSchedule(dto));
    }

    @PatchMapping("/schedule")
    public ResponseEntity<PoolScheduleDTO> updateSchedule(@Valid @RequestBody RequestPoolScheduleDTO dto) {
        return ResponseEntity.ok(poolService.updateSchedule(dto));
    }

    @DeleteMapping("/schedule")
    public ResponseEntity<Void> deleteSchedule(@Valid @RequestBody RequestPoolScheduleDTO dto) {
        poolService.deleteScheduleByDay(dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<PoolScheduleDTO>> getSchedules(@RequestParam @NotBlank String poolName) {
        return ResponseEntity.ok(poolService.getSchedulesForPool(PoolDTO.builder().name(poolName).build()));
    }


}