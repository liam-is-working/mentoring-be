package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.entities.BookingMentee;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Data
@Builder
public class BookingListResponse {
    List<BookingCard> bookingCards;
    @Data
    @Builder
    public static class BookingCard{
        private TopicDetailResponse topicDetailResponse;
        private String startTime;
        private Long id;
        private String endTime;
        private String startDate;
        private UserProfileResponse mentor;
        private UserProfileResponse owner;
        private List<UserProfileResponse> mentees;
        private String createdDate;
        private Set<UUID> attendedMentees;
        private Set<UUID> absentMentees;
        private String description;
        private UserProfileResponse cancelBy;
        private String reasonToCancel;
        private List<String> menteeNames;
        private String status;
        private boolean didMentorAttend;

        public static BookingCard fromBookingEntity(Booking booking){
            return BookingCard.builder()
                    .id(booking.getId())
                    .cancelBy(ofNullable(booking.getCancelBy()).map(UserProfileResponse::fromUserProfileMinimal).orElse(null))
                    .didMentorAttend(booking.isDidMentorAttend())
                    .attendedMentees(booking.getBookingMentees().stream().filter(BookingMentee::isDidMenteeAttend).map(BookingMentee::getMenteeId).collect(Collectors.toSet()))
                    .absentMentees(booking.getBookingMentees().stream().filter(bm -> !bm.isDidMenteeAttend()).map(BookingMentee::getMenteeId).collect(Collectors.toSet()))
                    .endTime(booking.getEndTime().format(DateTimeUtils.DEFAULT_TIME_FORMATTER))
                    .startTime(booking.getStartTime().format(DateTimeUtils.DEFAULT_TIME_FORMATTER))
                    .startDate(booking.bookDateAsString())
                    .mentees(booking.mentees().stream().map(UserProfileResponse::fromUserProfileMinimal).toList())
                    .createdDate(booking.createDateAsString())
                    .topicDetailResponse(TopicDetailResponse.fromTopicEntityNoMentor(booking.getTopic()))
                    .description(booking.getDescription())
                    .owner(UserProfileResponse.fromUserProfileMinimal(booking.owner()))
                    .mentor(UserProfileResponse.fromUserProfileMinimal(booking.getMentor()))
                    .reasonToCancel(booking.getReasonToCancel())
                    .menteeNames(booking.getBookingMentees()
                            .stream()
                            .map(BookingMentee::getMentee)
                            .map(UserProfile::getFullName).toList())
                    .status(booking.getStatus())
                    .build();
        }
    }
}
