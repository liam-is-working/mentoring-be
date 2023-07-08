package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.utilities.DateTimeUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateExceptionDateRequest {
    Long parentId;
    String exceptionDate;
    String startTime;
    @NotNull
    Boolean remove;

    public LocalTime startTimeAsLocalTime(){
        return DateTimeUtils.parseStringToLocalTime(startTime);
    }

    public LocalDate exceptionDateAsLocalDate(){
        return DateTimeUtils.parseStringToLocalDate(exceptionDate);
    }
}
