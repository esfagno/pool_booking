package com.poolapp.pool.controller;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.dto.requestDTO.RequestSessionDTO;
import com.poolapp.pool.dto.validation.UpdateValidation;
import com.poolapp.pool.service.SessionService;
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
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionDTO> createSession(@Valid @RequestBody SessionDTO dto) {
        return ResponseEntity.ok(sessionService.createSession(dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<SessionDTO>> createSessions(@Valid @RequestBody List<SessionDTO> dtos) {
        List<SessionDTO> saved = sessionService.createSessions(dtos);
        return ResponseEntity.ok(saved);
    }


    @PostMapping("/search")
    public ResponseEntity<List<SessionDTO>> searchSessions(@Valid @RequestBody RequestSessionDTO filterDto) {
        return ResponseEntity.ok(sessionService.findSessionsByFilter(filterDto));
    }

    @PatchMapping
    public ResponseEntity<SessionDTO> update(@Validated(UpdateValidation.class) @RequestBody RequestSessionDTO dto) {
        return ResponseEntity.ok(sessionService.updateSession(dto));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@Valid @RequestBody RequestSessionDTO dto) {
        sessionService.deleteSession(dto);
        return ResponseEntity.noContent().build();
    }


}
