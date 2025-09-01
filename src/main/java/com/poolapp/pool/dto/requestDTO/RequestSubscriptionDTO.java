package com.poolapp.pool.dto.requestDTO;


import com.poolapp.pool.model.enums.SubscriptionStatus;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSubscriptionDTO {

    @Valid
    private RequestSubscriptionTypeDTO requestSubscriptionTypeDTO;

    private SubscriptionStatus status;

}

