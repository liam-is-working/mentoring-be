package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.UserProfile;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    @Query("select up from UserProfile up " +
            "left join fetch up.account " +
            "where up.accountId = ?1")
    Optional<UserProfile> findUserProfileByAccount_Id(UUID accountId);

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
            "left join fetch up.availableTimes aT " +
            "left join fetch aT.availableTimeExceptionSet exc " +
            "left join fetch up.topics topics " +
            "left join fetch topics.category cat " +
            "left join fetch topics.field field " +
            "left join fetch up.followers " +
            "left join fetch up.feedbacks " +
            "where acc.role = 'MENTOR' and acc.status = 'ACTIVATED' ")
    List<UserProfile> getAllActivatedMentors();

    @Query("select up from UserProfile  up " +
            "left join fetch up.account acc " +
            "where acc.role = 'MENTEE' ")
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
}