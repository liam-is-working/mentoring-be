package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.validation.CheckStringDate;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CvInformationUpdateRequest {

    String description;

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
