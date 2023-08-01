package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.example.mentoringapis.validation.CheckStringDate;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Data
public class CreateScheduleRequest {
    @CheckStringDate(format = "HH:mm:ss")
    String startTime;
    @CheckStringDate()
    String startDate;
    @CheckStringDate()
    String endDate;
    Boolean daily = false;
    Boolean weekly = false;


    public void validate() throws ClientBadRequestError {
        if(startDateAsLocalDate().compareTo(endDateAsLocalDate())>0)
            throw new ClientBadRequestError("startDate must be before or equal endDate");
        if(daily && weekly){
            throw new ClientBadRequestError("daily-weekly, only one could be true ");
        }
    }

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
