package com.example.mentoringapis.service;

import com.example.mentoringapis.controllers.SeminarController;
import com.example.mentoringapis.entities.*;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.mailModel.MentorNotification;
import com.example.mentoringapis.models.paging.CustomPagingResponse;
import com.example.mentoringapis.models.upStreamModels.CreateSeminarRequest;
import com.example.mentoringapis.models.upStreamModels.SeminarResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateSeminarRequest;
import com.example.mentoringapis.repositories.*;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.google.cloud.Tuple;
import com.mailjet.client.errors.MailjetException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.spring.annotations.Recurring;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.example.mentoringapis.utilities.DateTimeUtils.*;
import static java.util.Optional.ofNullable;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
@RequiredArgsConstructor
public class SeminarService {
    private final SeminarRepository seminarRepository;
    private final AccountsRepository accountsRepository;
    private final UserProfileRepository userProfileRepository;
    private final FireStoreService fireStoreService;
    private final DepartmentRepository departmentRepository;
    private final MailService mailService;
    private final AppConfig appConfig;


    public List<Long> getTodaySeminarIds(){
        var startTime = DateTimeUtils.nowInVietnam().truncatedTo(ChronoUnit.DAYS);
        var endTime = startTime.plusDays(1);
        return seminarRepository
                .findAllByStartTimeBetween(startTime.format(DEFAULT_DATE_TIME_FORMATTER), endTime.format(DEFAULT_DATE_TIME_FORMATTER));
    }

    public List<SeminarResponse> getALlByDepartmentId(int departmentId) throws ResourceNotFoundException {
        var department = departmentRepository.findById(departmentId);
        if (department.isEmpty())
            throw new ResourceNotFoundException(String.format("Cannot find department with id: %s", departmentId));
        return seminarRepository.findAllByDepartment(department.get()).stream()
                .map(SeminarResponse::fromSeminarEntity)
                .collect(Collectors.toList());
    }

    public List<SeminarResponse> getAllByMentorId(UUID mentorId) {
        return userProfileRepository.findMentorWithSeminars(mentorId)
                .map(m -> m.getSeminars().stream().map(SeminarResponse::fromSeminarEntity).toList())
                .orElse(List.of());

    }

    public SeminarResponse getById(Long id) throws ResourceNotFoundException {
        return seminarRepository.findById(id)
                .map(SeminarResponse::fromSeminarEntity)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find Seminar with id: %s", id)));
    }

    public Long deleteSeminar(Long seminarId, Integer departmentId) throws ResourceNotFoundException, ClientBadRequestError {
        var seminarOptional = seminarRepository.findById(seminarId);
        var seminar = seminarOptional.orElseThrow(() ->  new ResourceNotFoundException(String.format("Cannot find seminar with id: %s", seminarId)));

//        if(seminar.getDepartment()==null || departmentId==null || seminar.getDepartment().getId() != departmentId)
//            throw new ClientBadRequestError("Staff doesnt belong to seminar's department");
        seminarRepository.delete(seminar);
        return seminarId;

    }

