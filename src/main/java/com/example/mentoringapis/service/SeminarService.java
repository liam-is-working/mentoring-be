package com.example.mentoringapis.service;

import com.example.mentoringapis.controllers.SeminarController;
import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Seminar;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.paging.CustomPagingResponse;
import com.example.mentoringapis.models.upStreamModels.CreateSeminarRequest;
import com.example.mentoringapis.models.upStreamModels.SeminarResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateSeminarRequest;
import com.example.mentoringapis.repositories.AccountsRepository;
import com.example.mentoringapis.repositories.DepartmentRepository;
import com.example.mentoringapis.repositories.SeminarRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.google.cloud.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.mentoringapis.utilities.DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
@RequiredArgsConstructor
public class SeminarService {
    private final SeminarRepository seminarRepository;
    private final AccountsRepository accountsRepository;
    private final FireStoreService fireStoreService;
    private final DepartmentRepository departmentRepository;
    private final StaticResourceService staticResourceService;
    private final FeedbackService feedbackService;

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
                .map(seminar -> SeminarResponse.fromSeminarEntity(seminar, staticResourceService))
                .collect(Collectors.toList());
    }

    public SeminarResponse getById(Long id) throws ResourceNotFoundException {
        return seminarRepository.findById(id)
                .map(seminar -> SeminarResponse.fromSeminarEntity(seminar, staticResourceService))
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find Seminar with id: %s", id)));
    }

    public Long deleteSeminar(Long seminarId) throws ResourceNotFoundException {
        var seminarOptional = seminarRepository.findById(seminarId);
        var seminar = seminarOptional.orElseThrow(() ->  new ResourceNotFoundException(String.format("Cannot find seminar with id: %s", seminarId)));
        seminarRepository.delete(seminar);
        return seminarId;

    }

    public SeminarResponse update(UpdateSeminarRequest request, long seminarId) throws ClientBadRequestError, ResourceNotFoundException {
        var currentSeminar = seminarRepository.findById(seminarId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find Seminar with id: %s", seminarId)));


        Set<UserProfile> mentorProfiles = getMentorProfiles(request.getMentorIds());
        Optional.ofNullable(mentorProfiles).ifPresent(currentSeminar::setMentors);
        Optional.ofNullable(request.getDescription()).ifPresent(currentSeminar::setDescription);
        Optional.ofNullable(request.getLocation()).ifPresent(currentSeminar::setLocation);
        Optional.ofNullable(request.getImageUrl()).ifPresent(currentSeminar::setImageUrl);
        Optional.ofNullable(request.getStartTime()).map(DateTimeUtils::parseDate).ifPresent(currentSeminar::setStartTime);
        Optional.ofNullable(request.getName()).ifPresent(currentSeminar::setName);
        Optional.ofNullable(request.getAttachmentUrls()).ifPresent(urlSet -> currentSeminar.setAttachmentUrl(String.join(";", urlSet)));
        seminarRepository.save(currentSeminar);
        return SeminarResponse.fromSeminarEntity(currentSeminar, staticResourceService);
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
                throw ClientBadRequestError.builder()
                        .errorMessages(String.format("MentorIds not found: %s", notFoundIds.toArray()))
                        .build();
            }
            mentorProfiles = mentorsList.stream().map(Tuple::y)
                    .map(Optional::get)
                    .map(Account::getUserProfile)
                    .collect(Collectors.toSet());
        }
        return mentorProfiles;
    }

    public SeminarResponse create(CreateSeminarRequest request, Integer departmentId) throws ClientBadRequestError, IOException {
        Set<UserProfile> mentorProfiles = getMentorProfiles(request.getMentorIds());
        var newSeminar = new Seminar();
        Optional.ofNullable(mentorProfiles).ifPresent(newSeminar::setMentors);
        newSeminar.setDescription(request.getDescription());
        newSeminar.setImageUrl(request.getImageUrl());
        newSeminar.setLocation(request.getLocation());
        newSeminar.setStartTime(DateTimeUtils.parseDate(request.getStartTime()));
        newSeminar.setName(request.getName());
        Optional.ofNullable(request.getAttachmentUrls()).ifPresent(urlSet -> newSeminar.setAttachmentUrl(String.join(";", urlSet)));
        if (departmentId != null) {
            var department = departmentRepository.findById(departmentId);
            newSeminar.setDepartment(department.orElse(null));
        }
        seminarRepository.save(newSeminar);
        fireStoreService.createDiscussionRoom(newSeminar.getId());
        feedbackService.initiateFeedback(newSeminar);
        return SeminarResponse.fromSeminarEntity(newSeminar, staticResourceService);
    }

    public CustomPagingResponse<SeminarResponse> searchByDateAndName(String startTime, String endTime, String searchName, Integer departmentId, String status, Pageable p){
        var idResults = Optional.ofNullable(departmentId)
                .map(id -> seminarRepository.byDate(startTime,endTime,searchName, id))
                .orElse(seminarRepository.byDate(startTime,endTime,searchName));
        var count = idResults.size();
        var offset = Math.min(p.getPageSize()*p.getPageNumber(),count);
        var offsetTo = Math.min(offset+p.getPageSize(),count);
        List<Long> idToReturn;
        if(offset<count){
            idToReturn = idResults.subList(offset, offsetTo);
        }else {
            //end of list
            return CustomPagingResponse.<SeminarResponse>builder()
                    .content(List.of())
                    .nextPage(null)
                    .build();
        }
        var seminarResult = seminarRepository.findAllById(idToReturn);


        var seminarResultMap = seminarResult.stream().collect(Collectors.toMap(
                Seminar::getId,
                seminar -> seminar
        ));

        ArrayList<Seminar> orderedSeminarResult = new ArrayList<>();
        for (int i = 0; i < idToReturn.size(); i++) {
            orderedSeminarResult.add(i, seminarResultMap.get(idToReturn.get(i)));
        }
        var seminarResList = orderedSeminarResult.stream()
                .sorted()
                .map(seminar -> SeminarResponse.fromSeminarEntity(seminar, staticResourceService))
                .collect(Collectors.toList());
        String nextPageLink = null;
        if (p.getPageSize()*(p.getPageNumber()+1) < count){
            nextPageLink = linkTo(methodOn(SeminarController.class).getAll(
                    startTime,endTime,searchName,departmentId,status,p.getPageNumber()+1,p.getPageSize()
            )).toUri().toString();
        }

        return CustomPagingResponse.<SeminarResponse>builder()
                .content(seminarResList)
                .nextPage(nextPageLink)
                .build();
    }

}
