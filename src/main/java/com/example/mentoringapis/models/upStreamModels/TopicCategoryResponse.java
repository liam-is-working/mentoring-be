package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.TopicCategory;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicCategoryResponse {
    Long id;
    String name;
    String createdDate;
    String updatedDate;

    public static TopicCategoryResponse fromEntity(TopicCategory category){
        return TopicCategoryResponse.builder()
                .name(category.getName())
                .id(category.getId())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(category.getCreatedDate()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(category.getUpdatedDate()))
                .build();
    }
}
