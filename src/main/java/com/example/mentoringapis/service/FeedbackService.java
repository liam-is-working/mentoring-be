package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Seminar;
import com.example.mentoringapis.entities.SeminarFeedback;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackReportResponse;
import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackRequest;
import com.example.mentoringapis.repositories.SeminarFeedbackRepository;
import com.example.mentoringapis.repositories.SeminarRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final SeminarRepository seminarRepository;
    private final SeminarFeedbackRepository seminarFeedbackRepository;
    private final TemplateService templateService;
    private final ObjectMapper om;
    private final WebClient webClient;
    @Value("${downstream.report-api.url}")
    private String url;

    private Seminar getSeminarById(Long id) throws ResourceNotFoundException {
        return seminarRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cant find seminar with id: " + id));

    }

    public Mono<SeminarFeedbackReportResponse> getFeedbackReport(Long seminarId) throws ResourceNotFoundException, IOException {
        var seminar = getSeminarById(seminarId);

        var feedbackResults = seminarFeedbackRepository.findSeminarFeedbackBySeminar(seminar);

        var template = templateService.renderAsString("form_1", Map.of("mentorNames", seminar.mentorNames()));
        var othersQuestionId = ((Integer) JsonPath.read(template, "$.questions.length()")) -1;
        var improvementsQuestionId = othersQuestionId-1;

        var newSeminarFeedbackRes = new SeminarFeedbackReportResponse(seminarId);

        feedbackResults.stream()
                .map(SeminarFeedback::getContent)
                .forEach(contentStr -> {
                    try {
                        var contentNode = om.readTree(contentStr);
                        var resultsString = contentNode.path("results").asText("");
                        var resultsNode = om.readTree(resultsString);
                        newSeminarFeedbackRes.getImprovements().add(resultsNode.at(String.format("/%s/answer", improvementsQuestionId)).asText(""));
                        newSeminarFeedbackRes.getOthers().add(resultsNode.at(String.format("/%s/answer", othersQuestionId)).asText(""));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                } );

        newSeminarFeedbackRes.setImprovements(newSeminarFeedbackRes.getImprovements().stream().filter(Strings::isNotBlank).collect(Collectors.toList()));
        newSeminarFeedbackRes.setOthers(newSeminarFeedbackRes.getOthers().stream().filter(Strings::isNotBlank).collect(Collectors.toList()));

        var statisticMono = webClient.method(HttpMethod.GET)
                .uri(url, seminarId)
                .retrieve()
                .bodyToMono(List.class);


        return statisticMono
                .map(statistic -> {
            newSeminarFeedbackRes.setReportStatistic(statistic);
            return newSeminarFeedbackRes;
        })
                .switchIfEmpty(Mono.just(newSeminarFeedbackRes));
    }

    public Object getFeedbackForm(Long seminarId) throws IOException, ResourceNotFoundException {
        var seminar = getSeminarById(seminarId);

        Object form;
        if(Strings.isBlank(seminar.getFeedbackForm())){
            var sortedMentorNames = seminar.getMentors().stream()
                    .map(m -> Map.of("id", m.getAccountId(),
                            "fullName", m.getFullName()))
                    .collect(Collectors.toList());
            form = templateService.render("form_1", Map.of("mentorNames", sortedMentorNames));
            seminar.setFeedbackForm(om.writeValueAsString(form));
            seminarRepository.save(seminar);
        }

        return om.readValue(seminar.getFeedbackForm(), Object.class);

    }

    public void initiateFeedback(Seminar seminar) throws IOException {
        var sortedMentorNames = seminar.mentorNames().stream().sorted().collect(Collectors.toList());
        var feedback = templateService.render("initFeedback_1", Map.of("mentorNames", sortedMentorNames));

        var feedbackResult = om.convertValue(feedback, SeminarFeedbackRequest.class);

        var tempMap = Map.of(
                "results", om.writeValueAsString(feedbackResult.getResults())
        );

        var content = om.writeValueAsString(tempMap);
        var newFeedback = new SeminarFeedback();
        newFeedback.setContent(content);
        newFeedback.setSeminar(seminar);
        seminarFeedbackRepository.save(newFeedback);
    }

    public void updateFeedback(Long seminarId, SeminarFeedbackRequest feedbackResult) throws ResourceNotFoundException, IOException, ClientBadRequestError {
        var seminar = getSeminarById(seminarId);
        var template = seminar.getFeedbackForm();
        feedbackResult.validate(template, om);

        var tempMap = Map.of(
                "results", om.writeValueAsString(feedbackResult.getResults())
        );

        var content = om.writeValueAsString(tempMap);
        var newFeedback = new SeminarFeedback();
        newFeedback.setContent(content);
        newFeedback.setSeminar(seminar);
        seminarFeedbackRepository.save(newFeedback);
    }
}
