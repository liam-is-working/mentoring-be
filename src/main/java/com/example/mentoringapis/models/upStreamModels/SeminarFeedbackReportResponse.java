package com.example.mentoringapis.models.upStreamModels;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeminarFeedbackReportResponse {

    private Object reportStatistic;
    private List<String> improvements = new ArrayList<>();
    private List<String> others = new ArrayList<>();
}
