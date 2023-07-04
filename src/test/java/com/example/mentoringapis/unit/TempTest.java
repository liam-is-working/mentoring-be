package com.example.mentoringapis.unit;

import com.example.mentoringapis.configurations.MentoringApisConfig;
import com.example.mentoringapis.entities.UserProfile;
import com.jayway.jsonpath.JsonPath;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import lombok.Data;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.transform.recurrence.Frequency;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest(classes = MentoringApisConfig.class)
public class TempTest {


    @Data
    public static class ScheduleRequestBody{
        String startTime;
        Boolean daily;
        Boolean weekly;
        Boolean once;
    }

    @Data
    public static class Schedule{
        UserProfile userProfile;
        LocalDateTime seedDate;
        String rrule;
    }

    public String buildRule(ScheduleRequestBody request){
        Recur<LocalDateTime> recur = null;
        if(request.daily)
            recur = new Recur<>(Frequency.DAILY, Integer.MAX_VALUE);
        if(request.weekly)
            recur = new Recur<>(Frequency.WEEKLY, Integer.MAX_VALUE);

        return Optional.ofNullable(recur).map(Recur::toString).orElse(null);
    }

    public List<LocalDateTime> getAllOccurrencesBetween(LocalDateTime startPeriod, LocalDateTime endPeriod, Schedule schedule){
        if(schedule.rrule==null)
            return List.of();
        return new Recur<LocalDateTime>(schedule.rrule).getDates(schedule.seedDate, startPeriod, endPeriod);
    }


    public List<LocalDateTime> buildRuleICal(){
        Recur<LocalDateTime> recur = new Recur<>(Frequency.DAILY, Integer.MAX_VALUE);
        return recur.getDates(LocalDateTime.now(), LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(16));
    }

//    public List<LocalDateTime> buildMultipleRule(){
//
//    }

    @Test
    public void getAllDateOfWeek(){
        buildRuleICal();
        var newSchedule = new Schedule();
        newSchedule.seedDate = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        newSchedule.setRrule(new Recur<>(Frequency.DAILY, Integer.MAX_VALUE).toString());
        getAllOccurrencesBetween(LocalDateTime.now().plusDays(100), LocalDateTime.now().plusDays(150), newSchedule);
        System.out.println("abc");
    }

    @Test
    public void scheduleMain() throws MailjetException {
        var mailjetClient = new MailjetClient(ClientOptions.builder().apiKey("44bb45c404c866f14b70ac9b6caccff1").apiSecretKey("218b0d9666c14e0262440f90aa580015").build());
        MailjetRequest request;
        MailjetResponse response;
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "lamnvse151336@fpt.edu.vn")
                                        .put("Name", "GrowthMe"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", "vulam270403@gmail.com")
                                                .put("Name", "Lam Nguyen")))
                                .put(Emailv31.Message.SUBJECT, "Your email flight plan!")
                                .put(Emailv31.Message.TEXTPART, "Dear passenger 1, welcome to Mailjet! May the delivery force be with you!")
                                .put(Emailv31.Message.HTMLPART, "<h3>Dear passenger 1, welcome to <a href=\"https://www.mailjet.com/\">Mailjet</a>!</h3><br />May the delivery force be with you!")));
        response = mailjetClient.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }

    @Test
    public void testJsonWithSpecialChar(){
        var obj = Map.of("a", List.of("á á", "a a"));
        JsonPath.read(obj, "$.a[?(@ == 'á')]");
    }
}
