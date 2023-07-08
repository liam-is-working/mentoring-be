package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.entities.AvailableTime;
import com.example.mentoringapis.entities.AvailableTimeException;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.repositories.AvailableTimeExceptionRepository;
import com.example.mentoringapis.repositories.BookingsRepository;
import com.example.mentoringapis.repositories.SchedulesRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.transform.recurrence.Frequency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final SchedulesRepository schedulesRepository;
    private final AvailableTimeExceptionRepository availableTimeExceptionRepository;
    private final UserProfileRepository userProfileRepository;
    private final BookingsRepository bookingsRepository;

    @Value("${mentoring.duration}")
    String duration;

    private int getDuration(){
       return Integer.parseInt(duration);
    }

    public String buildRule(CreateScheduleRequest request){
        Recur<LocalDateTime> recur = null;
        if(request.getDaily())
            recur = new Recur<>(Frequency.DAILY, Integer.MAX_VALUE);
        if(request.getWeekly())
            recur = new Recur<>(Frequency.WEEKLY, Integer.MAX_VALUE);
        return ofNullable(recur).map(Recur::toString).orElse(null);
    }

    public List<LocalDateTime> getAllOccurrencesDateTimeBetween(LocalDateTime startPeriod, LocalDateTime endPeriod, AvailableTime availableTime){
        //Schedule without repetition
        if(availableTime.getRrule()==null)
            return List.of(availableTime.getStartDate().atTime(availableTime.getStartTime()));
        return new Recur<LocalDateTime>(availableTime.getRrule()).getDates(availableTime.getStartDate().atTime(availableTime.getStartTime()), startPeriod, endPeriod);
    }

    public List<LocalDate> getAllOccurrencesDateBetween(LocalDate startPeriod, LocalDate endPeriod, String rule, LocalDate startDate, Collection<LocalDate> exception){
        if(rule == null)
            return List.of(startDate);
        var returnList = new Recur<LocalDate>(rule).getDates(startDate, startPeriod, endPeriod);
        returnList.removeAll(exception);
        return returnList;
    }

    public void removeSchedule(UUID mentorId, long scheduleId) throws ResourceNotFoundException {
        var mentor = userProfileRepository.findUserProfileByAccount_IdFetchSchedule(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));
        mentor.getAvailableTimes().stream().filter(aTime -> aTime.getId().equals(scheduleId)).findFirst().ifPresent(schedulesRepository::delete);
    };

    private boolean checkOverlapped(Set<AvailableTime> availableTimes, CreateScheduleRequest request, long parentId){
        var requestEndTime = request.startTimeAsLocalTime().plusMinutes(getDuration());
        var requestRecurRule = buildRule(request);

        //remove parent if needed
        availableTimes.removeIf(aT -> aT.getId().equals(parentId));

        var overlapExceptionDate = availableTimes.stream().map(AvailableTime::getAvailableTimeExceptionSet)
                .flatMap(Collection::stream)
                .filter(AvailableTimeException::isEnable)
                .filter(exc -> exc.getStartTime().isBefore(requestEndTime) && exc.getEndTime().isAfter(request.startTimeAsLocalTime()))
                .filter(exc -> exc.getExceptionDate().isBefore(request.endDateAsLocalDate()) && exc.getExceptionDate().isAfter(request.startDateAsLocalDate())) //collapse start/end day
                .anyMatch(exc -> {
                    var requestDatesAroundExcDate =
                            getAllOccurrencesDateBetween(exc.getExceptionDate().minusWeeks(1), exc.getExceptionDate().plusWeeks(1), requestRecurRule, request.startDateAsLocalDate(), List.of());
                    return requestDatesAroundExcDate.contains(exc.getExceptionDate());
                });

        var overlapAvailableTime = availableTimes.stream()
                .filter(aTime -> doesCollapseDateRange(aTime.getStartDate(), aTime.endDate(), request.startDateAsLocalDate(), request.endDateAsLocalDate())) //collapse time
                .filter(aTime -> doesCollapseTimeRange(aTime.getStartTime(), aTime.getEndTime(), request.startTimeAsLocalTime(), requestEndTime)) //collapse time
                .anyMatch(aTime -> {
                    //Generate dates 5 weeks start from collapsing date
                    var startOfOverlap = aTime.getStartDate().isAfter(request.startDateAsLocalDate()) ? aTime.getStartDate() : request.startDateAsLocalDate();
                    var fiveWeekAfterStart = startOfOverlap.plusWeeks(5);
                    var endOfATime = fiveWeekAfterStart.isAfter(aTime.endDate())?aTime.endDate():fiveWeekAfterStart;
                    var endOfRequest = fiveWeekAfterStart.isAfter(request.endDateAsLocalDate())?request.endDateAsLocalDate():fiveWeekAfterStart;
                    var aTimeDates = getAllOccurrencesDateBetween(startOfOverlap, endOfATime, aTime.getRrule(), aTime.getStartDate()
                            , aTime.exceptionDates());
                    var requestDates = getAllOccurrencesDateBetween(startOfOverlap, endOfRequest, requestRecurRule, request.startDateAsLocalDate(), List.of());
                    return aTimeDates.stream().anyMatch(requestDates::contains);
                });
        return overlapExceptionDate || overlapAvailableTime;
    }

    public boolean doesCollapseDateRange(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB){
        return startA.compareTo(endB)<0 && endA.compareTo(startB)>0 || (startA.equals(startB));
    }

    public boolean doesCollapseTimeRange(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB){
        return startA.compareTo(endB)<0 && endA.compareTo(startB)>0 || (startA.equals(startB));
    }

    public Collection<LocalDate> removeDates(Collection<LocalDate> total, Collection<LocalDate> toRemove){
        total.removeIf(toRemove::contains);
        return total;
    }

    public void editExceptionDate(UUID mentorId, long excId, UpdateExceptionDateRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var mentor = userProfileRepository.findUserProfileByAccount_IdFetchSchedule(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));
        var excToUpdate = mentor.getAvailableTimes().stream()
                .filter(aT -> aT.getId().equals(request.getParentId())).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find available time with id: %s", request.getParentId())))
                .getAvailableTimeExceptionSet()
                .stream().filter(exc -> exc.getId().equals(excId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find available time exc with id: %s", excId)));

        if(!request.getRemove()){
            var scheduleRequest = new CreateScheduleRequest();
            scheduleRequest.setStartDate(excToUpdate.getExceptionDate().format(DateTimeUtils.DEFAULT_DATE_FORMATTER));
            scheduleRequest.setStartTime(request.getStartTime());
            var isOverlapped = checkOverlapped(mentor.getAvailableTimes(), scheduleRequest, request.getParentId());

            if(isOverlapped)
                throw ClientBadRequestError.builder()
                        .errorMessages("Attempt to create overlapped schedule")
                        .build();

            excToUpdate.setStartTime(request.startTimeAsLocalTime());
            excToUpdate.setEndTime(request.startTimeAsLocalTime().plusMinutes(getDuration()));
        }

        excToUpdate.setEnable(!request.getRemove());
        availableTimeExceptionRepository.save(excToUpdate);

    }

    public DetailScheduleRequest addExceptionDate(UUID mentorId, CreateExceptionDateRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var mentor = userProfileRepository.findUserProfileByAccount_IdFetchSchedule(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));

        var parentAvailableTime = mentor.getAvailableTimes().stream().filter(aT -> aT.getId().equals(request.getParentId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find available time with id: %s", request.getParentId())));

        if (parentAvailableTime.exceptionDates().contains(request.exceptionDateAsLocalDate()))
            throw new ClientBadRequestError(String.format("Attempt to create two exception on same day\n ScheduleId: %s - ExcDate: %s",
                    request.getParentId(), request.getExceptionDate()));

        var newExcAvailableTime = new AvailableTimeException();
        newExcAvailableTime.setEnable(!request.getRemove());
        newExcAvailableTime.setParent(parentAvailableTime);
        newExcAvailableTime.setExceptionDate(request.exceptionDateAsLocalDate());
        newExcAvailableTime.setStartTime(parentAvailableTime.getStartTime());
        newExcAvailableTime.setEndTime(parentAvailableTime.getEndTime());

        if(!request.getRemove()){
            var scheduleRequest = new CreateScheduleRequest();
            scheduleRequest.setStartDate(request.getExceptionDate());
            scheduleRequest.setStartTime(request.getStartTime());
            var isOverlapped = checkOverlapped(mentor.getAvailableTimes(), scheduleRequest, request.getParentId());

            if(isOverlapped)
                throw ClientBadRequestError.builder()
                        .errorMessages("Attempt to create overlapped schedule")
                        .build();
            newExcAvailableTime.setStartTime(request.startTimeAsLocalTime());
            newExcAvailableTime.setStartTime(request.startTimeAsLocalTime().plusMinutes(getDuration()));
        }

        availableTimeExceptionRepository.save(newExcAvailableTime);
        return null;
    }

    public void editSchedule(UUID mentorId, long scheduleId, CreateScheduleRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));

        var scheduleToEdit = mentor.getAvailableTimes().stream().filter(aT -> aT.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find schedule with id: %s", scheduleId)));

        var isOverlapped = checkOverlapped(mentor.getAvailableTimes(), request, scheduleId);

        if(isOverlapped)
            throw ClientBadRequestError.builder()
                    .errorMessages("Attempt to create overlapped schedule")
                    .build();


        var rrule = buildRule(request);

        scheduleToEdit.setStartTime(request.startTimeAsLocalTime());
        scheduleToEdit.setStartDate(request.startDateAsLocalDate());
        scheduleToEdit.setEndTime(request.startTimeAsLocalTime().plusMinutes(getDuration()));
        scheduleToEdit.setEndDate(request.endDateAsLocalDate().isEqual(LocalDate.MAX)?null:request.endDateAsLocalDate());
        scheduleToEdit.setRrule(rrule);

        DetailScheduleRequest.fromScheduleEntity(schedulesRepository.save(scheduleToEdit));
    }


    public void createSchedule(UUID mentorId, CreateScheduleRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));

        var isOverlapped = checkOverlapped(mentor.getAvailableTimes(), request, -1);

        if(isOverlapped)
            throw ClientBadRequestError.builder()
            .errorMessages("Attempt to create overlapped schedule")
            .build();

        var rrule = buildRule(request);

        var newSchedule = new AvailableTime();
        newSchedule.setMentor(mentor);
        newSchedule.setStartTime(request.startTimeAsLocalTime());
        newSchedule.setEndTime(request.startTimeAsLocalTime().plusMinutes(getDuration()));
        newSchedule.setEndDate(request.endDateAsLocalDate().isEqual(LocalDate.MAX)?null:request.endDateAsLocalDate());
        newSchedule.setStartDate(request.startDateAsLocalDate());
        newSchedule.setRrule(rrule);
        DetailScheduleRequest.fromScheduleEntity(schedulesRepository.save(newSchedule));
    }

    public ScheduleResponse getMentorScheduleBetween(UUID userId, LocalDate startPeriod, LocalDate endPeriod) throws ResourceNotFoundException {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", userId)));
        var availableTimeSlots = mentor.getAvailableTimes()
                .stream()
                .parallel()
                .flatMap(s -> getAllOccurrencesDateTimeBetween(startPeriod.atStartOfDay(), endPeriod.plusDays(1).atStartOfDay(), s)
                        .stream()
                        .map(timeBlock -> ScheduleResponse.TimeSlot.fromScheduleAndDate(s.getId(),null, timeBlock, getDuration(), true)))
                .collect(Collectors.toList());

        mentor.getAvailableTimes()
                .stream().map(AvailableTime::getAvailableTimeExceptionSet)
                .flatMap(Collection::stream)
                .filter(exc -> exc.getExceptionDate().compareTo(startPeriod) >= 0 && exc.getExceptionDate().compareTo(endPeriod) <= 0)
                .map(exc -> ScheduleResponse.TimeSlot.fromScheduleAndDate(exc.getParent().getId(),exc.getId(), exc.getExceptionDate().atTime(exc.getStartTime()), getDuration(), exc.isEnable()))
                .forEach(slot ->
                {
                    availableTimeSlots.removeIf(s -> s.needToRemove(slot));
                    if(slot.isEnable())
                        availableTimeSlots.add(slot);
                });

        return ScheduleResponse.builder()
                .timeSlots(availableTimeSlots)
                .build();
    }
}
