package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Topic;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.mailModel.MentorNotification;
import com.example.mentoringapis.models.upStreamModels.CreateTopicRequest;
import com.example.mentoringapis.models.upStreamModels.TopicDetailResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateTopicRequest;
import com.example.mentoringapis.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;
    private final TopicFieldRepository topicFieldRepository;
    private final AccountsRepository accountsRepository;
    private final TopicCategoryRepository topicCategoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final MailService mailService;

    public TopicDetailResponse createTopic(CreateTopicRequest request, UUID mentorId) throws ResourceNotFoundException {
        var topicField = topicFieldRepository.findById(request.getFieldId());
        var topicCategory = topicCategoryRepository.findById(request.getCategoryId());
        if(topicCategory.isEmpty() || topicField.isEmpty())
            throw new ResourceNotFoundException("Topic field or category not found");

        var owner = accountsRepository.findAccountsByIdAndRole(mentorId, Account.Role.MENTOR.name());

        if(owner.isEmpty())
            throw new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId));

        var newTopic = new Topic();
        newTopic.setCategory(topicCategory.get());
        newTopic.setField(topicField.get());
        newTopic.setMentor(owner.get().getUserProfile());
        newTopic.setName(request.getName());
        newTopic.setDescription(request.getDescription());
        newTopic.setStatus(Topic.Status.WAITING.name());

        topicRepository.save(newTopic);
        return TopicDetailResponse.fromTopicEntity(newTopic);
    }

    public TopicDetailResponse editTopic(UpdateTopicRequest request, UUID mentorId, Long topicId) throws ResourceNotFoundException {
        var topicToUpdate = topicRepository.findById(topicId).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Cannot find topic with id: %s", topicId)));

        if(request.getCategoryId() != null){
            var topicCategory = topicCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() ->  new ResourceNotFoundException("Topic category not found"));
            topicToUpdate.setCategory(topicCategory);
        }

        if(request.getFieldId() != null){
            var topicField = topicFieldRepository.findById(request.getFieldId())
                    .orElseThrow(() ->  new ResourceNotFoundException("Topic category not found"));
            topicToUpdate.setField(topicField);
        }

        var owner = accountsRepository.findAccountsByIdAndRole(mentorId, Account.Role.MENTOR.name());

        if(owner.isEmpty() || !mentorId.equals(topicToUpdate.getMentor().getAccountId()))
            throw new ResourceNotFoundException("Request mentor doesnt match with ownerId");

        Optional.ofNullable(request.getName()).ifPresent(topicToUpdate::setName);
        Optional.ofNullable(request.getDescription()).ifPresent(topicToUpdate::setDescription);
        topicToUpdate.setStatus(Topic.Status.WAITING.name());

        topicRepository.save(topicToUpdate);
        return TopicDetailResponse.fromTopicEntity(topicToUpdate);
    }

    public List<TopicDetailResponse> getAll(){
        return topicRepository.findAll().stream()
                .filter(t -> !Topic.Status.DELETED.name().equals(t.getStatus()))
                .map(TopicDetailResponse::fromTopicEntity)
                .collect(Collectors.toList());
    }

    public List<TopicDetailResponse> getByMentorId(UUID mentorId){
        return topicRepository.findALlByMentorId(mentorId)
                .stream()
                .filter(topic -> topic.getStatus().equals(Topic.Status.ACCEPTED.name()))
                .map(TopicDetailResponse::fromTopicEntityNoMentor)
                .collect(Collectors.toList());
    }

    public List<TopicDetailResponse> changeStatus(List<Long> ids, String status) throws ClientBadRequestError {
        var topicsToActivate = topicRepository.findAllByIdIn(ids);

        if(Topic.Status.DELETED.name().equals(status)){
            if(topicsToActivate.stream().anyMatch(t -> !Topic.Status.WAITING.name().equals(t.getStatus())))
                throw new ClientBadRequestError("DELETE TOPIC CAN ONLY EFFECT ON WAITING TOPIC");
        }

        topicsToActivate.forEach(topic -> topic.setStatus(Topic.Status.valueOf(status).name()));
        topicRepository.saveAll(topicsToActivate);

        //update search vector
        topicsToActivate.forEach(t -> CompletableFuture.runAsync(() -> userProfileRepository.updateTsvSearch(t.getMentor().getAccountId().toString())));

        if(status.equals(Topic.Status.ACCEPTED.name()) && !topicsToActivate.isEmpty()){
            topicsToActivate.forEach( t ->
            {
                var notification = new MentorNotification(t, t.getMentor());
                var mentorId = t.getMentor().getAccountId();
                //send email if topic is accepted
                CompletableFuture.runAsync(() -> mailService.sendMentorNotificationMail(notification, mentorId));
            });
        }
        return topicsToActivate.stream().map(TopicDetailResponse::fromTopicEntity).collect(Collectors.toList());
    }

}
