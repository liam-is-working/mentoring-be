package com.example.mentoringapis.models.upStreamModels;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CvInformation {
    List<WorkingExp> workingExps;
    List<LearningExp> learningExps;
    List<SocialActivity> socialActivities;
    List<Achievement> achievements;
    List<Certificate> certificates;
    List<Skill> skills;

    @Getter
    @Setter
    public static class WorkingExp{
        String position;
        String company;
        boolean isWorkingHere;
        LocalDate startDate;
        LocalDate endDate;
        String description;
    }

    @Getter
    @Setter
    public static class LearningExp{
        String school;
        String major;
        LocalDate startDate;
        LocalDate endDate;
        String description;
    }

    @Getter
    @Setter
    public static class SocialActivity{
        String organization;
        String position;
        boolean isAttendingThis;
        LocalDate startDate;
        LocalDate endDate;
        String description;
    }

    @Getter
    @Setter
    public static class Achievement{
        String name;
        String organization;
        LocalDate achievingDate;
        String description;
    }

    @Getter
    @Setter
    public static class Certificate{
        String name;
        String organization;
        LocalDate achievingDate;
        LocalDate expiryDate;
        String description;
    }

    @Getter
    @Setter
    public static class Skill{
        String name;
        String description;
    }
}
