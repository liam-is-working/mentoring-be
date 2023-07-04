package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.entities.Schedule;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateScheduleRequest;
import com.example.mentoringapis.models.upStreamModels.DeleteScheduleRequest;
import com.example.mentoringapis.models.upStreamModels.DetailScheduleRequest;
import com.example.mentoringapis.models.upStreamModels.ScheduleResponse;
import com.example.mentoringapis.repositories.BookingsRepository;
import com.example.mentoringapis.repositories.SchedulesRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.transform.recurrence.Frequency;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.mentoringapis.utilities.DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER;
import static com.example.mentoringapis.utilities.DateTimeUtils.DEFAULT_TIME_FORMATTER;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final SchedulesRepository schedulesRepository;
    private final UserProfileRepository userProfileRepository;
    private final BookingsRepository bookingsRepository;

    public String buildRule(CreateScheduleRequest request){
        Recur<LocalDateTime> recur = null;
        if(request.getDaily())
            recur = new Recur<>(Frequency.DAILY, Integer.MAX_VALUE);
        if(request.getWeekly())
            recur = new Recur<>(Frequency.WEEKLY, Integer.MAX_VALUE);

        return ofNullable(recur).map(Recur::toString).orElse(null);
    }

    public List<LocalDateTime> getAllOccurrencesBetween(LocalDateTime startPeriod, LocalDateTime endPeriod, Schedule schedule){
        if(schedule.getRrule()==null)
            return List.of();
        return new Recur<LocalDateTime>(schedule.getRrule()).getDates(schedule.getSeedTime(), startPeriod, endPeriod);
    }

    public void removeSchedule(UUID mentorId, DeleteScheduleRequest deleteScheduleRequest) throws ResourceNotFoundException {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));
        if(DeleteScheduleRequest.Option.valueOf(deleteScheduleRequest.getOptions()).equals(DeleteScheduleRequest.Option.ONLY)){
            var unavailableBooking = new Booking();
            unavailableBooking.setMentor(mentor);
            unavailableBooking.setStartTime(DateTimeUtils.parseDate(deleteScheduleRequest.getStartTime()));
            unavailableBooking.setStatus(Booking.Status.NOT_AVAILABLE.name());
            bookingsRepository.save(unavailableBooking);
        }else {
            var schedule = schedulesRepository.findById(deleteScheduleRequest.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find schedule with id: %s", deleteScheduleRequest.getScheduleId())));
            schedulesRepository.delete(schedule);
        }
    }

    public DetailScheduleRequest createSchedule(UUID mentorId, CreateScheduleRequest request) throws ResourceNotFoundException {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));

        var rrule = buildRule(request);

        var bookings =  bookingsRepository
                .findByMentorEqualsAndStatusIn(mentor, List.of(Booking.Status.AVAILABLE.name(), Booking.Status.NOT_AVAILABLE.name()));        var slotTime = DateTimeUtils.parseDate(request.getStartTime()).format(DEFAULT_TIME_FORMATTER);


        return ofNullable(rrule)
                .map(rule -> {
                    //remove all only booking
                    bookings.stream()
                            .filter(b -> slotTime.equals(b.getStartTime().format(DEFAULT_TIME_FORMATTER)))
                            .forEach(bookingsRepository::delete);

                    var newSchedule = new Schedule();
                    var oldSchedule = schedulesRepository.findByMentorEqualsAndSlotTimeIs(mentor,
                            DateTimeUtils.parseSlotTime(slotTime));
                    if(!oldSchedule.isEmpty()){
                        newSchedule = oldSchedule.get(0);
                    }
                    newSchedule.setMentor(mentor);
                    newSchedule.setSeedTime(DateTimeUtils.parseDate(request.getStartTime()));
                    newSchedule.setSlotTime(DateTimeUtils.parseSlotTime(slotTime));
                    newSchedule.setRrule(rrule);
                    return DetailScheduleRequest.fromScheduleEntity(schedulesRepository.save(newSchedule));
                }).orElseGet(() -> {
                    var startTime = DateTimeUtils.parseDate(request.getStartTime());
                    //remove all same only booking
                    bookings.stream()
                            .filter(b -> startTime.equals(b.getStartTime()))
                            .forEach(bookingsRepository::delete);

                    var newBooking = new Booking();
                    newBooking.setStartTime(DateTimeUtils.parseDate(request.getStartTime()));
                    newBooking.setStatus(Booking.Status.AVAILABLE.name());
                    newBooking.setMentor(mentor);
                    return DetailScheduleRequest.fromBookingEntity(bookingsRepository.save(newBooking));
                });
    }

    public ScheduleResponse getMentorScheduleBetween(UUID userId, LocalDateTime startPeriod, LocalDateTime endPeriod) throws ResourceNotFoundException {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", userId)));
        var bookings =  bookingsRepository
                .findByMentorEqualsAndStatusIn(mentor, List.of(Booking.Status.AVAILABLE.name(), Booking.Status.NOT_AVAILABLE.name()));
        var notAvailableTimeBlock = bookings.stream()
                .filter(b -> b.getStatus().equals(Booking.Status.NOT_AVAILABLE.name()))
                .map(Booking::getStartTime)
                .collect(Collectors.toList());

        var availableTimeBlock = bookings.stream()
                .filter(b -> b.getStatus().equals(Booking.Status.AVAILABLE.name()))
                .map(Booking::getStartTime)
                .collect(Collectors.toList());
        var schedules = schedulesRepository.findByMentorEquals(mentor);
        var timeSlots = schedules
                .stream()
                .parallel()
                .flatMap(s -> getAllOccurrencesBetween(startPeriod, endPeriod, s)
                        .stream()
                        .filter(timeBlock -> !notAvailableTimeBlock.contains(timeBlock))
                        .peek(timeBlock -> availableTimeBlock.removeIf(a -> a.isEqual(timeBlock)))
                        .map(timeBlock -> ScheduleResponse.TimeSlot.fromScheduleAndDate(s, timeBlock)))
                .collect(Collectors.toList());

        availableTimeBlock.forEach(timeBlock -> {
            var timeslot = ScheduleResponse.TimeSlot.fromScheduleAndDate(null, timeBlock);
            timeSlots.add(timeslot);
        });

        return ScheduleResponse.builder()
                .timeSlots(timeSlots)
                .build();
    }
}
