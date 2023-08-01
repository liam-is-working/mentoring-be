package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.validation.EnumField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SeminarFeedbackRequest implements Serializable{
    private Metadata metadata;
    @NotNull
    @NotEmpty
    @Valid
    private List<Question> results;

    public static class Metadata{

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Question{
        public Question(Question q){
            this.id = q.id;
            this.question = q.question;
            this.type = q.type;
        }

        @NotNull
        private Integer id;
        @NotNull
        @NotEmpty
        private String question;
        @EnumField(availableValues = {"YES/NO", "RATING", "TEXT"},
        message = "Possible values: YES/NO, RATING, TEXT")
        private String type;
        @NotNull
        private Object answer;
    }

    public void validate(String template, ObjectMapper om) throws JsonProcessingException, ClientBadRequestError {
        var cloneResult = results.stream()
                .map(Question::new)
                .collect(Collectors.toList());

        try {
            JSONAssert.assertEquals(om.readTree(template).at("/questions").toString(),
                    om.writeValueAsString(cloneResult), false);
        } catch (JSONException | AssertionError e) {
            throw new ClientBadRequestError(String.format("Wrong format regarding to the template\n" +
                            "Diff: \n" +
                            "----------------\n" +
                            "%s\n" +
                            "-----------------", e.getMessage()));
        }
    }
}
