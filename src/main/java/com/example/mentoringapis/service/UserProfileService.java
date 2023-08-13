package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.*;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.MentorListResponse;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.repositories.MentorMenteeRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final FireStoreService fireStoreService;
    private final MentorMenteeRepository mentorMenteeRepository;
    private final ObjectMapper objectMapper;

    public MentorAccountResponse updateMentor(UpdateMentorProfileRequest request, UUID mentorId) throws ResourceNotFoundException {
        return MentorAccountResponse.fromAccountEntity(updateAccount(request, mentorId));
    }

    public StaffAccountResponse updateStaff(UpdateMentorProfileRequest request, UUID mentorId) throws ResourceNotFoundException {
        return StaffAccountResponse.fromAccountEntity(updateAccount(request, mentorId));
    }

    private Account updateAccount(UpdateMentorProfileRequest request, UUID mentorId) throws ResourceNotFoundException {
        var profileToUpdate = userProfileRepository.findUserProfileByAccount_Id(mentorId);
        return profileToUpdate.map(
                profile -> {
                    var account = profile.getAccount();
                    Optional.ofNullable(request.getPhoneNum()).ifPresent(profile::setPhoneNum);
                    Optional.ofNullable(request.getFullName()).ifPresent(profile::setFullName);
                    Optional.ofNullable(request.getStatus()).ifPresent(account::setStatus);
                    userProfileRepository.save(profile);

                    fireStoreService.updateUserProfile(profile.getFullName(), null, account.getRole(), account.getEmail(),mentorId);
                    if(request.getFullName()!=null)
                        //update search vector
                        CompletableFuture.runAsync(() -> userProfileRepository.updateTsvSearch(mentorId.toString()));

                    return account;
                }
        ).orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find profile with id: %s", mentorId)));
    }


    public UserProfileResponse update(UserProfileUpdateRequest request, UUID profileId){
        return userProfileRepository.findUserProfileByAccount_Id(profileId)
                .map(prof -> {
                    var isNameTheSame = Objects.equals(request.getFullName(),prof.getFullName());
                    prof.setFullName(request.getFullName());
                    prof.setGender(request.getGender());
                    prof.setAvatarUrl(request.getAvatarUrl());
                    prof.setCoverUrl(request.getCoverUrl());
                    prof.setDescription(request.getDescription());
                    if(request.getActivateAccount())
                        prof.getAccount().setStatus(Account.Status.ACTIVATED.name());
                    if(request.getDob() != null){
                        prof.setDob(Date.valueOf(request.getDob()));
                    }

                    //update search vector
                    if(!isNameTheSame)
                        CompletableFuture.runAsync(() -> userProfileRepository.updateTsvSearch(profileId.toString()));
                    fireStoreService.updateUserProfile(request.getFullName(), request.getAvatarUrl(),
                            prof.getAccount().getRole(), prof.getAccount().getEmail(), profileId);
                    return userProfileRepository.save(prof);
                }).map(UserProfileResponse::fromUserProfile).orElse(null);
    }

    public UserProfileResponse findByUUID(UUID id){
        return userProfileRepository
                .findUserProfileByAccount_Id(id)
                .map(UserProfileResponse::fromUserProfile)
                .orElse(null);
    }

    public List<UserProfileResponse> getAllMenteeByEmail(String email){
        var menteeProfs = userProfileRepository.searchMenteeByEmail(email);
        return menteeProfs.stream().map(UserProfileResponse::fromUserProfile)
                .toList();
    }

    public List<UserProfileResponse> getFollowers(UUID id) throws ResourceNotFoundException {
        var prof = userProfileRepository.findUserProfileByAccount_IdWithFollowers(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find profile with id: %s", id)));
        return prof.getFollowers().stream().map(UserProfileResponse::fromUserProfileMinimal)
                .toList();
    }

    public List<UserProfileResponse> getFollowing(UUID id) throws ResourceNotFoundException {
        var prof = userProfileRepository.findUserProfileByAccount_IdWithFollowings(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find profile with id: %s", id)));
        return prof.getFollowings().stream().map(UserProfileResponse::fromUserProfileMinimal)
                .toList();
    }

    public String findCvByUUID(UUID id){
        return userProfileRepository
                .findUserProfileByAccount_Id(id)
                .map(UserProfile::getCv).orElse(null);
    }

    public CvInformationResponse getCvResponseByUUID(UUID uuid){
        var cvString = findCvByUUID(uuid);
        try {
            var cvResponse = objectMapper.readValue(cvString, CvInformationResponse.class);
            return cvResponse;
        } catch (Exception e) {
            //TODO log
            var cvResponse = new CvInformationResponse();
            cvResponse.setUserProfileId(uuid);
            return cvResponse;
        }
    }

    private List<String> extractSkills(String cvString){
        try{
           return JsonPath.read(cvString, "$.skills[*].name");
        }catch (IllegalArgumentException | NullPointerException | PathNotFoundException exception){
            return List.of();
        }
    }

    private String extractOccupation(String cvString){
        try {
            List<CvInformationResponse.WorkingExp> workingExps = objectMapper.convertValue(JsonPath.read(cvString, "$.workingExps[*]"), new TypeReference<List<CvInformationResponse.WorkingExp>>() {});
            workingExps.sort(Comparator.comparing(wExp -> DateTimeUtils.parseStringToLocalDate(wExp.getStartDate())));
            Collections.reverse(workingExps);
            if(workingExps.isEmpty())
                return null;
            var mostRecentJob = workingExps.stream()
                    .filter(CvInformationResponse.WorkingExp::isWorkingHere)
                    .findFirst()
                    .orElse(workingExps.get(0));

            return String.format("%s tại %s", mostRecentJob.getPosition(), mostRecentJob.getCompany());
        }catch (IllegalArgumentException | NullPointerException | PathNotFoundException exception) {
            return null;
        }
    }

    public CvInformationResponse updateCv(CvInformationUpdateRequest cv, UUID profileId){
        return userProfileRepository.findUserProfileByAccount_Id(profileId)
                .map(profileToUpdate -> {
                    try {
                        profileToUpdate.setCv(objectMapper.writeValueAsString(cv));
                        profileToUpdate.setCvSearchable(cv.toSearchable());
                        return userProfileRepository.save(profileToUpdate);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .map(UserProfile::getCv)
                .map(cvString -> {
                    try {
                        var cvRes = objectMapper.readValue(cvString, CvInformationResponse.class);
                        cvRes.setUserProfileId(profileId);

                        //update search vector
                        CompletableFuture.runAsync(() -> userProfileRepository.updateTsvSearch(profileId.toString()));
                        return cvRes;
                    } catch (JsonProcessingException e) {
                        //TODO log
                        return null;
                    }
                })
                .orElse(null);
    }

    private MentorListResponse.MentorCard mentorCardFromEntity(UserProfile mentor){
        return MentorListResponse.MentorCard
                .builder()
                .avatarUrl(mentor.getAvatarUrl())
                .followers(mentor.getFollowers().size())
                .ratingOptional(mentor.getFeedbacks().stream().mapToInt(MeetingFeedback::getRating).average())
                .mentorId(mentor.getAccountId().toString())
                .description(mentor.getDescription())
                .fullName(mentor.getFullName())
                .occupation(extractOccupation(mentor.getCv()))
                .skills(extractSkills(mentor.getCv()))
                .topics(mentor.getTopics()
                        .stream()
                        .filter(topic -> topic.getStatus().equals(Topic.Status.ACCEPTED.name()))
                        .map(TopicDetailResponse::fromTopicEntityNoMentor)
                        .toList())
                .build();
    }

    public MentorListResponse getMentorCards(){
        var mentors = userProfileRepository.getAllActivatedMentors();
        var mentorCards = mentors.stream()
                .map(this::mentorCardFromEntity).toList();

        var response = new MentorListResponse();
        response.setMentorCards(mentorCards);
        return response;
    }

    public MentorListResponse getMentorCards(String[] searchString){
        if(searchString==null || searchString.length==0)
            return getMentorCards();

        var searchResult = userProfileRepository.searchAllActivatedMentors(String.join(" ", searchString));
        searchResult.sort((r1,r2) -> Float.compare(r2.getRank(),r1.getRank()));


        var mentorsList = userProfileRepository.getAllActivatedMentors(searchResult.stream().map(UserProfileRepository.SearchMentorResult::getAccountId).toList());


        var mentorsMap = mentorsList.stream().collect(Collectors.toMap(UserProfile::getAccountId, up -> up));

        var response  = new MentorListResponse();
        var mentorCards = searchResult.stream()
                .map(sR  -> mentorsMap.get(sR.getAccountId()))
                .map(this::mentorCardFromEntity).toList();
        response.setMentorCards(mentorCards);
        return response;
    }

    public void follow(UUID mentorId, UUID menteeId) {
        var mentor = userProfileRepository.findUserProfileByAccount_Id(mentorId)
                .orElse(null);
        var mentee = userProfileRepository.findUserProfileByAccount_Id(menteeId)
                .orElse(null);
        if (mentee == null || mentor == null
                || !mentee.getAccount().getRole().equals(Account.Role.STUDENT.name())
                || !mentor.getAccount().getRole().equals(Account.Role.MENTOR.name()))
            return;

        var newMenteeMentor = new MentorMentee();
        newMenteeMentor.setMenteeId(menteeId);
        newMenteeMentor.setMentorId(mentorId);
        try {
            mentorMenteeRepository.save(newMenteeMentor);
        } catch (Exception ignored) {
        }
    }

    public void unfollow(UUID mentorId, UUID menteeId) {
        var id = new MenteeMentorId(mentorId,menteeId);
        if(mentorMenteeRepository.existsById(id)){
            try {
                mentorMenteeRepository.deleteById(id);
            } catch (Exception ignored) {
            }
        }

    }
}
