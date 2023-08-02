package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.*;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackReportResponse;
import com.example.mentoringapis.repositories.BookingRepository;
import com.example.mentoringapis.repositories.SeminarRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.spring.annotations.Recurring;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
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
    private final UserProfileRepository userProfileRepository;
    private final BookingRepository bookingRepository;
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

    private final JSONObject sender = new JSONObject()
            .put("Email", "lamnvse151336@fpt.edu.vn")
            .put("Name", "GrowthMe");

    private final JSONObject receiver = new JSONObject()
            .put("Email", "vulam270402@gmail.com")
            .put("Name", "GrowthMe");

    private List<JSONObject> buildRecipientsFromBooking(Booking booking){

        if(booking == null)
            return null;

        var recipients = new ArrayList<JSONObject>();
        booking.getBookingMentees().stream()
                .map(BookingMentee::getMentee)
                .forEach(mentee -> recipients.add(
                        new JSONObject()
                                .put("Email", mentee.getAccount().getEmail())
                                .put("Name", mentee.getFullName())
                ));
        recipients.add(new JSONObject()
                .put("Email", booking.getMentor().getAccount().getEmail())
                .put("Name", booking.getMentor().getFullName()));

        return recipients;
    }

    public void sendReminderEmail(long bookingId){
        var booking = bookingRepository.findById(bookingId)
                .orElse(null);
        if(booking==null || !Booking.Status.ACCEPTED.name().equals(booking.getStatus()))
            return;

        var recipients = buildRecipientsFromBooking(booking);
        recipients.forEach(recipient -> {
            var request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, sender)
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(recipient)
                                            .put(receiver)
                                    )
                                    .put(Emailv31.Message.TEMPLATEID, 4971040)
                                    .put(Emailv31.Message.TEMPLATELANGUAGE, true)
                                    .put(Emailv31.Message.SUBJECT, "Growth Me - Booking reminder")
                                    .put(Emailv31.Message.VARIABLES, generateReminderMailVariable(booking, recipient.get("Name").toString())
                                    )
                            )
                    );
            try {
                mailjetClient.post(request);
            } catch (MailjetException e) {
                log.warn("Email exception", e);
            }
        });


    }

    public void sendBookingMail(long bookingId){
        var booking = bookingRepository.findById(bookingId)
                .orElse(null);
        if(booking==null)
            return;
        var recipients = buildRecipientsFromBooking(booking);

        recipients.forEach(recipient -> {
            var request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, sender)
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(recipient)
                                            .put(receiver)
                                    )
                                    .put(Emailv31.Message.TEMPLATEID, 4964663)
                                    .put(Emailv31.Message.TEMPLATELANGUAGE, true)
                                    .put(Emailv31.Message.SUBJECT, "Growth Me - Booking update")
                                    .put(Emailv31.Message.VARIABLES, generateBookingMailVariable(booking, recipient.get("Name").toString())
                                    )
                            )
                    );
            try {
                mailjetClient.post(request);
            } catch (MailjetException e) {
                log.warn("Email exception", e);
            }
        });


    }

    private JSONObject generateReminderMailVariable(Booking booking, String firstName){
        return new JSONObject()
                .put("firstName", firstName)
                .put("bookingDate", booking.bookDateAsString())
                .put("startTime", booking.startTimeAsString())
                .put("endTime", booking.endTimeAsString())
                .put("topicName", booking.getTopic().getName())
                .put("roomLink", String.format("https://studywithmentor-swm.web.app/meeting-room/%s",booking.getId()));
    }

    private JSONObject generateBookingMailVariable(Booking booking, String firstName){
        return new JSONObject()
                .put("firstName", firstName)
                .put("bookingDate", booking.bookDateAsString())
                .put("startTime", booking.startTimeAsString())
                .put("endTime", booking.endTimeAsString())
                .put("detailLink", "https://studywithmentor-swm.web.app/booking/details/"+booking.getId())
                .put("allBookingLink", "https://studywithmentor-swm.web.app/booking/list")
                .put("status", booking.getStatus());
    }

    public void sendEmail(Account mentorAccount, Seminar seminar, SeminarFeedbackReportResponse reportResponse) throws MailjetException {
        var request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, sender)
                                .put(Emailv31.Message.TO, new JSONArray()
                                    .put(new JSONObject()
                                            .put("Email", mentorAccount.getEmail())
                                            .put("Name", mentorAccount.getUserProfile().getFullName()))
                                        .put(receiver)
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


    //TODO store question position
    private String getMentorConnectCountFromReportStatistic(UserProfile mentor, Object report, String form){
        var searchNameJPath = String.format("$.metaData[?(@.id == '%s')].fullName", mentor.getAccountId());
        List<String> searchName = JsonPath.read(form, searchNameJPath);

        var searchString = String.format(MENTOR_CONNECTING_QUESTION_FORMAT, searchName.get(0));
        var jpath = String.format("$[?(@.question == '%s')].statistics", searchString);
        List<Object> statistics = JsonPath.read(report, jpath);
        var statistic = om.convertValue(statistics.get(0), Map.class);
        return String.valueOf(statistic.get("Yes"));
    }

    private String getMentorRatingFromReportStatistic(UserProfile mentor, Object report, String form){
        var searchNameJPath = String.format("$.metaData[?(@.id == '%s')].fullName", mentor.getAccountId());
        List<String> searchName = JsonPath.read(form, searchNameJPath);

        var searchString = String.format(MENTOR_RATING_QUESTION_FORMAT, searchName.get(0));
        var jpath = String.format("$[?(@.question == '%s')].statistics", searchString);
        List<Object> statistics = JsonPath.read(report, jpath);
        var statistic = om.convertValue(statistics.get(0), SeminarFeedbackReportResponse.RatingStatistic.class);
        return String.format("%.2f",statistic.average());
    }

    public JSONObject generateVariableFromSeminarAndMentor(Account mentor, Seminar seminar, SeminarFeedbackReportResponse reportResponse){
        return new JSONObject()
                .put("fullName", mentor.getUserProfile().getFullName())
                .put("seminarName", seminar.getName())
                .put("wantToConnect", getMentorConnectCountFromReportStatistic(mentor.getUserProfile(), reportResponse.getReportStatistic(), seminar.getFeedbackForm()))
                .put("rating", getMentorRatingFromReportStatistic(mentor.getUserProfile(), reportResponse.getReportStatistic(), seminar.getFeedbackForm()))
                .put("feedbackLink", String.format("https://studywithmentor-swm.web.app/feedback-overview/%s", seminar.getId()))
                .put("loginLink", "https://studywithmentor-swm.web.app/");
    }

    public void sendInvitationEmail(UUID mentorId) {
        var mentorOptional = userProfileRepository.findUserProfileByAccount_Id(mentorId);
        if(mentorOptional.isEmpty())
            return;
        var mentor = mentorOptional.get();
        var request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, sender)
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", mentor.getAccount().getEmail())
                                                .put("Name", mentor.getFullName()))
                                        .put(receiver)
                                )
                                .put(Emailv31.Message.TEMPLATEID, 4984820)
                                .put(Emailv31.Message.TEMPLATELANGUAGE, true)
                                .put(Emailv31.Message.SUBJECT, "Growth Me - Mentor Invitation")
                                .put(Emailv31.Message.VARIABLES, generateVariableForMentorInvitation(mentor)
                                )
                        )
                );
        try {
            mailjetClient.post(request);
        } catch (MailjetException e) {
            log.error("Mail exception",e);
        }
    }

    public JSONObject generateVariableForMentorInvitation(UserProfile mentorProfile){
        return new JSONObject()
                .put("fullName",mentorProfile.getFullName())
                .put("loginLink", "https://studywithmentor-swm.web.app/");
    }

    @Recurring(id = "send-invitation-email-job", cron = "0 1 * * *", zoneId = "Asia/Ho_Chi_Minh")
    @Job(name = "Sending invitation email job")
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
