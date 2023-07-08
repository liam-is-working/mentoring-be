package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.AvailableTime;
import com.example.mentoringapis.entities.AvailableTimeException;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class ScheduleResponse {
    @Builder
    @Data
    public static class TimeSlot{
        Long scheduleId;
        Long exceptionId;
        String startTime;
        String endTime;
        boolean enable;

        public static TimeSlot fromScheduleAndDate(Long scheduleId, Long exceptionId, LocalDateTime startTime, long duration, boolean enable){
            return TimeSlot.builder()
                    .scheduleId(scheduleId)
                    .exceptionId(exceptionId)
                    .startTime(startTime.format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .endTime(startTime.plusMinutes(duration).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .enable(enable)
                    .build();
        }

        public boolean needToRemove(TimeSlot other){
            return other.scheduleId.equals(scheduleId) && DateTimeUtils.parseDate(startTime).truncatedTo(ChronoUnit.DAYS)
                    .compareTo(DateTimeUtils.parseDate(other.startTime).truncatedTo(ChronoUnit.DAYS)) == 0;
        }
    }
    public static class Metadata{
    }
    List<TimeSlot> timeSlots;
    Metadata metadata;
}
