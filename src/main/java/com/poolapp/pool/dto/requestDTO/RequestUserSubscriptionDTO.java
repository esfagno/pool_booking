package com.poolapp.pool.dto.requestDTO;

import com.poolapp.pool.dto.validation.UpdateValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestUserSubscriptionDTO {

    @NotBlank(groups = UpdateValidation.class)
    @Size(max = 255)
    @Email
    private String userEmail;

    @Valid
    @NotNull
    private RequestSubscriptionDTO requestSubscriptionDTO;

}

