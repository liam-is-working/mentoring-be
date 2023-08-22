package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.TopicCategory;
import com.example.mentoringapis.entities.TopicField;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicFieldResponse {
    Long id;
    String name;
    String createdDate;
    String updatedDate;

    public static TopicFieldResponse fromEntity(TopicField field){
        return TopicFieldResponse.builder()
                .name(field.getName())
                .id(field.getId())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(field.getCreatedDate()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(field.getUpdatedDate()))
                .build();
    }
}
