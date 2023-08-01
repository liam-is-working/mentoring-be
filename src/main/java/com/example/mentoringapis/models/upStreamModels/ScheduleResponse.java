package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.AvailableTime;
import com.example.mentoringapis.entities.AvailableTimeException;
import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Data
@Builder
public class ScheduleResponse {

    @Builder
    @Data
    public static class TimeSlot {
        BookingListResponse.BookingCard bookingCard;
        Long scheduleId;
        Long exceptionId;
        String startTime;
        String endTime;
        String startDate;
        String endDate;
        boolean isWeekly;
        boolean isDaily;
        boolean enable;
        boolean isBooked;
        String bookStatus;
        boolean belongToSeries;

        public static TimeSlot fromBooking(Booking b){
            return TimeSlot.builder()
                    .bookingCard(BookingListResponse.BookingCard.fromBookingEntity(b))
                    .startTime(b.getBookingDate().atTime(b.getStartTime()).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .endTime(b.getBookingDate().atTime(b.getEndTime()).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .bookStatus(b.getStatus())
                    .isBooked(true)
                    .build();
        }

        public static TimeSlot fromBookingSimplified(Booking b){
            return TimeSlot.builder()
                    .startTime(b.getBookingDate().atTime(b.getStartTime()).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .endTime(b.getBookingDate().atTime(b.getEndTime()).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .bookStatus(b.getStatus())
                    .isBooked(true)
                    .build();
        }

        public static TimeSlot fromScheduleAndDate(AvailableTime aT, LocalDate startDate, Long exceptionId, boolean enable){
            return TimeSlot.builder()
                    .scheduleId(aT.getId())
                    .exceptionId(exceptionId)
                    .startTime(startDate.atTime(aT.getStartTime()).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .endTime(startDate.atTime(aT.getEndTime()).format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                    .enable(enable)
                    .startDate(startDate.format(DateTimeUtils.DEFAULT_DATE_FORMATTER))
                    .endDate(Optional.ofNullable(aT.getEndDate()).map(eDate -> eDate.format(DateTimeUtils.DEFAULT_DATE_FORMATTER)).orElse(null))
                    .isDaily(Optional.ofNullable(aT.getRrule()).map(rule -> rule.contains("DAILY")).orElse(false))
                    .isWeekly(Optional.ofNullable(aT.getRrule()).map(rule -> rule.contains("WEEKLY")).orElse(false))
                    .enable(enable)
                    .belongToSeries(!Objects.isNull(aT.getRrule()))
                    .isBooked(false)
                    .build();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeSlot timeSlot = (TimeSlot) o;
            return startTime.equals(timeSlot.startTime) && Objects.equals(endTime, timeSlot.endTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startTime, endTime);
        }

    }
    public static class Metadata{
    }
    Set<TimeSlot> timeSlots;
    Metadata metadata;
}
