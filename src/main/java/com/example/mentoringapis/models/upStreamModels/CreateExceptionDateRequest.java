package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.errors.ClientBadRequestError;
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

    public void validate() throws ClientBadRequestError {
        if(startTimeAsLocalTime().compareTo(LocalTime.of(23,0))>=0)
            throw new ClientBadRequestError("startTime must be before 23:00:00");
    }

    public LocalTime startTimeAsLocalTime(){
        return DateTimeUtils.parseStringToLocalTime(startTime);
    }

    public LocalDate exceptionDateAsLocalDate(){
        return DateTimeUtils.parseStringToLocalDate(exceptionDate);
    }
}
