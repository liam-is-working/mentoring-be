package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.service.StaticResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("resource")
@RequiredArgsConstructor
public class StaticResourceController {
    private final StaticResourceService staticResourceService;
    @PostMapping("/images")
    public ResponseEntity<String> uploadFile(@RequestParam("image") MultipartFile file) throws IOException, ClientBadRequestError {
        return  ResponseEntity.ok(staticResourceService.uploadImage(file));
    }

    @PostMapping("/attachments")
    public ResponseEntity<String> uploadAttachments(@RequestParam("attachment") MultipartFile file) throws IOException, ClientBadRequestError {
        return  ResponseEntity.ok(staticResourceService.uploadAttachment(file));
    }
}
