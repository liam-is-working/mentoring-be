package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.MeetingLog;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;
import org.dmfs.rfc5545.DateTime;

import java.time.LocalDateTime;

import static com.example.mentoringapis.utilities.DateTimeUtils.localDateTimeStringFromZone;

@Data
@Builder
public class MeetingLogResponse {
    private long id;
    private UserProfileResponse attendant;
    private long bookingId;
    private String logDate;
    private String message;

    public static MeetingLogResponse fromEntity(MeetingLog meetingLog){
        return MeetingLogResponse.builder()
                .id(meetingLog.getId())
                .attendant(UserProfileResponse.fromUserProfileMinimal(meetingLog.getAttendant()))
                .logDate(localDateTimeStringFromZone(meetingLog.getCreatedDate()))
                .message(meetingLog.getMessage())
                .build();
    }
}
