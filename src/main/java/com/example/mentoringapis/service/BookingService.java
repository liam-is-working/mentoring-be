package com.example.mentoringapis.service;

import com.example.mentoringapis.controllers.BookingController;
import com.example.mentoringapis.entities.*;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.BookingListResponse;
import com.example.mentoringapis.models.upStreamModels.CreateBookingRequest;
import com.example.mentoringapis.models.upStreamModels.MeetingLogResponse;
import com.example.mentoringapis.models.upStreamModels.UserProfileResponse;
import com.example.mentoringapis.repositories.*;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.example.mentoringapis.configurations.ConstantConfiguration.DEFAULT_REMINDER_DELAY_HOUR;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final UserProfileRepository userProfileRepository;
    private final BookingMenteeRepository bookingMenteeRepository;
    private final BookingRepository bookingRepository;
    private final TopicRepository topicRepository;
    private final MeetingLogsRepository meetingLogsRepository;
    private final MailService mailService;
    private final ScheduleService scheduleService;
    private final AppConfig appConfig;

    public boolean isAllowedToBook(UUID studentId){
        var bookingMentee = bookingMenteeRepository.findAllByMenteeId(studentId);
        return bookingMentee.stream()
                .filter(bm -> Booking.Status.REQUESTED.name().equals(bm.getBooking().getStatus()))
                .count() <= appConfig.getMaxRequestedBooking();
    }

    public List<MeetingLogResponse> getMeetingLogs(long id) {
        var logs = meetingLogsRepository.findAllByBookingId(id);
        return logs.stream().map(MeetingLogResponse::fromEntity).toList();
    }

    public void createAttendantLog(long id, List<UUID> attendantsList) {
        var attendants = userProfileRepository.findAllByIdSimplified(attendantsList);
        var bookingOptional = bookingRepository.findByBookingId(id);

        if (bookingOptional.isEmpty())
            return;

        var booking = bookingOptional.get();
        if (attendantsList.contains(booking.getMentor().getAccountId()))
            booking.setDidMentorAttend(true);

        booking.getBookingMentees().stream()
                .filter(bm -> attendantsList.contains(bm.getMenteeId()))
                .forEach(bm -> bm.setDidMenteeAttend(true));

        attendants.forEach(a -> {
            var newMeetingLog = new MeetingLog();
            newMeetingLog.setAttendant(a);
            newMeetingLog.setCreatedDate(DateTimeUtils.nowInVietnam());
            newMeetingLog.setBooking(booking);
            newMeetingLog.setMessage(String.format("%s joins the meeting", a.getFullName()));
            meetingLogsRepository.save(newMeetingLog);
        });

        bookingRepository.save(booking);
    }

    public void cancelBooking(long bookingId) {
        var booking = bookingRepository.findByBookingId(bookingId)
                .orElse(null);
        if(booking==null)
            return;

        if(Booking.Status.REQUESTED.name().equals(booking.getStatus())){
            booking.setStatus(Booking.Status.REJECTED.name());
            booking.setReasonToCancel("Automatically rejected");
            bookingRepository.save(booking);
        }
    }

    public void cancelBooking(UserProfile requester, Iterable<Booking> bookings, BookingController.UpdateStatusRequest request) {
        bookings.forEach(b -> {
            b.setStatus(Booking.Status.REJECTED.name());
            b.setReasonToCancel(request.getReason());
            b.setCancelBy(requester);
        });
        bookingRepository.saveAll(bookings);
    }

    public void acceptBooking(Iterable<Booking> bookings) {
        bookings.forEach(b -> {
//            if(!Booking.Status.REQUESTED.name().equals(b.getStatus()))
//                return;

            b.setStatus(Booking.Status.ACCEPTED.name());
            sendBookingReminderEmail(List.of(b.getId()));
        });
        bookingRepository.saveAll(bookings);
    }

    public void leaveGroupBooking(UserProfile requester, List<Long> bookingIds) {
        bookingMenteeRepository.deleteAllByIds(bookingIds, requester.getAccountId());
    }

    public void sendBookingReminderEmail(List<Long> bookingIds) {
        //Send Email
        var bookings = bookingRepository.findAllById(bookingIds);
//            bookings.forEach(b -> CompletableFuture.runAsync(() -> mailService.sendReminderEmail(b.getId())));
        bookings.forEach(b -> {
            var sendTime = b.getBookingDate().atTime(b.getStartTime()).minusMinutes(appConfig.getReminderEmailDelay());
            var zonedSendTime = ZonedDateTime.of(sendTime, DateTimeUtils.VIET_NAM_ZONE);
            var bId = b.getId();

            var tempZoneSendTime = DateTimeUtils.nowInVietnam().plusMinutes(2);
            BackgroundJob.schedule(zonedSendTime,() -> mailService.sendReminderEmail(bId));
        });
    }

    public void sendUpdateBookingEmail(List<Long> bookingIds){
        bookingIds.forEach(mailService::sendBookingMail);
    }


    public void updateBookingStatus(BookingController.UpdateStatusRequest request, UUID requesterId) throws ResourceNotFoundException {
        switch (Booking.Status.valueOf(request.getStatus())) {
            case ACCEPTED: {
                acceptBooking(bookingRepository.findAllById(request.getBookingIds()));
                CompletableFuture.runAsync(() -> sendUpdateBookingEmail(request.getBookingIds()));
                break;
            }
            case REJECTED: {
                var bookings = bookingRepository.findAllById(request.getBookingIds());
                var requester = userProfileRepository.findUserProfileByAccount_Id(requesterId)
                        .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find profile with id: %s", requesterId)));

                //cancel by admin, mentor, owner
                if (Account.Role.MENTOR.name().equalsIgnoreCase(requester.getAccount().getRole())
                        || Account.Role.ADMIN.name().equalsIgnoreCase(requester.getAccount().getRole())
                        || StreamSupport.stream(bookings.spliterator(), false).allMatch(b -> b.owner().getAccountId().equals(requesterId))) {
                    cancelBooking(requester, bookings, request);
                    sendUpdateBookingEmail(request.getBookingIds());
                    break;
                }

                //cancel by member
                leaveGroupBooking(requester, request.getBookingIds());
                break;
            }
            default:
                break;
        }

    }

    public void createBooking(CreateBookingRequest request, UUID ownerId) throws ResourceNotFoundException, ClientBadRequestError {
        if(request.getParticipants().size()>appConfig.getMaxParticipant())
            throw new ClientBadRequestError(String.format("Số lượng người cho phép cho booking: %d", appConfig.getMaxParticipant()));

        var bookingsOfMember = bookingMenteeRepository.findAllByMenteeIdInAndStatusNotRejected(request.getParticipants());
        var mentor = userProfileRepository.findUserProfileByAccount_Id(request.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", request.getMentorId())));
        var owner = userProfileRepository.findUserProfileByAccount_Id(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentee with id: %s", ownerId)));
        var topic = topicRepository.findById(request.getTopicId())
                .filter(t -> Topic.Status.ACCEPTED.name().equals(t.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find activated topic with id: %s", request.getTopicId())));

        var overLimitBooking = request.getParticipants().stream()
                .filter(id -> bookingsOfMember.stream()
                                .filter(bm -> bm.getMenteeId().equals(id)
                                        && Booking.Status.REQUESTED.name().equals(bm.getBooking().getStatus()))
                                .count() > appConfig.getMaxRequestedBooking())
                .map(id -> bookingsOfMember.stream().filter(bm -> bm.getMenteeId().equals(id)).findFirst().map(BookingMentee::getMentee).orElse(null))
                .collect(Collectors.toList());

        if(!overLimitBooking.isEmpty()){
            var error = new ClientBadRequestError("Over limit booking");
            error.setDetails(overLimitBooking.stream().map(UserProfileResponse::fromUserProfileMinimal).toList());
            throw error;
        }


        var overlappedBookings = bookingsOfMember.stream()
                .filter(bookingMentee -> {
                    var booking = bookingMentee.getBooking();
                    return request.startDateAsLocalDate().equals(booking.getBookingDate())
                            && ScheduleService.doesCollapseTimeRange(request.startTimeAsLocalTime(), request.endTimeAsLocalTime(),
                            booking.getStartTime(), booking.getEndTime())
                            && !booking.getStatus().equals(Booking.Status.REJECTED.name());
                }).map(BookingMentee::getMentee)
                .collect(Collectors.toList());

        var scheduleDates = scheduleService.getMentorScheduleBetween(request.getMentorId(), request.startDateAsLocalDate(), request.startDateAsLocalDate(), false);

        scheduleDates.getTimeSlots().stream()
                .filter(ts -> !ts.isBooked() && Objects.equals(request.getScheduleId(), ts.getScheduleId()) &&
                        request.startDateTime().equals(ts.getStartTime()) && request.endDateTime().equals(ts.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new ClientBadRequestError("Slot is not available"));

        if (!overlappedBookings.isEmpty()) {
            var error = new ClientBadRequestError("Overlapped bookings");
            error.setDetails(overlappedBookings.stream().map(UserProfileResponse::fromUserProfileMinimal).toList());
            throw error;
        }

        var newBooking = new Booking();

        newBooking.setBookingDate(request.startDateAsLocalDate());
        newBooking.setMentor(mentor);
        newBooking.setStatus(Booking.Status.REQUESTED.name());
        newBooking.setOwner(owner);
        newBooking.setTopic(topic);
        newBooking.setDescription(request.getDescription());
        newBooking.setEndTime(request.endTimeAsLocalTime());
        newBooking.setStartTime(request.startTimeAsLocalTime());

        newBooking = bookingRepository.save(newBooking);

        Set<BookingMentee> bookingMentees = new HashSet<>();
        for (UUID menteeId : request.getParticipants()) {
            var bookingMentee = new BookingMentee();
            bookingMentee.setBookingId(newBooking.getId());
            bookingMentee.setMenteeId(menteeId);
            bookingMentees.add(bookingMentee);
            if (menteeId.equals(ownerId)) {
                bookingMentee.setOwner(true);
            }
        }

        bookingMenteeRepository.saveAll(bookingMentees);

        var newBookingId = newBooking.getId();
        //mail
        BackgroundJob.schedule(Instant.now().plus(2, ChronoUnit.MINUTES), () -> mailService.sendBookingMail(newBookingId));

        //auto cancel
        var cancelTime = ZonedDateTime.of(newBooking.getBookingDate().atTime(newBooking.getStartTime()).minus(appConfig.getAutoRejectBookingDelay(), ChronoUnit.MINUTES), DateTimeUtils.VIET_NAM_ZONE);
        BackgroundJob.schedule(cancelTime, () -> cancelBooking(newBookingId));
    }


    public BookingListResponse getBooking(List<Booking> bookings) {
        return BookingListResponse.builder()
                .bookingCards(bookings.stream()
                        .map(BookingListResponse.BookingCard::fromBookingEntity)
                        .toList())
                .build();
    }

    public BookingListResponse getAllBooking(String topicName) {
        return BookingListResponse.builder()
                .bookingCards(bookingRepository.getAll(topicName).stream()
                        .map(BookingListResponse.BookingCard::fromBookingEntity)
                        .toList())
                .build();
    }

    public BookingListResponse getMentorBooking(UUID mentorId) {
        var bookings = bookingRepository.findAllByMentorId(mentorId);
        return getBooking(bookings);
    }

    public BookingListResponse getMenteeBooking(UUID menteeId) {
        var bookings = bookingMenteeRepository.findAllByMenteeId(menteeId)
                .stream()
                .map(BookingMentee::getBooking)
                .toList();
        return getBooking(bookings);
    }

    public BookingListResponse.BookingCard getBooking(long bookingId) throws ResourceNotFoundException {
        return bookingRepository.findById(bookingId)
                .map(BookingListResponse.BookingCard::fromBookingEntity)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find booking with id: %s", bookingId)));
    }
}
