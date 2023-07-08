package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Data
public class EditScheduleRequest {
    String startTime;
    String startDate;
    String endDate;
    Boolean daily = false;
    Boolean weekly = false;

    public LocalTime startTimeAsLocalTime(){
        return DateTimeUtils.parseStringToLocalTime(startTime);
    }

    public LocalDate startDateAsLocalDate(){
        return DateTimeUtils.parseStringToLocalDate(startDate);
    }

    public LocalDate endDateAsLocalDate(){
        if ((!daily && !weekly) && Objects.isNull(endDate))
            endDate = startDate;
        else if (Objects.isNull(endDate))
            return LocalDate.MAX;
        return DateTimeUtils.parseStringToLocalDate(endDate);
    }
}
