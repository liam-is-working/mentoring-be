package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Data;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateBookingRequest {
    private UUID mentorId;
    private String startTime;
    private String endTime;
    private String startDate;
    private Long scheduleId;
    private Long exceptionId;
    private Long topicId;
    private String description;
    private List<UUID> participants;

    public String startDateTime() { return  startDateAsLocalDate().atTime(startTimeAsLocalTime()).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER);}
    public String endDateTime() { return  startDateAsLocalDate().atTime(endTimeAsLocalTime()).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER);}


    public LocalTime startTimeAsLocalTime(){
        return DateTimeUtils.parseStringToLocalTime(startTime);
    }

    public LocalDate startDateAsLocalDate(){
        return DateTimeUtils.parseStringToLocalDate(startDate);
    }

    public LocalTime endTimeAsLocalTime(){
        return DateTimeUtils.parseStringToLocalTime(endTime);
    }


}
