package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Seminar;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackReportResponse;
import com.example.mentoringapis.repositories.SeminarRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.BackgroundJob;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private static final String MENTOR_RATING_QUESTION_FORMAT = "Bạn đánh giá thế nào về chất lượng và chuyên môn của diễn giả %s";
    private static final String MENTOR_CONNECTING_QUESTION_FORMAT = "Bạn có muốn được kết nối với diễn giả %s sau buổi seminar này không?";
    private final MailjetClient mailjetClient;
    private final SeminarService seminarService;
    private final SeminarRepository seminarRepository;
    private final FeedbackService feedbackService;
    private final ObjectMapper om;

    @Value("${mail.delayDuration}")
    private String delayDurationInMinute;


    private int delayMinutes(){
        return Integer.parseInt(delayDurationInMinute);
    }

    private Map<Long, Seminar> accumulateSeminars(List<Long> seminarIds){
        return seminarRepository.findAllById(seminarIds).stream()
                .collect(Collectors.toMap(
                        Seminar::getId,
                        sem -> sem
                ));
    }

    public void sendEmail(Account mentorAccount, Seminar seminar, SeminarFeedbackReportResponse reportResponse) throws MailjetException {
        var request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "lamnvse151336@fpt.edu.vn")
                                        .put("Name", "GrowthMe"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                    .put(new JSONObject()
                                            .put("Email", mentorAccount.getEmail())
                                            .put("Name", mentorAccount.getUserProfile().getFullName()))
                                        .put(new JSONObject()
                                            .put("Email", "vulam270403@gmail.com")
                                            .put("Name", "Vu Lam")
                                        )
                                )
                                .put(Emailv31.Message.TEMPLATEID, 4887683)
                                .put(Emailv31.Message.TEMPLATELANGUAGE, true)
                                .put(Emailv31.Message.SUBJECT, "Growth Me - Mentor Invitation")
                                .put(Emailv31.Message.VARIABLES, generateVariableFromSeminarAndMentor(mentorAccount, seminar, reportResponse)
                                )
                        )
                );
        mailjetClient.post(request);
    }

    public List<UUID> sendEmail(Long seminarId, List<UUID> selectedMentorIds) throws IOException {
        var seminarOptional = seminarRepository.findById(seminarId);
        if(seminarOptional.isEmpty()){
            log.info(String.format("Cancel mail schedule since cannot find seminar with Id: %s", seminarId));
            return List.of();
        }
        var seminar = seminarOptional.get();
        CompletableFuture<SeminarFeedbackReportResponse> seminarReportFuture;
        try {
            seminarReportFuture = feedbackService.getFeedbackReport(seminarId).toFuture();
        }catch (ResourceNotFoundException e) {
            log.info(String.format("Cancel mail since cannot find seminar with Id: %s", seminarId));
            return List.of();
        }
        if(selectedMentorIds == null){
            selectedMentorIds = seminar.getMentors().stream().map(UserProfile::getAccountId).collect(Collectors.toList());
        }


        var seminarReport = seminarReportFuture.join();
        if(seminarReport.getReportStatistic().isEmpty()){
            log.info(String.format("Seminar %s doesnt have any feedback, cancel mail action!", seminarId));
            return List.of();
        }

        for(UserProfile mentor : seminar.getMentors()){
            if(selectedMentorIds.contains(mentor.getAccountId())){
                try {
                    sendEmail(mentor.getAccount(), seminar, seminarReport);
                } catch (MailjetException e) {
                    log.error(e.getMessage());
                    selectedMentorIds.remove(mentor.getAccountId());
                }
            }
        }

        return selectedMentorIds;
    }


    private String getMentorConnectCountFromReportStatistic(String mentorName, Object report){
        var searchString = String.format(MENTOR_CONNECTING_QUESTION_FORMAT, mentorName);
        var jpath = String.format("$[?(@.question == '%s')].statistics", searchString);
        List<Object> statistics = JsonPath.read(report, jpath);
        var statistic = om.convertValue(statistics.get(0), Map.class);
        return String.valueOf(statistic.get("Yes"));
    }

    private String getMentorRatingFromReportStatistic(String mentorName, Object report){
        var searchString = String.format(MENTOR_RATING_QUESTION_FORMAT, mentorName);
        var jpath = String.format("$[?(@.question == '%s')].statistics", searchString);
        List<Object> statistics = JsonPath.read(report, jpath);
        var statistic = om.convertValue(statistics.get(0), SeminarFeedbackReportResponse.RatingStatistic.class);
        return String.format("%.2f",statistic.average());
    }

    public JSONObject generateVariableFromSeminarAndMentor(Account mentor, Seminar seminar, SeminarFeedbackReportResponse reportResponse){
        return new JSONObject()
                .put("fullName", mentor.getUserProfile().getFullName())
                .put("seminarName", seminar.getName())
                .put("wantToConnect", getMentorConnectCountFromReportStatistic(mentor.getUserProfile().getFullName(), reportResponse.getReportStatistic()))
                .put("rating", getMentorRatingFromReportStatistic(mentor.getUserProfile().getFullName(), reportResponse.getReportStatistic()))
                .put("feedbackLink", String.format("https://studywithmentor-swm.web.app/feedback-overview/%s", seminar.getId()))
                .put("loginLink", "https://studywithmentor-swm.web.app/");
    }

//    @Recurring(id = "send-invitation-email-job", cron = "42 17 1 1 *", zoneId = "Asia/Ho_Chi_Minh")
//    @Job(name = "Sending invitation email job")
    public void sendingMailJob() throws MailjetException {
        var seminarIds = seminarService.getTodaySeminarIds();
        var seminarsMap = accumulateSeminars(seminarIds);
        seminarsMap.forEach(
                (id, seminar) -> {
                    var sendTime = seminar.getStartTime().plusMinutes(delayMinutes());
                    var zonedSendTime = ZonedDateTime.of(sendTime, DateTimeUtils.VIET_NAM_ZONE);
                    BackgroundJob.schedule(zonedSendTime,() -> sendEmail(id, null));
                }
        );
    }

}
