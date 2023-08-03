package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.BookingService;
import com.example.mentoringapis.service.UserProfileService;
import com.example.mentoringapis.utilities.AuthorizationUtils;
import com.example.mentoringapis.validation.EnumField;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.Optional.ofNullable;

@CrossOrigin(origins= {"*"}, maxAge = 4800, allowCredentials = "false" )
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {
    private final UserProfileService userProfileService;
    private final BookingService bookingService;

    @GetMapping("/mentors")
    public ResponseEntity<MentorListResponse> getMentors(@RequestParam String[] searchString){
        return ResponseEntity.ok(userProfileService.getMentorCards(searchString));
    }

    @GetMapping("/{bookingId}/logs")
    public ResponseEntity<List<MeetingLogResponse>> getLogs(@PathVariable long bookingId){
        return ResponseEntity.ok(bookingService.getMeetingLogs(bookingId));
    }

    @PostMapping("/attend/{bookingId}")
    public ResponseEntity attendVirtualRoom(@RequestBody UuidListRequest attendants, @PathVariable long bookingId) throws ResourceNotFoundException {
        CompletableFuture.runAsync(() -> bookingService.createAttendantLog(bookingId, attendants.getIds()));
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @PostMapping(produces = {"application/json"})
    public ResponseEntity createBooking(Authentication authentication, @RequestBody CreateBookingRequest request) throws ResourceNotFoundException, MentoringAuthenticationError {
        var currentUserId = AuthorizationUtils.getCurrentUserUuid(authentication);
        try {
            bookingService.createBooking(request, currentUserId);
        } catch (ClientBadRequestError error) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", error.getErrorMessages(),
                    "body", ofNullable(error.getDetails()).orElse("") ));
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/is-allowed/{menteeId}")
    public ResponseEntity isAllowedToBook(@PathVariable UUID menteeId){
        return ResponseEntity.ok(Map.of("result", bookingService.isAllowedToBook(menteeId)));
    }

    @PutMapping()
    public ResponseEntity updateStatus(Authentication authentication, @RequestBody UpdateStatusRequest request) throws ResourceNotFoundException, MentoringAuthenticationError {
        var currentUserId = AuthorizationUtils.getCurrentUserUuid(authentication);
        bookingService.updateBookingStatus(request, currentUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public ResponseEntity getBooking(Authentication authentication) throws MentoringAuthenticationError {
        var account = AuthorizationUtils.getCurrentUser(authentication);
        if(account.getRole().equals(Account.Role.MENTOR.name()))
            return ResponseEntity.ok(bookingService.getMentorBooking(account.getAccount().getId()));
        return ResponseEntity.ok(bookingService.getMenteeBooking(account.getAccount().getId()));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingListResponse.BookingCard> getBooking(@PathVariable long bookingId) throws ResourceNotFoundException {
        return ResponseEntity.ok(bookingService.getBooking(bookingId));
    }

    @Data
    public static class UpdateStatusRequest{
        @NotEmpty
        List<Long> bookingIds;
        String reason;
        @NotNull
        @EnumField(availableValues = {"ACCEPTED", "REJECTED", "REQUESTED"},
        message = "[ACCEPTED, REQUESTED, REJECTED]")
        String status;
    }

}
