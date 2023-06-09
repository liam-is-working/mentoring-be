package com.example.mentoringapis.service;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.mentoringapis.configurations.ConstantConfiguration.*;

@Service
@RequiredArgsConstructor
public class StaticResourceService {
    private final Bucket bucket;

    public String uploadImage(MultipartFile file) throws ClientBadRequestError, IOException {
        var blobName = IMAGE_RESOURCE_PATH.concat(UUID.randomUUID().toString());
        if(file.getContentType() == null || !file.getContentType().contains("image")){
            throw ClientBadRequestError.builder()
                    .errorMessages("File content type must be in image/* format")
                    .build();
        }
        Blob b = bucket.create(blobName, file.getBytes(), file.getContentType());
        return b.getName();
    }

    public String uploadAttachment(MultipartFile file) throws IOException {
        var blobName = ATTACHMENTS_RESOURCE_PATH.concat(UUID.randomUUID().toString());
        Blob b = bucket.create(blobName, file.getBytes(), file.getContentType());
        return b.getName();
    }

    public String uploadJsonPayload(byte[] content) throws IOException {
        var blobName = SEMINAR_FEEDBACK_PATH.concat(UUID.randomUUID().toString());
        Blob b = bucket.create(blobName, content, "json");
        return b.getName();
    }

    public URL generateResourceUrl(String filename){
        return Optional.ofNullable(filename)
                .filter(Strings::isNotEmpty)
                .map(bucket::get)
                .map(blob -> blob.signUrl(10, TimeUnit.HOURS))
                .orElse(null);
    }
}
