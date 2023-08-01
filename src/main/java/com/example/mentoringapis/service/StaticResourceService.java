package com.example.mentoringapis.service;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static com.example.mentoringapis.configurations.ConstantConfiguration.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class StaticResourceService {
    private final Bucket bucket;

    public String uploadImage(MultipartFile file) throws ClientBadRequestError, IOException {
        var blobName = IMAGE_RESOURCE_PATH.concat(UUID.randomUUID().toString());
        if(file.getContentType() == null || !file.getContentType().contains("image")){
            throw new ClientBadRequestError("File content type must be in image/* format");
        }
        Blob b = bucket.create(blobName, file.getBytes(), file.getContentType());
        return b.getMediaLink();
    }

    public String uploadAttachment(MultipartFile file) throws IOException {
        var blobName = String.format(ATTACHMENTS_RESOURCE_FORMAT, UUID.randomUUID().toString(), file.getOriginalFilename());
        Blob b = bucket.create(blobName, file.getBytes(), file.getContentType());
        return b.getMediaLink();
    }

    public String uploadJsonPayload(byte[] content) throws IOException {
        var blobName = SEMINAR_FEEDBACK_PATH.concat(UUID.randomUUID().toString());
        Blob b = bucket.create(blobName, content, "json");
        return b.getName();
    }

}
