package com.poolapp.pool.util;

import com.poolapp.pool.dto.SessionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSessionCapacityRequest {
    private SessionDTO sessionDTO;
    private CapacityOperation operation;
}