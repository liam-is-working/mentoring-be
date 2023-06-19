package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.Seminar;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.service.StaticResourceService;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.mentoringapis.utilities.DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER;
import static java.util.Optional.ofNullable;

@Getter
@Setter
@Builder
public class SeminarResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String imageUrl;
    private String imageLink;
    private String startTime;
    private Set<MentorAccountResponse> mentors;
    private DepartmentRes department;
    private String status;
    private String[] attachmentLinks;
    private String[] attachmentUrls;
    private Map<String, String> attachments;

    enum Status{
        FUTURE, PAST
    }

    static Status getStatus(LocalDateTime startTime){
        var startTimeInUtc = startTime.atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        var nowInUtc = ZonedDateTime.now();
        return startTimeInUtc.isAfter(nowInUtc) ? Status.FUTURE : Status.PAST;
    }

    @Data
    @Builder
    static class DepartmentRes{
        private int id;
        private String name;

        static DepartmentRes fromDepartmentEntity(Department department){
            return DepartmentRes.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .build();
        }
    }

    public static SeminarResponse fromSeminarEntity(Seminar seminarEntity, StaticResourceService staticResourceService) {
        var attachmentUrls = ofNullable(seminarEntity.getAttachmentUrl()).filter(s -> !s.isBlank()).map(s -> s.split(";")).orElse(null);
        Map<String, String> attachmentMaps = null;
        try {
            attachmentMaps = ofNullable(attachmentUrls).map(urls -> Arrays.stream(urls).collect(Collectors.toMap(
                    url -> url.substring(url.indexOf('*'), url.lastIndexOf('*')),
                    url -> url
            ))).orElse(null);
        } catch (IndexOutOfBoundsException ignored){}

        return SeminarResponse.builder()
                .id(seminarEntity.getId())
                .imageLink(seminarEntity.getImageUrl())
                .attachmentLinks(attachmentUrls)
                .description(seminarEntity.getDescription())
                .imageUrl(seminarEntity.getImageUrl())
                .attachmentUrls(attachmentUrls)
                .attachments(attachmentMaps)
                .name(seminarEntity.getName())
                .location(seminarEntity.getLocation())
                .startTime(seminarEntity.getStartTime().format(DEFAULT_DATE_TIME_FORMATTER))
                .status(getStatus(seminarEntity.getStartTime()).name())
                .mentors(seminarEntity.getMentors().stream().map(UserProfile::getAccount).map(MentorAccountResponse::fromAccountEntity).collect(Collectors.toSet()))
                .department(ofNullable(seminarEntity.getDepartment()).map(DepartmentRes::fromDepartmentEntity).orElse(null))
                .build();
    }

}
