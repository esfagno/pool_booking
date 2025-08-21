package com.poolapp.pool.controller;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.dto.requestDTO.BookingUpdateRequest;
import com.poolapp.pool.dto.requestDTO.RequestBookingDTO;
import com.poolapp.pool.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping//работает
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        return ResponseEntity.ok(bookingService.createBooking(bookingDTO));
    }

    @PostMapping("/search")//заработало
    public ResponseEntity<List<BookingDTO>> searchBookings(@Valid @RequestBody RequestBookingDTO filterDTO) {
        return ResponseEntity.ok(bookingService.findBookingsByFilter(filterDTO));
    }

    @PatchMapping
    public ResponseEntity<BookingDTO> updateBooking(
            @Valid @RequestBody BookingUpdateRequest updateRequest) {
        return ResponseEntity.ok(
                bookingService.updateBooking(updateRequest.getCurrentBooking(), updateRequest.getNewBooking()));
    }

    @DeleteMapping//рабоатет
    public ResponseEntity<Void> deleteBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        bookingService.deleteBooking(bookingDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel")//работает
    public ResponseEntity<Void> cancelBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        bookingService.cancelBooking(bookingDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> hasUserBooked(
            @RequestParam String userEmail,
            @RequestParam String poolName,
            @RequestParam LocalDateTime sessionStartTime) {
        return ResponseEntity.ok(bookingService.hasUserBookedForSession(userEmail, poolName, sessionStartTime));
    }

    @DeleteMapping("/by-session")
    public ResponseEntity<Void> deleteBookingsBySession(@Valid @RequestBody SessionDTO sessionDTO) {
        bookingService.deleteBookingsBySession(sessionDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count-by-session")
    public ResponseEntity<Long> countBookingsBySession(@Valid @RequestBody SessionDTO sessionDTO) {
        return ResponseEntity.ok(bookingService.countBookingsBySession(sessionDTO));
    }
}

//при создании время create update автоматически активная то есть
//новое дто + раскидать по методам сильно много кода
//а так работает+подписка тоже работает
//проверить как письмо отсылается
