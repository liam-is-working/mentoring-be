package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateDepartmentRequest;
import com.example.mentoringapis.models.upStreamModels.DepartmentResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateDepartmentRequest;
import com.example.mentoringapis.repositories.AccountsRepository;
import com.example.mentoringapis.repositories.DepartmentRepository;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final AccountsRepository accountsRepository;

    public List<DepartmentResponse> getAll(){
        return departmentRepository.findAllDepartments().stream().map(DepartmentResponse::fromDepartment).toList();
    }

    public DepartmentResponse createDepartment(CreateDepartmentRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var staffAccounts = accountsRepository.findAllById(request.getStaffIds());
        var dep = departmentRepository.findByNameEquals(request.getName());
        if(!dep.isEmpty())
            throw new ClientBadRequestError(String.format("Duplicate department name: %s", request.getName()));

        for (Account acc : staffAccounts) {
            if(!Account.Role.STAFF.name().equals(acc.getRole()))
                throw new ResourceNotFoundException(String.format("Cannot find satff with id: %s",acc.getId()));
        }
        var department = new Department();
        department.setName(request.getName());
        department.setStaffAccounts(new HashSet<>(staffAccounts));
        departmentRepository.save(department);
        return DepartmentResponse.fromDepartment(department);
    }

    public Optional<List<Account>> getStaffAccounts(List<UUID> ids) throws ResourceNotFoundException {
        if(ids == null)
            return Optional.empty();
        var staffAccounts = accountsRepository.findAllById(ids);
        for (Account acc : staffAccounts) {
            if(!Account.Role.STAFF.name().equals(acc.getRole()))
                throw new ResourceNotFoundException(String.format("Cannot find staff with id: %s",acc.getId()));
        }
        return Optional.of(staffAccounts);
    }

    public Department getDepartment(Integer id) throws ResourceNotFoundException {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find department with id: %s", id)));

    }

    public DepartmentResponse updateDepartment(UpdateDepartmentRequest request, Integer departmentId) throws ResourceNotFoundException {
        var department = getDepartment(departmentId);
        var staffs = getStaffAccounts(request.getStaffIds());
        staffs.ifPresent(s -> department.setStaffAccounts(new HashSet<>(s)));
        Optional.ofNullable(request.getName()).ifPresent(department::setName);

        departmentRepository.save(department);
        return DepartmentResponse.fromDepartment(department);
    }

    public void deleteDepartment(int id) throws ResourceNotFoundException, ClientBadRequestError {
        var department = getDepartment(id);
        if(!department.getStaffAccounts().isEmpty())
            throw new ClientBadRequestError(String.format("Department [%s] staff list are not empty", id));
        departmentRepository.delete(department);
    }

    public DepartmentResponse addStaffs(List<UUID> ids, Integer departmentId) throws ResourceNotFoundException {
        var staffs = getStaffAccounts(ids);
        var department = getDepartment(departmentId);
        staffs.ifPresent(s -> s.forEach(staff -> {
            staff.setDepartment(department);
            accountsRepository.save(staff);
        }));
        return DepartmentResponse.fromDepartment(department);
    }

    public DepartmentResponse removeStaffs(List<UUID> ids, Integer departmentId) throws ResourceNotFoundException {
        var staffsToRemove = getStaffAccounts(ids);
        var department = getDepartment(departmentId);
        staffsToRemove.ifPresent(s -> s.forEach(staff -> {
//            staff.setDepartment(null);
//            accountsRepository.save(staff);
            department.getStaffAccounts().removeIf(acc -> acc.getId().equals(staff.getId()));
        }));
        departmentRepository.save(department);
        return DepartmentResponse.fromDepartment(department);
    }

}
