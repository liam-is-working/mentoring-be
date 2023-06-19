package com.example.mentoringapis.models.upStreamModels;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeminarFeedbackReportResponse {
    private Long seminarId;
    private List reportStatistic = new ArrayList<>();
    private List<String> improvements = new ArrayList<>();
    private List<String> others = new ArrayList<>();

    public SeminarFeedbackReportResponse(long seminarId){
        this.seminarId = seminarId;
    }

    @Data
    public static class YesNoStatistic {
        private Integer Yes;
        private Integer No;
    }

    @Data
    public static class RatingStatistic {
        @JsonSetter(value = "1")
        private Integer One;
        @JsonSetter(value = "2")
        private Integer Two;
        @JsonSetter(value = "3")
        private Integer Three;
        @JsonSetter(value = "4")
        private Integer Four;
        @JsonSetter(value = "5")
        private Integer Five;

        public double average(){
            return 1.0*(One + Two*2 + Three*3 + Four*4 + Five*5)/(One+Two+Three+Four+Five);
        }
    }
}
