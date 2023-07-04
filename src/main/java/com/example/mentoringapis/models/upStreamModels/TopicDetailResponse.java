package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Topic;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicDetailResponse {
    Long id;
    String name;
    String description;
    String money;
    String status;
    String field;
    String category;
    Long fieldId;
    Long categoryId;
    MentorAccountResponse mentor;

    public static TopicDetailResponse fromTopicEntity(Topic topic){
        return TopicDetailResponse.builder()
                .category(topic.getCategory().getName())
                .field(topic.getField().getName())
                .name(topic.getName())
                .money(topic.getMoney())
                .description(topic.getDescription())
                .id(topic.getId())
                .status(topic.getStatus())
                .fieldId(topic.getField().getId())
                .categoryId(topic.getCategory().getId())
                .mentor(MentorAccountResponse.fromAccountEntity(topic.getMentor().getAccount()))
                .build();
    }
}
