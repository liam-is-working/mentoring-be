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

    @Query(value = "select ranking.mentor_id from\n" +
            "(select mm.mentor_id, avg(rating) as avg_rating, sum(follow_count.count) as sumFollow\n" +
            "     from mentor_mentee_rating as mm left join\n" +
            "         (select mentor_id, count(mentor_id) as count\n" +
            "         from mentor_mentee group by mentor_id)\n" +
            "             as follow_count\n" +
            "        on follow_count.mentor_id = mm.mentor_id\n" +
            "     GROUP BY mm.mentor_id\n" +
            "     ORDER BY sumFollow DESC, avg_rating DESC ) as ranking\n" +
            "WHERE ranking.avg_rating >= 3", nativeQuery = true)
    List<UUID> getTopMentors();


    @Query(value = "select user_profiles.account_id as accountId, ts_rank_cd(tsvector_search, query) as rank " +
            "from user_profiles left join accounts a on user_profiles.account_id = a.id , plainto_tsquery('simple', ?1) query " +
            "where (query @@ user_profiles.tsvector_search or UPPER(user_profiles.tsvector_search\\:\\:text) LIKE %?1% ) and a.status = 'ACTIVATED' ", nativeQuery = true)
    List<SearchMentorResult> searchAllActivatedMentors(String searchString);

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