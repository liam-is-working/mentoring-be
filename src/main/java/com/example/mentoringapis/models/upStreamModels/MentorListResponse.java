package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.MeetingFeedback;
import com.example.mentoringapis.entities.Topic;
import com.example.mentoringapis.entities.TopicField;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.models.upStreamModels.CreateTopicRequest;
import com.example.mentoringapis.models.upStreamModels.TopicDetailResponse;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class MentorListResponse {
    List<MentorCard> mentorCards;

    @Data
    @Builder
    public static class MentorCard{
        private String fullName;
        private String mentorId;
        private String description;
        private Integer followers;
        private List<String> availableTimes;
        private String avatarUrl;
        private String occupation;
        private List<TopicDetailResponse> topics;
        private List<String> skills;

        @JsonIgnore
        private OptionalDouble ratingOptional;
        @JsonGetter
        public String ratingString(){
            if(ratingOptional!= null && ratingOptional.isPresent())
                return String.format("%.2f", ratingOptional.getAsDouble());
            return null;
        }

        public boolean doesTopicFieldsMatch(Set<String> fields){
            if(fields == null || fields.isEmpty())
                return true;
            return topics.stream()
                    .map(TopicDetailResponse::getField)
                    .anyMatch(fields::contains);
        }

        public boolean doesTopicCatMatch(Set<String> categories){
            if(categories == null || categories.isEmpty())
                return true;
            return topics.stream()
                    .map(TopicDetailResponse::getCategory)
                    .anyMatch(categories::contains);
        }

        @JsonGetter()
        public Set<String> getSearchString() {
            var searchStrings = new HashSet<>(skills);
            searchStrings.add(occupation);
            searchStrings.addAll(topics.stream().map(TopicDetailResponse::getName).toList());
            searchStrings.addAll(topics.stream().map(TopicDetailResponse::getCategory).toList());
            searchStrings.addAll(topics.stream().map(TopicDetailResponse::getField).toList());
            searchStrings.add(fullName);
            searchStrings.removeIf(Objects::isNull);
            return searchStrings.stream().map(String::toUpperCase).collect(Collectors.toSet());
        }
    }

    @Data
    @Builder
    public static class MentorTopicCard{
        private String topicName;
        private long topicId;

        public static MentorTopicCard fromTopicEntity(Topic topic){
            return MentorTopicCard
                    .builder()
                    .topicName(topic.getName())
                    .topicId(topic.getId())
                    .build();
        }
    }
}
