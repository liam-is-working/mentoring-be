package com.example.mentoringapis.models.upStreamModels;

import lombok.Data;

@Data
public class UpdateTopicRequest {
    String name;
    String description;
    Long fieldId;
    Long categoryId;
    String money;
}
