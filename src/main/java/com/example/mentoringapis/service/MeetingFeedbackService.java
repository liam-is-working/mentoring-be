package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.MeetingFeedback;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateMeetingFeedbackRequest;
import com.example.mentoringapis.models.upStreamModels.MeetingFeedbackResponse;
import com.example.mentoringapis.models.upStreamModels.MentorAccountResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateMeetingFeedbackRequest;
import com.example.mentoringapis.repositories.BookingRepository;
import com.example.mentoringapis.repositories.MeetingFeedbackRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class MeetingFeedbackService {
    private final MeetingFeedbackRepository meetingFeedbackRepository;
    private final UserProfileRepository userProfileRepository;
    private final BookingRepository bookingRepository;

    public MeetingFeedbackResponse.MeetingFeedbackCard editFeedback(UpdateMeetingFeedbackRequest request, UUID giverId, long feedbackId) throws ResourceNotFoundException, ClientBadRequestError {
        var feedback = meetingFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find feedback with id: %s", feedbackId)));

        if(!feedback.getGiver().getAccountId().equals(giverId))
            throw new ClientBadRequestError("Not owner of feedback");

        ofNullable(request.getRating()).ifPresent(feedback::setRating);
        ofNullable(request.getContent()).ifPresent(feedback::setContent);
        feedback.setLatestDate(DateTimeUtils.nowInVietnam());

        if(request.getDelete()) {
            meetingFeedbackRepository.delete(feedback);
            return null;
        }

       return MeetingFeedbackResponse.MeetingFeedbackCard.fromEntity(meetingFeedbackRepository.save(feedback));
    }

    public MeetingFeedbackResponse.MeetingFeedbackCard createFeedback(CreateMeetingFeedbackRequest request, Long bookingId, UUID giverId) throws ResourceNotFoundException {
        var profiles = userProfileRepository.findAllById(List.of(giverId, request.getReceiver()))
                .stream().collect(Collectors.toMap(UserProfile::getAccountId, p-> p));
        if(profiles.size()!=2)
            throw new ResourceNotFoundException(String.format("Cannot find account with id: %s", request.getReceiver()));

        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find booking with id: %s", bookingId)));

        var newFeedback = new MeetingFeedback();
        newFeedback.setBooking(booking);
        newFeedback.setContent(request.getContent());
        newFeedback.setRating(request.getRating());
        newFeedback.setGiver(profiles.get(giverId));
        newFeedback.setReceiver(profiles.get(request.getReceiver()));
        newFeedback.setLatestDate(DateTimeUtils.nowInVietnam());

        meetingFeedbackRepository.save(newFeedback);

        return MeetingFeedbackResponse.MeetingFeedbackCard.fromEntity(newFeedback);
    }

    public MeetingFeedbackResponse getFeedbacks(UUID userId, Long bookingId){
        var feedbacks = ofNullable(userId)
                .map(meetingFeedbackRepository::findAllByUser)
                .orElse(meetingFeedbackRepository.findAllByBooking(bookingId));

        var feedbackCardList = feedbacks.stream().map(MeetingFeedbackResponse.MeetingFeedbackCard::fromEntity)
                .toList();

        return MeetingFeedbackResponse.builder().feedbacks(feedbackCardList).build();
    }
}
