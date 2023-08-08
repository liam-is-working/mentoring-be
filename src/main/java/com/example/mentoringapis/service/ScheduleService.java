package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.AvailableTime;
import com.example.mentoringapis.entities.AvailableTimeException;
import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.mailModel.MentorNotification;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.repositories.AvailableTimeExceptionRepository;
import com.example.mentoringapis.repositories.BookingRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final SchedulesRepository schedulesRepository;
    private final BookingRepository bookingRepository;
    private final AvailableTimeExceptionRepository availableTimeExceptionRepository;
    private final UserProfileRepository userProfileRepository;
    private final MailService mailService;

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

    public static List<LocalDateTime> getAllOccurrencesDateTimeBetween(LocalDateTime startPeriod, LocalDateTime endPeriod, AvailableTime availableTime){
        var resultList = new ArrayList<LocalDateTime>();
        //Schedule without repetition
        if(availableTime.getRrule()==null &&  availableTime.getStartDate().compareTo(startPeriod.toLocalDate())>=0 && availableTime.getStartDate().compareTo(endPeriod.toLocalDate())<=0){
            resultList.add(availableTime.getStartDate().atTime(availableTime.getStartTime()));
            return resultList;
        }
        else if( availableTime.getRrule() == null)
            return resultList;
        if(endPeriod.isAfter(availableTime.endDate().plusDays(1).atStartOfDay()))
            endPeriod = availableTime.endDate().plusDays(1).atStartOfDay();
        var returnList = new Recur<LocalDateTime>(availableTime.getRrule()).getDates(availableTime.getStartDate().atTime(availableTime.getStartTime()), startPeriod, endPeriod);
        returnList.removeIf(aTime -> availableTime.exceptionDates().contains(aTime.toLocalDate()));
        return returnList;
    }

    public static List<LocalDateTime> getRecentAvailableTimes(Collection<AvailableTime> availableTimes){
        var nowInVn = DateTimeUtils.nowInVietnam().toLocalDateTime().truncatedTo(ChronoUnit.DAYS);
        var twoWeekFromNowInVn = nowInVn.plusWeeks(2).plusDays(1);
        return availableTimes.stream()
                .parallel()
                .map(aT -> {
                    var available = getAllOccurrencesDateTimeBetween(nowInVn, twoWeekFromNowInVn, aT);
                    aT.exceptionDateTimes().stream()
                            .filter(date -> date.compareTo(nowInVn)>=0 && date.compareTo(twoWeekFromNowInVn)<=0)
                            .forEach(available::add);
                    return available;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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

    private boolean checkOverlapped(Set<AvailableTime> availableTimes, Collection<Booking> booked, CreateScheduleRequest request, long parentId){
        var requestEndTime = request.startTimeAsLocalTime().plusMinutes(getDuration());
        var requestRecurRule = buildRule(request);

        //remove parent if needed
        availableTimes.removeIf(aT -> aT.getId().equals(parentId));

        var overlapBooked = booked.stream()
                .filter(booking -> !booking.getStatus().equals(Booking.Status.REJECTED.name()))
                .filter(booking -> doesCollapseDateRange(booking.getBookingDate(), booking.getBookingDate(), request.startDateAsLocalDate(), request.endDateAsLocalDate()))
                .filter(booking -> doesCollapseTimeRange(booking.getStartTime(), booking.getEndTime(), request.startTimeAsLocalTime(), requestEndTime))
                .anyMatch(booking -> {
                    var requestDatesAroundBookingDate =
                            getAllOccurrencesDateBetween(booking.getBookingDate().minusMonths(1), booking.getBookingDate().plusMonths(1), requestRecurRule, request.startDateAsLocalDate(), List.of());
                    return requestDatesAroundBookingDate.contains(booking.getBookingDate());
                });

        var overlapExceptionDate = availableTimes.stream().map(AvailableTime::getAvailableTimeExceptionSet)
                .flatMap(Collection::stream)
                .filter(AvailableTimeException::isEnable)
                .filter(exc -> doesCollapseDateRange(exc.getExceptionDate(), exc.getExceptionDate(), request.startDateAsLocalDate(), request.endDateAsLocalDate()))
                .filter(exc -> doesCollapseTimeRange(exc.getStartTime(), exc.getEndTime(), request.startTimeAsLocalTime(), requestEndTime))
                .anyMatch(exc -> {
                    var requestDatesAroundExcDate =
                            getAllOccurrencesDateBetween(exc.getExceptionDate().minusMonths(1), exc.getExceptionDate().plusMonths(1), requestRecurRule, request.startDateAsLocalDate(), List.of());
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
        return overlapExceptionDate || overlapAvailableTime || overlapBooked;
    }

    public static boolean doesCollapseDateRange(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB){
        return startA.compareTo(endB)<0 && endA.compareTo(startB)>0 || (startA.equals(startB));
    }

    public static boolean doesCollapseTimeRange(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB){
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
        var bookings = bookingRepository.findAllByMentorIdAndBookingDate(mentorId, LocalDate.now());

        if(!request.getRemove()){
            var scheduleRequest = new CreateScheduleRequest();
            scheduleRequest.setStartDate(excToUpdate.getExceptionDate().format(DateTimeUtils.DEFAULT_DATE_FORMATTER));
            scheduleRequest.setStartTime(request.getStartTime());
            var isOverlapped = checkOverlapped(mentor.getAvailableTimes(),bookings, scheduleRequest, request.getParentId());

            if(isOverlapped)
                throw new ClientBadRequestError("Attempt to create overlapped schedule");

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

        var bookings = bookingRepository.findAllByMentorIdAndBookingDate(mentorId, request.exceptionDateAsLocalDate());

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
            var isOverlapped = checkOverlapped(mentor.getAvailableTimes(),bookings, scheduleRequest, request.getParentId());

            if(isOverlapped)
                throw new ClientBadRequestError("Attempt to create overlapped schedule");
            newExcAvailableTime.setStartTime(request.startTimeAsLocalTime());
            newExcAvailableTime.setEndTime(request.startTimeAsLocalTime().plusMinutes(getDuration()));
        }

        availableTimeExceptionRepository.save(newExcAvailableTime);
        return null;
    }

    public void editSchedule(UUID mentorId, long scheduleId, CreateScheduleRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));

        var bookings = bookingRepository.findAllByMentorIdAndBookingDate(mentorId, request.startDateAsLocalDate());
        var scheduleToEdit = mentor.getAvailableTimes().stream().filter(aT -> aT.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find schedule with id: %s", scheduleId)));

        var isOverlapped = checkOverlapped(mentor.getAvailableTimes(),bookings, request, scheduleId);

        if(isOverlapped)
            throw new ClientBadRequestError("Attempt to create overlapped schedule");


        var rrule = buildRule(request);

        scheduleToEdit.setStartTime(request.startTimeAsLocalTime());
        scheduleToEdit.setStartDate(request.startDateAsLocalDate());
        scheduleToEdit.setEndTime(request.startTimeAsLocalTime().plusMinutes(getDuration()));
        scheduleToEdit.setEndDate(request.endDateAsLocalDate().isEqual(LocalDate.MAX)?null:request.endDateAsLocalDate());
        scheduleToEdit.setRrule(rrule);

        //send mail
        var notification = new MentorNotification(mentor);
        CompletableFuture.runAsync(() -> mailService.sendMentorNotificationMail(notification, mentorId));

        DetailScheduleRequest.fromScheduleEntity(schedulesRepository.save(scheduleToEdit));
    }

    public void createSchedule(UUID mentorId, CreateScheduleRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));
        var bookings = bookingRepository.findAllByMentorIdAndBookingDate(mentorId, request.startDateAsLocalDate());

        var isOverlapped = checkOverlapped(mentor.getAvailableTimes(), bookings, request, -1);

        if(isOverlapped)
            throw new ClientBadRequestError("Attempt to create overlapped schedule");

        var rrule = buildRule(request);

        var newSchedule = new AvailableTime();
        newSchedule.setMentor(mentor);
        newSchedule.setStartTime(request.startTimeAsLocalTime());
        newSchedule.setEndTime(request.startTimeAsLocalTime().plusMinutes(getDuration()));
        newSchedule.setEndDate(request.endDateAsLocalDate().isEqual(LocalDate.MAX)?null:request.endDateAsLocalDate());
        newSchedule.setStartDate(request.startDateAsLocalDate());
        newSchedule.setRrule(rrule);

        //send mail
        var notification = new MentorNotification(mentor);
        CompletableFuture.runAsync(() -> mailService.sendMentorNotificationMail(notification, mentorId));

        DetailScheduleRequest.fromScheduleEntity(schedulesRepository.save(newSchedule));
    }

    public ScheduleResponse getMentorScheduleBetween(UUID userId, LocalDate startPeriod, LocalDate endPeriod, boolean showBooking) throws ResourceNotFoundException {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", userId)));
        List<Booking> bookings;
        var resultSet = new HashSet<ScheduleResponse.TimeSlot>();

        if(showBooking){
            bookings = bookingRepository.findAllByMentorIdAndBookingDate(userId, startPeriod, endPeriod);
            bookings.stream()
                    .filter(b -> !Booking.Status.REJECTED.name().equals(b.getStatus()))
                    .map(ScheduleResponse.TimeSlot::fromBooking)
                    .forEach(resultSet::add);
        }
        else{
            bookings = bookingRepository.findAllByMentorIdAndBookingDateSimplified(userId, startPeriod, endPeriod);
            bookings.stream()
                    .filter(b -> !Booking.Status.REJECTED.name().equals(b.getStatus()))
                    .map(ScheduleResponse.TimeSlot::fromBookingSimplified)
                    .forEach(resultSet::add);
        }




        mentor.getAvailableTimes()
                .stream().map(AvailableTime::getAvailableTimeExceptionSet)
                .flatMap(Collection::stream)
                .filter(exc -> exc.isEnable() && exc.getExceptionDate().compareTo(startPeriod) >= 0 && exc.getExceptionDate().compareTo(endPeriod) <= 0)
                .map(exc -> ScheduleResponse.TimeSlot.fromScheduleAndDate(exc,exc.getExceptionDate(), exc.isEnable()))
                .forEach(resultSet::add);

        mentor.getAvailableTimes()
                .stream()
                .parallel()
                .flatMap(s -> getAllOccurrencesDateTimeBetween(startPeriod.atStartOfDay(), endPeriod.plusDays(1).atStartOfDay(),s)
                        .stream()
                        .map(timeBlock -> ScheduleResponse.TimeSlot.fromScheduleAndDate(s,timeBlock.toLocalDate(), null, true)))
                .forEach(resultSet::add);

        if(!showBooking){
            resultSet.removeIf(ScheduleResponse.TimeSlot::isBooked);
        }

        return ScheduleResponse.builder()
                .timeSlots(resultSet)
                .build();
    }
}
