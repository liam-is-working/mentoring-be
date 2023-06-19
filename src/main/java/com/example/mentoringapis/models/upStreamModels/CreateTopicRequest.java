package com.example.mentoringapis.models.upStreamModels;

import lombok.Data;

@Data
public class CreateTopicRequest {
    String name;
    Long fieldId;
    Long categoryId;
}
