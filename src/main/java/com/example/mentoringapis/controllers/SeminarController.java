package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.paging.CustomPagingResponse;
import com.example.mentoringapis.models.upStreamModels.CreateSeminarRequest;
import com.example.mentoringapis.models.upStreamModels.SeminarResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateSeminarRequest;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.SeminarService;
import com.example.mentoringapis.utilities.DateTimeUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.mentoringapis.configurations.ConstantConfiguration.DEFAULT_SEMINAR_PAGE_SIZE;
import static com.example.mentoringapis.utilities.DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER;
import static com.example.mentoringapis.utilities.DateTimeUtils.DEFAULT_DATE_TIME_PATTERN;

@RestController
@RequestMapping("/seminars")
@RequiredArgsConstructor
public class SeminarController {
    private final SeminarService seminarService;

    //TODO fix default
    @GetMapping("/search")
    public ResponseEntity<CustomPagingResponse<SeminarResponse>> getAll(
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "") String searchString,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize) {
        if(pageSize==null)
            pageSize = DEFAULT_SEMINAR_PAGE_SIZE;
        if(pageIndex==null)
            pageIndex = 0;
        if(startDate==null || endDate==null){
            startDate = "1900-01-01";
            endDate = "2900-01-01";
        }
        if ("past".equals(status)) {
            endDate = DateTimeUtils.nowInVietnam().format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN));
        }else if( "future".equals(status)){
            startDate = DateTimeUtils.nowInVietnam().format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN));
        }else {
            endDate = DateTimeUtils.parseRoundDate(endDate).plusDays(1).format(DEFAULT_DATE_TIME_FORMATTER);
        }
        return ResponseEntity.ok(seminarService.searchByDateAndName(startDate,endDate,searchString, departmentId,status, PageRequest.of(pageIndex,pageSize)));
    }

    @GetMapping("/{seminarId}")
    public ResponseEntity<SeminarResponse> getById(@PathVariable long seminarId) throws ResourceNotFoundException {
        return ResponseEntity.ok(seminarService.getById(seminarId));
    }

    @GetMapping("/byDepartment/{departmentId}")
    public ResponseEntity<List<SeminarResponse>> getByDepartmentId(@PathVariable int departmentId) throws ResourceNotFoundException {
        return ResponseEntity.ok(seminarService.getALlByDepartmentId(departmentId));
    }

    @GetMapping("/byMyDepartment")
    public ResponseEntity<List<SeminarResponse>> getByMyDepartment(Authentication authentication) throws ResourceNotFoundException {
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(seminarService.getALlByDepartmentId(currentUser.getDepartmentId()));
    }

//    @Secured("STAFF") TODO:authorize
    @PostMapping
    public ResponseEntity<SeminarResponse> create(@Valid @RequestBody CreateSeminarRequest request, Authentication authentication) throws ClientBadRequestError, IOException {
//        var currentUser = (CustomUserDetails) authentication.getPrincipal();
//        return ResponseEntity.ok(seminarService.create(request, currentUser.getDepartmentId()));

        if(authentication != null){
            var currentUser = (CustomUserDetails) authentication.getPrincipal();
            return ResponseEntity.ok(seminarService.create(request, currentUser.getDepartmentId()));
        }else {
            return ResponseEntity.ok(seminarService.create(request, null));
        }
    }

    @PostMapping("/{seminarId}")
    public ResponseEntity<SeminarResponse> create(@Valid @RequestBody UpdateSeminarRequest request, @PathVariable Long seminarId) throws ClientBadRequestError, ResourceNotFoundException {
        return ResponseEntity.ok(seminarService.update(request, seminarId));
    }

    @DeleteMapping("{seminarId}")
    public ResponseEntity<Long> delete(@PathVariable Long seminarId) throws ClientBadRequestError, ResourceNotFoundException {
        return ResponseEntity.ok(seminarService.deleteSeminar(seminarId));
    }


}
