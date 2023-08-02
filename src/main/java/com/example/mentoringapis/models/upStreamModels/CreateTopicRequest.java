package com.example.mentoringapis.models.upStreamModels;

import lombok.Data;

@Data
public class CreateTopicRequest {
    String name;
    String description;
    Long fieldId;
    Long categoryId;
}
