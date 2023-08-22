package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.*;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.MentorListResponse;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.repositories.DepartmentRepository;
import com.example.mentoringapis.repositories.MentorMenteeRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.*;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final FireStoreService fireStoreService;
    private final MentorMenteeRepository mentorMenteeRepository;
    private final DepartmentRepository departmentRepository;
    private final ObjectMapper objectMapper;
    private final BigQuery bigQuery;
    private final AppConfig appConfig;


    public MentorAccountResponse updateMentor(UpdateMentorProfileRequest request, UUID mentorId) throws ResourceNotFoundException {
        return MentorAccountResponse.fromAccountEntity(updateAccount(request, mentorId));
    }

    public StaffAccountResponse updateStaff(UpdateMentorProfileRequest request, UUID mentorId) throws ResourceNotFoundException {
        return StaffAccountResponse.fromAccountEntity(updateAccount(request, mentorId));
    }

    private Account updateAccount(UpdateMentorProfileRequest request, UUID mentorId) throws ResourceNotFoundException {
        var profileToUpdate = userProfileRepository.findUserProfileByAccount_Id(mentorId);
        Department department = null;
        if(request.getDepartmentId() != null){
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find department with id %s", request.getDepartmentId())));
        }
        return profileToUpdate.map(
                profile -> {
                    var account = profile.getAccount();
                    ofNullable(request.getPhoneNum()).ifPresent(profile::setPhoneNum);
                    ofNullable(request.getFullName()).ifPresent(profile::setFullName);
                    ofNullable(request.getStatus()).ifPresent(account::setStatus);
                    if(request.getDepartmentId() != null){
                        var departmentChange = departmentRepository.findById(request.getDepartmentId()).orElse(account.getDepartment());
                        account.setDepartment(departmentChange);
                    }
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
                    ofNullable(request.getFullName()).ifPresent(prof::setFullName);
                    ofNullable(request.getGender()).ifPresent(prof::setGender);
                    ofNullable(request.getAvatarUrl()).ifPresent(prof::setAvatarUrl);
                    ofNullable(request.getCoverUrl()).ifPresent(prof::setCoverUrl);
                    ofNullable(request.getPhoneNumber()).ifPresent(prof::setPhoneNum);
                    ofNullable(request.getDescription()).ifPresent(prof::setDescription);
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
                }).map(UserProfileResponse::fromUserProfileMinimal).orElse(null);
    }

    public UserProfileResponse findByUUID(UUID id){
        return userProfileRepository
                .findUserProfileByAccount_Id(id)
                .map(UserProfileResponse::fromUserProfile)
                .orElse(null);
    }

    public List<UserProfileResponse> getAllMenteeByEmail(String email){
        var menteeProfs = userProfileRepository.searchMenteeByEmail(email);
        return menteeProfs.stream().map(UserProfileResponse::fromUserProfileMinimal)
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

    public MentorListResponse.MentorCard mentorCardFromEntity(UserProfile mentor){
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
                        .sorted((o1, o2) -> o2.getBookings().size() - o1.getBookings().size())
                        .map(TopicDetailResponse::fromTopicEntityNoMentor)
                        .toList())
                .build();
    }

    public MentorListResponse getRecommendation(UUID menteeId, String[] searchString) throws InterruptedException {
        if(searchString == null)
            searchString = new String[]{};
        var sanitizedSearchStrings = Arrays.stream(searchString).filter(Objects::nonNull)
                .map(String::toUpperCase)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        var recommendedList = new ArrayList<UUID>();

        var queryString = String.format("SELECT f0_ FROM " +
                "`growthme-392303.bigquerypublic.recommendation_result` " +
                "where mentee_id = '%s'", menteeId);
        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(queryString)
                        .setUseLegacySql(false)
                        .build();

        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());


        var following = mentorMenteeRepository.findAllByMenteeId(menteeId).stream().map(MentorMentee::getMentorId).collect(Collectors.toList());
        var topMentors = userProfileRepository.getTopMentors().stream().filter(id -> !following.contains(id)).collect(Collectors.toList());

        queryJob = queryJob.waitFor();

        // Get the results.
        try{
            TableResult result = queryJob.getQueryResults();
            for (var row : result.iterateAll().iterator().next().get(0).getRepeatedValue()){
                recommendedList.add(UUID.fromString(row.getRecordValue().get(0).getStringValue()));
            }
        }catch (Exception ignored){}

        topMentors.removeIf(recommendedList::contains);
        if(recommendedList.size()<appConfig.getMaxMentorRecommendation()){
            var toAdd = appConfig.getMaxMentorRecommendation()-recommendedList.size();
            recommendedList.addAll(topMentors.subList(0, Math.min(toAdd, topMentors.size())));
        }

        var mentors = userProfileRepository.getAllActivatedMentors(recommendedList)
                .stream().collect(Collectors.toMap(UserProfile::getAccountId, m-> m));

        var returnVal = new MentorListResponse();
        returnVal.setMentorCards(recommendedList.stream()
                .map(mentors::get)
                .filter(Objects::nonNull)
                .map(this::mentorCardFromEntity)
                .filter(card ->
                {
                    if (sanitizedSearchStrings.isEmpty())
                        return true;
                    return card.getSearchString()
                            .stream()
                            .anyMatch(s -> sanitizedSearchStrings.stream().anyMatch(s::contains));

                })
                .toList());
        return returnVal;

        // Print all pages of the results.
//        for (FieldValueList row : result.iterateAll()) {
//            // String type
//            String url = row.get("url").getStringValue();
//            String viewCount = row.get("view_count").getStringValue();
//            System.out.printf("%s : %s views\n", url, viewCount);
//        }
    }

    public MentorListResponse getMentorCards(){
        var mentors = userProfileRepository.getAllActivatedMentors();
        var mentorCards = mentors.parallelStream()
                .map(this::mentorCardFromEntity)
                .sorted((m1,m2) -> Double.compare(m2.getRatingOptional().orElse(0),m1.getRatingOptional().orElse(0)))
                .sorted((m1,m2) -> m2.getFollowers()-m1.getFollowers())
                .toList();

        var response = new MentorListResponse();
        response.setMentorCards(mentorCards);
        return response;
    }

    public MentorListResponse getMentorCards(String[] searchString){
        if(searchString == null)
            searchString = new String[]{};
        var sanitizedSearchStrings = Arrays.stream(searchString).filter(Objects::nonNull)
                .map(String::toUpperCase)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if(sanitizedSearchStrings.isEmpty()|| Strings.isBlank(String.join(" ", sanitizedSearchStrings)))
            return getMentorCards();

        var searchResult = userProfileRepository.searchAllActivatedMentors(String.join(" ", sanitizedSearchStrings).trim());
        searchResult.sort((r1,r2) -> Float.compare(r2.getRank(),r1.getRank()));


        var mentorsList = userProfileRepository.getAllActivatedMentors(searchResult.stream().map(UserProfileRepository.SearchMentorResult::getAccountId).toList());


        var mentorsMap = mentorsList.stream().collect(Collectors.toMap(UserProfile::getAccountId, up -> up));

        var response  = new MentorListResponse();
        var mentorCards = searchResult.stream()
                .map(sR  -> mentorsMap.get(sR.getAccountId()))
                .filter(Objects::nonNull)
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
