package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.example.mentoringapis.validation.CheckStringDate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

import static com.example.mentoringapis.utilities.DateTimeUtils.DEFAULT_DATE_TIME_PATTERN;

@Getter
@Setter
public class UpdateSeminarRequest {
    @Size(min = 1)
    private String name;
    private String description;
    private String location;
    private String imageUrl;
    @CheckStringDate(format = DEFAULT_DATE_TIME_PATTERN)
    private String startTime;
    @CheckStringDate(format = DEFAULT_DATE_TIME_PATTERN)
    private String endTime;
    private Integer departmentId;
    private Set<UUID> mentorIds;
    private Set<String> attachmentUrls;
}
