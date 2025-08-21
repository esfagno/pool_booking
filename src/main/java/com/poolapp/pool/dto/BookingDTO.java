package com.poolapp.pool.dto;

import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.BookingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {

    @NotBlank
    @Size(max = 255)
    @Email
    private String userEmail;

    @Valid
    @NotNull
    private SessionDTO sessionDTO;

    @Valid
    private UserSubscriptionDTO userSubscriptionDTO;

    private LocalDateTime bookingTime;

    private BookingStatus status;

}
