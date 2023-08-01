package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.BookingService;
import com.example.mentoringapis.service.UserProfileService;
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
import java.util.Optional;
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
    public ResponseEntity createBooking(Authentication authentication, @RequestBody CreateBookingRequest request) throws ResourceNotFoundException {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        try {
            bookingService.createBooking(request, currentUserId);
        } catch (ClientBadRequestError error) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", error.getErrorMessages(),
                    "body", ofNullable(error.getDetails()).orElse("") ));
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping()
    public ResponseEntity updateStatus(Authentication authentication, @RequestBody UpdateStatusRequest request) throws ResourceNotFoundException {
        var account = ((CustomUserDetails) authentication.getPrincipal()).getAccount();
        bookingService.updateBookingStatus(request, account.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public ResponseEntity getBooking(Authentication authentication){
        var account = ((CustomUserDetails) authentication.getPrincipal()).getAccount();
        if(account.getRole().equals(Account.Role.MENTOR.name()))
            return ResponseEntity.ok(bookingService.getMentorBooking(account.getId()));
        return ResponseEntity.ok(bookingService.getMenteeBooking(account.getId()));
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