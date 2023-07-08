package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.UserProfile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends PagingAndSortingRepository<UserProfile, UUID> {
    UserProfile save(UserProfile entity);
    Optional<UserProfile> findUserProfileByAccount_Id(UUID accountId);
    @Query("select up from UserProfile up " +
            "left join fetch up.availableTimes aTime " +
            "left join fetch aTime.availableTimeExceptionSet " +
            "where up.accountId = ?1")
    Optional<UserProfile> findUserProfileByAccount_IdFetchSchedule(UUID accountId);
}