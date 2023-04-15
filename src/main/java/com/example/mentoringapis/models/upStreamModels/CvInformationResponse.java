package com.example.mentoringapis.models.upStreamModels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.stream.IntStream;

@Getter
@Setter
public class CvInformationResponse {

    String description;

    UUID userProfileId;

    WorkingExp[] workingExps;
    LearningExp[] learningExps;
    SocialActivity[] socialActivities;
    Achievement[] achievements;
    Certificate[] certificates;
    Skill[] skills;

    @JsonGetter()
    WorkingExp[] getWorkingExps() {
        if(workingExps == null)
            return null;
        IntStream.range(0, workingExps.length)
                .forEach(index -> workingExps[index].setIndex(index));
        return workingExps;
    }
    @JsonGetter
    LearningExp[] getLearningExps() {
        if(learningExps == null)
            return null;
        IntStream.range(0, learningExps.length)
                .forEach(index -> learningExps[index].setIndex(index));
        return learningExps;
    }
    @JsonGetter
    SocialActivity[] getSocialActivities() {
        if(socialActivities == null)
            return null;
        IntStream.range(0, socialActivities.length)
                .forEach(index -> socialActivities[index].setIndex(index));
        return socialActivities;
    }
    @JsonGetter
    Achievement[] getAchievements() {
        if(achievements == null)
            return null;
        IntStream.range(0, achievements.length)
                .forEach(index -> achievements[index].setIndex(index));
        return achievements;
    }
    @JsonGetter
    Certificate[] getCertificates() {
        if(certificates == null)
            return null;
        IntStream.range(0, certificates.length)
                .forEach(index -> certificates[index].setIndex(index));
        return certificates;
    }
    @JsonGetter
    Skill[] getSkills() {
        if(skills == null)
            return null;
        IntStream.range(0, skills.length)
                .forEach(index -> skills[index].setIndex(index));
        return skills;
    }


    @Getter
    @Setter
    public static class WorkingExp{
        Integer index;
        String position;
        String company;
        boolean isWorkingHere;
        String startDate;
        String endDate;
        String description;

    }

    @Getter
    @Setter
    public static class LearningExp{
        Integer index;
        String school;
        String major;
        String startDate;
        String endDate;
        String description;
    }

    @Getter
    @Setter
    public static class SocialActivity{
        Integer index;
        String organization;
        String position;
        boolean isAttendingThis;
        String startDate;
        String endDate;
        String description;
    }

    @Getter
    @Setter
    public static class Achievement{
        Integer index;
        String name;
        String organization;
        String achievingDate;
        String description;
    }

    @Getter
    @Setter
    public static class Certificate{
        Integer index;
        String name;
        String organization;
        String achievingDate;
        String expiryDate;
        String description;
    }

    @Getter
    @Setter
    public static class Skill{
        Integer index;
        String name;
        String description;
    }
}
