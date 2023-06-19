package com.example.mentoringapis.unit;

import biweekly.Biweekly;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.ParseContext;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceRule;
import biweekly.property.Summary;
import biweekly.util.Duration;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import com.jayway.jsonpath.JsonPath;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class TempTest {
    @Test
    public void testIcalendar(){
        ICalendar ical = new ICalendar();
        VEvent event = new VEvent();
        Summary summary = event.setSummary("Team Meeting");
        summary.setLanguage("en-us");

        Date start = new Date();
        event.setDateStart(start);

        Duration duration = new Duration.Builder().hours(1).build();
        event.setDuration(duration);

        Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(2).build();
        event.setRecurrenceRule(recur);
        ical.addEvent(event);
        DateIterator it = event.getDateIterator(TimeZone.getDefault());
        RecurrenceRuleScribe scribe = new RecurrenceRuleScribe();
        ParseContext context = new ParseContext();
        context.setVersion(ICalVersion.V2_0);
        RecurrenceRule rrule = scribe.parseText("FREQ=WEEKLY;INTERVAL=2", null, new ICalParameters(), context);
        String str = Biweekly.write(ical).go();
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
