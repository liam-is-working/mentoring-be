package com.example.mentoringapis.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Slf4j
public class DateTimeUtils {
    private DateTimeUtils() {
    }

    public static final String DEFAULT_DATE_TIME_PATTERN  = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_PATTERN  = "HH:mm:ss";
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER  = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);
    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER  = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DEFAULT_DATE_TIME_PATTERN);

    public static final ZoneId VIET_NAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static ZonedDateTime nowInVietnam(){
        return ZonedDateTime.now(VIET_NAM_ZONE);
    }

    public static LocalDateTime parseDate(String date) {
        try {
            return LocalDateTime.parse(date, DEFAULT_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    public static LocalTime parseSlotTime(String slotTime) {
        try {
            return LocalTime.parse(slotTime, DEFAULT_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    public static LocalDateTime parseRoundDate(String date) {
        try {
            if(!date.contains(":"))
                date = date.concat(" 00:00:00");
            return LocalDateTime.parse(date, DEFAULT_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

//    private LocalDateTime parseTimestamp(String timestamp) {
//        try {
//            return DATE_TIME_FORMAT.parse(timestamp);
//        } catch (ParseException e) {
//            throw new IllegalArgumentException(e);
//        }
//    }
}
