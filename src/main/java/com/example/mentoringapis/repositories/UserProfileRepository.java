package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.MenteeMentorId;
import com.example.mentoringapis.entities.UserProfile;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    @Query("select up from UserProfile up " +
            "left join fetch up.account " +
            "where up.accountId = ?1")
    Optional<UserProfile> findUserProfileByAccount_Id(UUID accountId);

    @Query(value = "select update_profile_tsvsearch(?1)", nativeQuery = true)
    void updateTsvSearch(String accountId);

    @Query("select up from UserProfile up " +
            "left join fetch up.account " +
            "left join fetch up.seminars seminars " +
            "left join fetch seminars.mentors mList " +
            "left join fetch mList.account " +
            "left join fetch seminars.department " +
            "where up.accountId = ?1")
    Optional<UserProfile> findMentorWithSeminars(UUID accountId);

    @Query("select up from UserProfile up " +
            "left join fetch up.followers followers " +
            "left join fetch followers.account fa " +
            "where up.accountId = ?1")
    Optional<UserProfile> findUserProfileByAccount_IdWithFollowers(UUID accountId);
    @Query(value = "select mentor_id from mentor_mentee left join accounts acc on mentor_id = acc.id\n" +
            "where acc.status = 'ACTIVATED' and mentee_id = ?1", nativeQuery = true)
    List<UUID> fetchFollowingIds(UUID menteeId);
    @Query("select up from UserProfile up " +
            "left join fetch up.followings following " +
            "left join fetch following.account fa " +
            "where up.accountId = ?1")
    Optional<UserProfile> findUserProfileByAccount_IdWithFollowings(UUID accountId);
    @Query("select up from UserProfile up " +
            "left join fetch up.availableTimes aTime " +
            "left join fetch up.account acc " +
            "left join fetch acc.department " +
            "left join fetch up.followers " +
            "left join fetch up.feedbacks " +
            "left join fetch aTime.availableTimeExceptionSet exS " +
            "where up.accountId = ?1")
    Optional<UserProfile> findUserProfileByAccount_IdFetchSchedule(UUID accountId);
    @Query("select up from UserProfile  up " +
            "left join fetch up.account acc " +
            "left join fetch acc.department d " +
            "left join fetch up.topics topics " +
            "left join fetch topics.bookings " +
            "left join fetch topics.category cat " +
            "left join fetch topics.field field " +
            "left join fetch up.followers " +
            "left join fetch up.feedbacks " +
            "where acc.role = 'MENTOR' and acc.status = 'ACTIVATED' ")
    List<UserProfile> getAllActivatedMentors();

    @Query("select up from UserProfile  up " +
            "left join fetch up.account acc " +
            "left join fetch acc.department d " +
            "left join fetch up.topics topics " +
            "left join fetch topics.bookings " +
            "left join fetch topics.category cat " +
            "left join fetch topics.field field " +
            "left join fetch up.followers " +
            "left join fetch up.feedbacks " +
            "where acc.role = 'MENTOR' and acc.status = 'ACTIVATED' and up.accountId in ?1")
    List<UserProfile> getAllActivatedMentors(Iterable<UUID> ids);

    @Query("select up from UserProfile  up " +
            "left join fetch up.account acc " +
            "left join fetch acc.department d " +
            "left join fetch up.topics topics " +
            "left join fetch topics.category cat " +
            "left join fetch topics.field field " +
            "left join fetch up.followers " +
            "left join fetch up.feedbacks " +
            "where acc.role = 'MENTOR' and acc.status = 'ACTIVATED' and up.accountId in ?1")
    List<UserProfile> getAllTopActivatedMentors(Iterable<UUID> ids);

    @Query(value = "select COALESCE(rating.receiver_id, followCount.mentor_id) as id from\n" +
            "((select receiver_id, avg(rating) as avgRate from meeting_feedbacks group by receiver_id) as rating\n" +
            "full outer join\n" +
            "(select mentor_id, count(mentor_id) as countFollow from mentor_mentee group by mentor_id) as followCount\n" +
            "on rating.receiver_id = followCount.mentor_id)\n" +
            "where (avgRate >= 4 or avgRate isnull) and COALESCE(rating.receiver_id, followCount.mentor_id) not in (select mentor_id from mentor_mentee where mentee_id = ?1)\n" +
            "order by countFollow DESC NULLS LAST, avgRate DESC NULLS LAST", nativeQuery = true)
    List<UUID> getTopMentors(UUID mentee_id);

    @Query(value = "select receiver_id\n" +
            "from meeting_feedbacks\n" +
            "where rating < 4 and giver_id = ?1", nativeQuery = true)
    List<UUID> getBadReview(UUID mentee_id);


    @Query(value = "select user_profiles.account_id as accountId, ts_rank_cd(tsvector_search, query) as rank " +
            "from user_profiles left join accounts a on user_profiles.account_id = a.id , to_tsquery('simple', ?1) query " +
            "where (query @@ user_profiles.tsvector_search) and a.status = 'ACTIVATED' ", nativeQuery = true)
    List<SearchMentorResult> searchAllActivatedMentors(String searchString);

    @Query(value = "select user_profiles.account_id as accountId, ts_rank_cd(tsvector_search, query) as rank " +
            "from user_profiles left join accounts a on user_profiles.account_id = a.id , to_tsquery('simple', ?1) query " +
            "where a.id IN ?2 and a.status = 'ACTIVATED' and (query @@ user_profiles.tsvector_search)   ", nativeQuery = true)
    List<SearchMentorResult> searchAllActivatedMentors(String searchString, Set<UUID> ids);

    public interface SearchMentorResult{
        UUID getAccountId();
        float getRank();
    }

    @Query("select up from UserProfile  up " +
            "left join fetch up.account acc " +
            "where acc.role = 'STUDENT' ")
    List<UserProfile> getAllMentees();

    @Query("select up from UserProfile  up " +
            "left join fetch up.account acc " +
            "where acc.email LIKE %?1% and acc.role = 'STUDENT' ")
    List<UserProfile> searchMenteeByEmail(String email);

    @NotNull
    @Override
    @Query("select up from UserProfile up " +
            "left join fetch up.account acc " +
            "left join fetch acc.department " +
            "left join fetch up.followers " +
            "left join fetch up.feedbacks " +
            "where up.accountId in ?1")
    List<UserProfile> findAllById(@NotNull Iterable<UUID> uuids);

    @NotNull
    @Query("select up from UserProfile up " +
            "where up.accountId in ?1")
    List<UserProfile> findAllByIdSimplified(@NotNull Iterable<UUID> uuids);

    @Query(value = "select * from mentor_mentee_cross() ",
    nativeQuery = true)
    Set<CrossId> getMentorMenteeCross();

    interface CrossId{
        UUID getMentorid();
        UUID getMenteeid();
    }
}