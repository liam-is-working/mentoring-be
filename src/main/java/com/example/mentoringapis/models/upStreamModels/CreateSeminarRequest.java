package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.example.mentoringapis.validation.CheckStringDate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.example.mentoringapis.utilities.DateTimeUtils.DEFAULT_DATE_TIME_PATTERN;

@Getter
@Setter
public class CreateSeminarRequest {
    @NotNull
    @NotEmpty
    private String name;
    private String description;
    private String location;
    private String imageUrl;
    private Integer departmentId;
    private Set<String> attachmentUrls;
    @CheckStringDate(format = DEFAULT_DATE_TIME_PATTERN)
    @NotNull
    private String startTime;
    @CheckStringDate(format = DEFAULT_DATE_TIME_PATTERN)
    @NotNull
    private String endTime;
    private Set<UUID> mentorIds;

    public void validate() throws ClientBadRequestError {
        var start = DateTimeUtils.parseDate(startTime);
        var end = DateTimeUtils.parseDate(endTime);
        if(start.isAfter(end))
            throw new ClientBadRequestError("startTime is after endTime");
    }
}
