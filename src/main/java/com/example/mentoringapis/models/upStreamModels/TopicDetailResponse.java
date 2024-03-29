package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Topic;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicDetailResponse {
    Long id;
    String name;
    String description;
    String status;
    String field;
    String createdDate;
    String updatedDate;
    String category;
    Long fieldId;
    Long categoryId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    MentorAccountResponse mentor;

    public static TopicDetailResponse fromTopicEntity(Topic topic){
        return TopicDetailResponse.builder()
                .category(topic.getCategory().getName())
                .field(topic.getField().getName())
                .name(topic.getName())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(topic.getCreatedDate()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(topic.getUpdatedDate()))
                .description(topic.getDescription())
                .id(topic.getId())
                .status(topic.getStatus())
                .fieldId(topic.getField().getId())
                .categoryId(topic.getCategory().getId())
                .mentor(MentorAccountResponse.fromAccountEntity(topic.getMentor().getAccount()))
                .build();
    }

    public static TopicDetailResponse fromTopicEntityNoMentor(Topic topic){
        return TopicDetailResponse.builder()
                .category(topic.getCategory().getName())
                .field(topic.getField().getName())
                .name(topic.getName())
                .description(topic.getDescription())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(topic.getCreatedDate()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(topic.getUpdatedDate()))
                .id(topic.getId())
                .status(topic.getStatus())
                .fieldId(topic.getField().getId())
                .categoryId(topic.getCategory().getId())
                .build();
    }
}
