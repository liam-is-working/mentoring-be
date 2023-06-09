package com.example.mentoringapis.models.upStreamModels;

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
    private Set<String> attachmentUrls;
    @CheckStringDate(format = DEFAULT_DATE_TIME_PATTERN)
    @NotNull
    private String startTime;
    private Set<UUID> mentorIds;
}
