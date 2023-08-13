package com.example.mentoringapis.models.mailModel;

import com.example.mentoringapis.entities.Seminar;
import com.example.mentoringapis.entities.Topic;
import com.example.mentoringapis.entities.UserProfile;
import lombok.Data;

@Data
public class MentorNotification {
    private String message;
    private String mentorFullName;
    private String link;

    public MentorNotification(Seminar seminar, UserProfile mentor){
        message = String.format("Welcome Mrs/Mr %s for the upcoming seminar named " +
                "%s", mentor.getFullName(), seminar.getName());
        link = String.format("https://studywithmentor-swm.web.app/seminars/%s", seminar.getId());
        mentorFullName = mentor.getFullName();
    }

    public MentorNotification(Topic topic, UserProfile mentor){
        message = String.format("Mrs/Mr %s has a new interesting topic to share, check out %s ",
                mentor.getFullName(), topic.getName());
        link = String.format("https://studywithmentor-swm.web.app/cv/%s", mentor.getAccountId());
        mentorFullName = mentor.getFullName();
    }

    public MentorNotification(UserProfile mentor){
        message = String.format("Mrs/Mr %s has has change their schedule ", mentor.getFullName());
        link = String.format("https://studywithmentor-swm.web.app/cv/%s", mentor.getAccountId());
        mentorFullName = mentor.getFullName();
    }
}
