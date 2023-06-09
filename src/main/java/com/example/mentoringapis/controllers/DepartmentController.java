package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.models.upStreamModels.CreateSeminarRequest;
import com.example.mentoringapis.models.upStreamModels.DepartmentResponse;
import com.example.mentoringapis.models.upStreamModels.SeminarResponse;
import com.example.mentoringapis.repositories.DepartmentRepository;
import com.example.mentoringapis.service.SeminarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentRepository
                .findAll().stream()
                .map(DepartmentResponse::fromDepartment)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create() {
        var newDepartment = new Department();
        newDepartment.setName("randomDepartment");
        return ResponseEntity.ok(DepartmentResponse.fromDepartment(departmentRepository.save(newDepartment)));
    }

}
