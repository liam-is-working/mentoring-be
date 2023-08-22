package com.example.mentoringapis.service;

import com.example.mentoringapis.repositories.MentorMenteeRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BigQueryService {
    private final UserProfileRepository userProfileRepository;
    private final MentorMenteeRepository mentorMenteeRepository;
    private final UserProfileService userProfileService;


}
