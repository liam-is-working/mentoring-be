package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.validation.CheckStringDate;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class CvInformationUpdateRequest {

    String description;

    public String toSearchable(){
        StringBuilder sb = new StringBuilder();
        Arrays.stream(workingExps).forEach(work -> sb.append(work.company).append(' ').append(work.position).append(' '));
        Arrays.stream(achievements).forEach(acm -> sb.append(acm.name).append(' ').append(acm.organization).append(' '));
        Arrays.stream(socialActivities).forEach(sact -> sb.append(sact.organization).append(' ').append(sact.position).append(' '));
        Arrays.stream(learningExps).forEach(learn -> sb.append(learn.school).append(' ').append(learn.major).append(' '));
        Arrays.stream(certificates).forEach(cert -> sb.append(cert.name).append(' ').append(cert.organization).append(' '));
        Arrays.stream(skills).forEach(skill -> sb.append(skill.name).append(' '));
        return sb.toString();
    }

    @Valid
    WorkingExp[] workingExps;
    @Valid
    LearningExp[] learningExps;
    @Valid
    SocialActivity[] socialActivities;
    @Valid
    Achievement[] achievements;
    @Valid
    Certificate[] certificates;
    @Valid
    Skill[] skills;

    @Getter
    @Setter
    public static class WorkingExp{
        String position;
        String company;
        boolean isWorkingHere;
        @CheckStringDate
        String startDate;
        @CheckStringDate
        String endDate;
        String description;

    }

    @Getter
    @Setter
    public static class LearningExp{
        String school;
        String major;
        @CheckStringDate
        String startDate;
        @CheckStringDate
        String endDate;
        String description;
    }

    @Getter
    @Setter
    public static class SocialActivity{
        String organization;
        String position;
        boolean isAttendingThis;
        @CheckStringDate
        String startDate;
        @CheckStringDate
        String endDate;
        String description;
    }

    @Getter
    @Setter
    public static class Achievement{
        String name;
        String organization;
        @CheckStringDate
        String achievingDate;
        String description;
    }

    @Getter
    @Setter
    public static class Certificate{
        String name;
        String organization;
        @CheckStringDate
        String achievingDate;
        @CheckStringDate
        String expiryDate;
        String description;
    }

    @Getter
    @Setter
    public static class Skill{
        String name;
        String description;
    }
}
