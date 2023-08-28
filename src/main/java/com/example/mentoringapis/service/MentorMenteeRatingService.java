package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.MeetingFeedback;
import com.example.mentoringapis.entities.MenteeMentorId;
import com.example.mentoringapis.entities.MentorMenteeRating;
import com.example.mentoringapis.repositories.MeetingFeedbackRepository;
import com.example.mentoringapis.repositories.MentorMenteeRatingRepository;
import com.example.mentoringapis.repositories.MentorMenteeRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.spring.annotations.Recurring;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorMenteeRatingService {
    private final MeetingFeedbackRepository meetingFeedbackRepository;
    private final MentorMenteeRepository mentorMenteeRepository;
    private final UserProfileRepository userProfileRepository;
    private final MentorMenteeRatingRepository mentorMenteeRatingRepository;

    public Set<UserProfileRepository.CrossId> getAllCombination(){
        var cross = userProfileRepository.getMentorMenteeCross();
        cross.forEach(x -> System.out.println(String.format("%s-%s", x.getMenteeid(),x.getMentorid())));
        return cross;
    };


    @Recurring(id = "accumulate-rating", cron = "0 0 * * *", zoneId = "Asia/Ho_Chi_Minh")
    @Job(name = "Accumulate data for recommendation system job")
    public void accumulateRating(){
        Set<MentorMenteeRating> result = new HashSet<>();
        var crossSet = getAllCombination();
        var avgRating = meetingFeedbackRepository.getAvgRating()
                .stream().collect(Collectors.toMap(x -> new MenteeMentorId(x.getMentorid(), x.getMenteeid()), MeetingFeedbackRepository.AverageRating::getAvg));
        var followings = mentorMenteeRepository.findAll();
        crossSet.forEach(cross -> {
            var id = new MenteeMentorId(cross.getMentorid(), cross.getMenteeid());
            var rating  = new MentorMenteeRating();

            rating.setMenteeId(id.getMenteeId());
            rating.setMentorId(id.getMentorId());

            var avg = avgRating.get(id);
            if(followings.removeIf(x -> x.getMenteeId().equals(id.getMenteeId()) && x.getMentorId().equals(id.getMentorId())))
                avg = 6f;
            rating.setRating(avg);

            if(avg != null)
                result.add(rating);
        });

        mentorMenteeRatingRepository.deleteAllInBatch();
        mentorMenteeRatingRepository.flush();
        mentorMenteeRatingRepository.saveAll(result);
    }
}
