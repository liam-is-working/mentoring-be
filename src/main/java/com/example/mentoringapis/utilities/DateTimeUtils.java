package com.example.mentoringapis.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Slf4j
public class DateTimeUtils {
    private DateTimeUtils() {
    }

    public static final String DEFAULT_DATE_TIME_PATTERN  = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_PATTERN  = "HH:mm:ss";
    public static final String DEFAULT_DATE_PATTERN  = "yyyy-MM-dd";
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER  = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);
    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER  = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER  = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static String localDateTimeStringFromZone(ZonedDateTime zonedDateTime){
        return LocalDateTime.ofInstant(zonedDateTime.toInstant(), DateTimeUtils.VIET_NAM_ZONE).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER);
    }

    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DEFAULT_DATE_TIME_PATTERN);

    public static final ZoneId VIET_NAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static ZonedDateTime nowInVietnam(){
        return ZonedDateTime.now(VIET_NAM_ZONE);
    }

    public static LocalDateTime nowInVietnamLocalDateFormat(){
        return LocalDateTime.ofInstant(nowInVietnam().toInstant(), VIET_NAM_ZONE);
    }

    public static LocalDateTime parseDate(String date) {
        try {
            return LocalDateTime.parse(date, DEFAULT_DATE_TIME_FORMATTER).truncatedTo(ChronoUnit.MINUTES);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage());
            return LocalDateTime.now();
        }
    }

    public static LocalTime parseStringToLocalTime(String time) {
        try {
            return LocalTime.parse(time, DEFAULT_TIME_FORMATTER).truncatedTo(ChronoUnit.MINUTES);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage());
            return LocalTime.now();
        }
    }

    public static LocalDate parseStringToLocalDate(String time) {
        try {
            return LocalDate.parse(time, DEFAULT_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage());
            return LocalDate.now();
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
