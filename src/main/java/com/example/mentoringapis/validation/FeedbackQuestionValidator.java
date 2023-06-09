package com.example.mentoringapis.validation;

import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackRequest;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;

@Component
public class FeedbackQuestionValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return SeminarFeedbackRequest.Question.class.equals(clazz);
    }

    private final Map<String, List<Object>> typeAnswerPossibleValues =
            Map.of(
                    "YES/NO", List.of("Yes", "No"),
                    "RATING", List.of(1,2,3,4,5)
            );

    @Override
    public void validate(Object target, Errors errors) {
        var q = (SeminarFeedbackRequest.Question) target;
        if(!typeAnswerPossibleValues.get(q.getType()).contains(q.getAnswer())){
            errors.reject("answer",
                    String.format("answer: %s is not in: %s",q.getAnswer(), typeAnswerPossibleValues.get(q.getType())));
        }
    }
}
