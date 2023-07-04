package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Schedule;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class ScheduleResponse {
    @Builder
    @Data
    public static class TimeSlot{
        Long scheduleId = null;
        String startTime;
        String endTime;

        public static TimeSlot fromScheduleAndDate(Schedule schedule, LocalDateTime dateTime){
            return TimeSlot.builder()
                    .scheduleId(Optional.ofNullable(schedule).map(Schedule::getId).orElse(null))
                    .startTime(dateTime.format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .endTime(dateTime.plusHours(1).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .build();

        };
    }
    public static class Metadata{
    }
    List<TimeSlot> timeSlots;
    Metadata metadata;
}
