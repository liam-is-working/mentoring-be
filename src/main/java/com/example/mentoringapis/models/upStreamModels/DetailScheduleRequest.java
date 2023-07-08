package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.entities.AvailableTime;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetailScheduleRequest {
    String startTime;
    Boolean daily = null;
    Boolean weekly = null;

    public static DetailScheduleRequest fromScheduleEntity(AvailableTime availableTime){
        return DetailScheduleRequest.builder()
                .startTime(availableTime.getStartTime().format(DateTimeUtils.DEFAULT_TIME_FORMATTER))
                .build();
    }

    public static DetailScheduleRequest fromBookingEntity(Booking booking){
        return DetailScheduleRequest.builder()
                .startTime(booking.getStartTime().format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                .build();
    }
}
