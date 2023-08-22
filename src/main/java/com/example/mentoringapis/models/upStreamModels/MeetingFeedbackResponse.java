package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.MeetingFeedback;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import static com.example.mentoringapis.utilities.DateTimeUtils.localDateTimeStringFromZone;

@Data
@Builder
public class MeetingFeedbackResponse {
    private List<MeetingFeedbackCard> feedbacks;

    @Data
    @Builder
    public static class MeetingFeedbackCard{
        private long id;
        private String content;
        private int rating;
        private String feedbackDate;
        private String createdDate;
        private String updatedDate;
        private UserProfileResponse receiver;
        private UserProfileResponse giver;
        private BookingListResponse.BookingCard bookingCard;

        public static MeetingFeedbackCard fromEntity(MeetingFeedback entity){
            return MeetingFeedbackCard.builder()
                    .bookingCard(BookingListResponse.BookingCard.fromBookingEntity(entity.getBooking()))
                    .receiver(UserProfileResponse.fromUserProfileMinimal(entity.getReceiver()))
                    .giver(UserProfileResponse.fromUserProfileMinimal(entity.getGiver()))
                    .id(entity.getId())
                    .createdDate(localDateTimeStringFromZone(entity.getCreatedDate()))
                    .updatedDate(localDateTimeStringFromZone(entity.getUpdatedDate()))
                    .content(entity.getContent())
                    .rating(entity.getRating())
                    .feedbackDate(localDateTimeStringFromZone(entity.getLatestDate()))
                    .build();
        }
    }
}