    public SeminarResponse update(UpdateSeminarRequest request, long seminarId, Integer departmentId) throws ClientBadRequestError, ResourceNotFoundException {
        Department department = null;
        if(request.getDepartmentId() != null){
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find department with id: %s", request.getDepartmentId())));
        }

        var currentSeminar = seminarRepository.findById(seminarId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find Seminar with id: %s", seminarId)));

//        if(currentSeminar.getDepartment()==null || departmentId==null || currentSeminar.getDepartment().getId() != departmentId)
//            throw new ClientBadRequestError("Staff doesnt belong to seminar's department");


        Set<UserProfile> mentorProfiles = getMentorProfiles(request.getMentorIds());

        var currentMentorIds = currentSeminar.getMentors().stream().map(UserProfile::getAccountId).collect(Collectors.toSet());
        var mentorDiffs = mentorProfiles.stream().filter(mf -> currentMentorIds.contains(mf.getAccountId())).collect(Collectors.toSet());


        ofNullable(mentorProfiles).ifPresent(currentSeminar::setMentors);
        ofNullable(request.getDescription()).ifPresent(currentSeminar::setDescription);
        ofNullable(request.getLocation()).ifPresent(currentSeminar::setLocation);
        ofNullable(request.getImageUrl()).ifPresent(currentSeminar::setImageUrl);
        ofNullable(request.getStartTime()).map(DateTimeUtils::parseDate).ifPresent(currentSeminar::setStartTime);
        ofNullable(request.getEndTime()).map(DateTimeUtils::parseDate).ifPresent(currentSeminar::setEndTime);
        ofNullable(request.getName()).ifPresent(currentSeminar::setName);
        ofNullable(department).ifPresent(currentSeminar::setDepartment);
        ofNullable(request.getAttachmentUrls()).ifPresent(urlSet -> currentSeminar.setAttachmentUrl(String.join(";", urlSet)));

        if (currentSeminar.getStartTime().isAfter(currentSeminar.getEndTime())){
            throw new ClientBadRequestError("startTime is after endTime");
        }

        seminarRepository.save(currentSeminar);

        //send notify email
        if(!mentorDiffs.isEmpty() ){
            mentorDiffs.parallelStream().forEach(
                    p -> {
                        var notification = new MentorNotification(currentSeminar, p);
                        var pId = p.getAccountId();
                        //update search vector
                        CompletableFuture.runAsync(() -> userProfileRepository.updateTsvSearch(pId.toString()));
                        //send email to follower and mentor
                        CompletableFuture.runAsync(() -> mailService.sendMentorNotificationMail(notification, pId));
                    }
            );
        }

        return SeminarResponse.fromSeminarEntity(currentSeminar);
    }

    Set<UserProfile> getMentorProfiles(Set<UUID> mentorIds) throws ClientBadRequestError {
        Set<UserProfile> mentorProfiles = null;
        if (mentorIds != null) {
            var mentorsList = mentorIds.stream().map(
                    id -> Tuple.of(id, accountsRepository.findAccountsByIdAndRole(id, Account.Role.MENTOR.name()))
            ).collect(Collectors.toList());
            if (mentorsList.stream().map(Tuple::y).anyMatch(Optional::isEmpty)) {
                var notFoundIds = mentorsList.stream()
                        .filter(tuple -> tuple.y().isEmpty())
                        .map(Tuple::x)
                        .collect(Collectors.toList());
                throw new ClientBadRequestError(String.format("MentorIds not found: %s", notFoundIds.toArray()));
            }
            mentorProfiles = mentorsList.stream().map(Tuple::y)
                    .map(Optional::get)
                    .map(Account::getUserProfile)
                    .collect(Collectors.toSet());
        }
        return mentorProfiles;
    }

    public SeminarResponse create(CreateSeminarRequest request, Integer departmentId) throws ClientBadRequestError, IOException {
        request.validate();

        Set<UserProfile> mentorProfiles = getMentorProfiles(request.getMentorIds());
        var newSeminar = new Seminar();
        ofNullable(mentorProfiles).ifPresent(newSeminar::setMentors);
        newSeminar.setDescription(request.getDescription());
        newSeminar.setImageUrl(request.getImageUrl());
        newSeminar.setLocation(request.getLocation());
        newSeminar.setStartTime(DateTimeUtils.parseDate(request.getStartTime()));
        newSeminar.setEndTime(DateTimeUtils.parseDate(request.getEndTime()));
        newSeminar.setName(request.getName());
        ofNullable(request.getAttachmentUrls()).ifPresent(urlSet -> newSeminar.setAttachmentUrl(String.join(";", urlSet)));
        if (departmentId != null) {
            var department = departmentRepository.findById(departmentId);
            newSeminar.setDepartment(department.orElse(null));
        }
        seminarRepository.save(newSeminar);
        fireStoreService.createDiscussionRoom(newSeminar.getId());

        //schedule report email
        var sendTime = newSeminar.getEndTime().plusMinutes(appConfig.getSeminarReportEmailDelay());
        var zonedSendTime = ZonedDateTime.of(sendTime, DateTimeUtils.VIET_NAM_ZONE);
        BackgroundJob.schedule(zonedSendTime,() -> mailService.sendReportEmail(newSeminar.getId(),newSeminar.getEndTime(), null));

        //send notify email
        if(mentorProfiles != null){
            mentorProfiles.parallelStream().forEach(
                    p -> {
                        var notification = new MentorNotification(newSeminar, p);
                        var pId = p.getAccountId();
                        //update search vector
                        CompletableFuture.runAsync(() -> userProfileRepository.updateTsvSearch(pId.toString()));
                        //send email to follower and mentor
                        CompletableFuture.runAsync(() -> mailService.sendMentorNotificationMail(notification, pId));
                    }
            );
        }

        return SeminarResponse.fromSeminarEntity(newSeminar);
    }

    public CustomPagingResponse<SeminarResponse> searchSeminars(String startTime, String endTime, String searchName, Integer departmentId, List<UUID> mentorUUIDs, String status, Pageable p){
        var seminars = seminarRepository.findAll()
                .stream()
                .filter(s -> {
                    var matchTime = s.getStartTime().toLocalDate().compareTo(DateTimeUtils.parseStringToLocalDate(startTime))>=0 && s.getStartTime().toLocalDate().compareTo(DateTimeUtils.parseStringToLocalDate(endTime))<=0;

                    var matchName = true;
                    if(Strings.isNotBlank(searchName))
                    matchName = StringUtils.containsAnyIgnoreCase(s.getName(),searchName) || s.getMentors().stream().anyMatch(m -> ofNullable(m.getFullName()).map(fName ->StringUtils.containsAnyIgnoreCase(fName, searchName)).orElse(false));

                    var matchDepartment = true;
                    if (departmentId != null)
                       matchDepartment = departmentId.equals(s.getDepartment().getId());

                    var matchMentor = true;
                    if(mentorUUIDs!=null && !mentorUUIDs.isEmpty())
                        matchMentor = s.getMentors().stream().map(UserProfile::getAccountId).toList().containsAll(mentorUUIDs);

                    var matchStatus = true;
                    if("past".equals(status)){
                        matchStatus = s.getStartTime().isBefore(nowInVietnam().toLocalDateTime());
                    }
                    if("future".equals(status)){
                        matchStatus = s.getStartTime().isAfter(nowInVietnam().toLocalDateTime());
                    }

                    return matchDepartment && matchName && matchTime && matchMentor && matchStatus;
                })
                .sorted((o1, o2) -> {
                    var diffO1 = Math.abs(ChronoUnit.SECONDS.between(nowInVietnamLocalDateFormat(), o1.getStartTime()));
                    var diffO2 = Math.abs(ChronoUnit.SECONDS.between(nowInVietnamLocalDateFormat(), o2.getStartTime()));
                    return Long.compare(diffO1,diffO2);
                }).toList();


        var count = seminars.size();
        var offset = Math.min(p.getPageSize()*p.getPageNumber(),count);
        var offsetTo = Math.min(offset+p.getPageSize(),count);
        if(offset<count){
            String nextPageLink = null;
            if (p.getPageSize()*(p.getPageNumber()+1) < count){
                nextPageLink = linkTo(methodOn(SeminarController.class).getAll(
                        mentorUUIDs,startTime,endTime,searchName,departmentId,status,p.getPageNumber()+1,p.getPageSize()
                )).toUri().toString();
            }

            var seminarsToReturn = seminars.subList(offset, offsetTo)
                    .stream().map(SeminarResponse::fromSeminarEntity).toList();

            return CustomPagingResponse.<SeminarResponse>builder()
                    .content(seminarsToReturn)
                    .nextPage(nextPageLink)
                    .build();
        }else {
            //end of list
            return CustomPagingResponse.<SeminarResponse>builder()
                    .content(List.of())
                    .nextPage(null)
                    .build();
        }
    }

    @Recurring(id = "send-invitation-email-job", cron = "0 1 * * *", zoneId = "Asia/Ho_Chi_Minh")
    @Job(name = "Sending invitation email job")
    public void sendingMailJob() throws MailjetException {
        var seminarIds = getTodaySeminarIds();
        var seminarsMap = mailService.accumulateSeminars(seminarIds);
        seminarsMap.forEach(
                (id, seminar) -> {
                    var sendTime = seminar.getEndTime().plusMinutes(appConfig.getSeminarReportEmailDelay());
                    var zonedSendTime = ZonedDateTime.of(sendTime, DateTimeUtils.VIET_NAM_ZONE);
                    BackgroundJob.schedule(zonedSendTime,() -> mailService.sendEmail(id, null));
                }
        );
    }
//    public CustomPagingResponse<SeminarResponse> searchByDateAndName(String startTime, String endTime, String searchName, Integer departmentId, UUID mentorUUID, String status, Pageable p){
//        String mentorId = "";
//        if(mentorUUID!=null)
//            mentorId = mentorUUID.toString();
//        List<Long> idResults;
//
//        if(departmentId!= null)
//            idResults = seminarRepository.byDate(startTime,endTime,searchName, departmentId, mentorId);
//        else
//            idResults = seminarRepository.byDate(startTime,endTime,searchName,mentorId);
//
//        var count = idResults.size();
//        var offset = Math.min(p.getPageSize()*p.getPageNumber(),count);
//        var offsetTo = Math.min(offset+p.getPageSize(),count);
//        List<Long> idToReturn;
//        if(offset<count){
//            idToReturn = idResults.subList(offset, offsetTo);
//        }else {
//            //end of list
//            return CustomPagingResponse.<SeminarResponse>builder()
//                    .content(List.of())
//                    .nextPage(null)
//                    .build();
//        }
//        var seminarResult = seminarRepository.findAllById(idToReturn);
//
//
//        var seminarResultMap = seminarResult.stream().collect(Collectors.toMap(
//                Seminar::getId,
//                seminar -> seminar
//        ));
//
//        ArrayList<Seminar> orderedSeminarResult = new ArrayList<>();
//        for (int i = 0; i < idToReturn.size(); i++) {
//            orderedSeminarResult.add(i, seminarResultMap.get(idToReturn.get(i)));
//        }
//        var seminarResList = orderedSeminarResult.stream()
//                .sorted()
//                .map(seminar -> SeminarResponse.fromSeminarEntity(seminar))
//                .collect(Collectors.toList());
//        String nextPageLink = null;
//        if (p.getPageSize()*(p.getPageNumber()+1) < count){
//            nextPageLink = linkTo(methodOn(SeminarController.class).getAll(
//                    mentorUUID,startTime,endTime,searchName,departmentId,status,p.getPageNumber()+1,p.getPageSize()
//            )).toUri().toString();
//        }
//
//        return CustomPagingResponse.<SeminarResponse>builder()
//                .content(seminarResList)
//                .nextPage(nextPageLink)
//                .build();
//    }

}
