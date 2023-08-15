package com.example.mentoringapis.service;

import com.example.mentoringapis.controllers.AdminController;
import com.example.mentoringapis.entities.TopicCategory;
import com.example.mentoringapis.entities.TopicField;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.repositories.TopicCategoryRepository;
import com.example.mentoringapis.repositories.TopicFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class TopicFieldCategoryService {
    private final TopicFieldRepository topicFieldRepository;
    private final TopicCategoryRepository topicCategoryRepository;

    private Iterable<TopicField> getALlFieldsAndCheckDuplicate(String name) throws ClientBadRequestError {
        var fields = topicFieldRepository.findAll();
        for (TopicField t: fields) {
            if(t.getName().equals(name))
                throw new ClientBadRequestError("Duplicate topic field");
        };
        return fields;
    }

    private Iterable<TopicCategory> getALlCatsAndCheckDuplicate(String name) throws ClientBadRequestError {
        var cats = topicCategoryRepository.findAll();
        for (TopicCategory t: cats) {
            if(t.getName().equals(name))
                throw new ClientBadRequestError("Duplicate topic category");
        };
        return cats;
    }


    public Iterable<TopicField> createTopicField(AdminController.CreateSimpleEntityRequest request) throws ClientBadRequestError {
        var fields = getALlFieldsAndCheckDuplicate(request.getName());


        var newField = new TopicField();
        newField.setName(request.getName());

        topicFieldRepository.save(newField);
        return topicFieldRepository.findAll();
    }

    public Iterable<TopicField> editTopicField(AdminController.CreateSimpleEntityRequest request, long id) throws ClientBadRequestError {
        var fields = getALlFieldsAndCheckDuplicate(request.getName());

        fields.forEach(f -> {
            if(f.getId().equals(id)) {
                f.setName(request.getName());
                topicFieldRepository.save(f);
            }
        });

        return topicFieldRepository.findAll();
    }

    private void updateSearchVectorWhenTopicFieldChange(long id){
        var field = topicFieldRepository.findById(id)
                .orElse(null);
        if(field == null)
            return;

    }

    public Iterable<TopicCategory> createTopicCategory(AdminController.CreateSimpleEntityRequest request) throws ClientBadRequestError {
        var cats = getALlCatsAndCheckDuplicate(request.getName());

        var newCat = new TopicCategory();
        newCat.setName(request.getName());

        topicCategoryRepository.save(newCat);
        return topicCategoryRepository.findAll();
    }

    public Iterable<TopicCategory> editTopicCats(AdminController.CreateSimpleEntityRequest request, long id) throws ClientBadRequestError {
        var cats = getALlCatsAndCheckDuplicate(request.getName());

        cats.forEach(f -> {
            if(f.getId().equals(id)) {
                f.setName(request.getName());
                topicCategoryRepository.save(f);
            }
        });

        return topicCategoryRepository.findAll();
    }

    public Iterable<TopicCategory> deleteCat(long id) throws ResourceNotFoundException, ClientBadRequestError {
        var cat = topicCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("cannot find topic category id: %s", id)));
        if(cat.getTopics().isEmpty()){
            topicCategoryRepository.delete(cat);
        }else {
            throw new ClientBadRequestError("attempt to delete category that has topic");
        }
        return topicCategoryRepository.findAll();
    }

    public Iterable<TopicField> deleteField(long id) throws ResourceNotFoundException, ClientBadRequestError {
        var field = topicFieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("cannot find topic field id: %s", id)));
        if(field.getTopics().isEmpty()){
            topicFieldRepository.delete(field);
        }else {
            throw new ClientBadRequestError("attempt to delete topic that has topic");
        }
        return topicFieldRepository.findAll();
    }}
