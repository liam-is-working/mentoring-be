package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.entities.Schedule;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetailScheduleRequest {
    String startTime;
    Boolean daily = null;
    Boolean weekly = null;

    public static DetailScheduleRequest fromScheduleEntity(Schedule schedule){
        return DetailScheduleRequest.builder()
                .startTime(schedule.getSeedTime().format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                .build();
    }

    public static DetailScheduleRequest fromBookingEntity(Booking booking){
        return DetailScheduleRequest.builder()
                .startTime(booking.getStartTime().format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                .build();
    }
}
