package com.example.mentoringapis.models.upStreamModels;

import lombok.Data;

@Data
public class UpdateTopicRequest {
    String name=null;
    Long fieldId=null;
    Long categoryId=null;
}
