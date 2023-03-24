package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.UserProfile;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends PagingAndSortingRepository<UserProfile, UUID> {
    UserProfile save(UserProfile entity);
    Optional<UserProfile> findUserProfileByAccount_Id(UUID accountId);
}