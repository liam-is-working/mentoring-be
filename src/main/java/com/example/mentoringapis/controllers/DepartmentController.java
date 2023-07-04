package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateDepartmentRequest;
import com.example.mentoringapis.models.upStreamModels.DepartmentResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateDepartmentRequest;
import com.example.mentoringapis.repositories.DepartmentRepository;
import com.example.mentoringapis.service.DepartmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentRepository
                .findAll().stream()
                .map(DepartmentResponse::fromDepartment)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@RequestBody CreateDepartmentRequest request) throws ResourceNotFoundException {
        return ResponseEntity.ok(departmentService.createDepartment(request));
    }

    @PostMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponse> update(@RequestBody UpdateDepartmentRequest request, @PathVariable int departmentId) throws ResourceNotFoundException {
        return ResponseEntity.ok(departmentService.updateDepartment(request, departmentId));
    }

    @PostMapping("/{departmentId}/staffs/add")
    public ResponseEntity<DepartmentResponse> addStaff(@RequestBody StaffListIds ids, @PathVariable int departmentId) throws ResourceNotFoundException {
        return ResponseEntity.ok(departmentService.addStaffs(ids.staffIds, departmentId));
    }

    @PostMapping("/{departmentId}/staffs/remove")
    public ResponseEntity<DepartmentResponse> removeStaff(@RequestBody StaffListIds ids, @PathVariable int departmentId) throws ResourceNotFoundException {
        return ResponseEntity.ok(departmentService.removeStaffs(ids.staffIds, departmentId));
    }

    @Data
    static class StaffListIds{
        private List<UUID> staffIds;
    }

}
