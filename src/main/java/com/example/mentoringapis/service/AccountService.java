package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.AccountResponse;
import com.example.mentoringapis.models.upStreamModels.AccountUpdateRequest;
import com.example.mentoringapis.models.upStreamModels.MentorAccountResponse;
import com.example.mentoringapis.models.upStreamModels.StaffAccountResponse;
import com.example.mentoringapis.repositories.AccountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountsRepository accountsRepository;

    public List<AccountResponse> getAll(){
        return StreamSupport.stream(accountsRepository.findAll().spliterator(), false)
                .map(AccountResponse::fromAccountEntity)
                .collect(Collectors.toList());
    }

    public List<MentorAccountResponse> getMentors(){
        return accountsRepository
                .findAccountsByRole(Account.Role.MENTOR.name())
                .stream()
                .map(MentorAccountResponse::fromAccountEntity)
                .collect(Collectors.toList());
    }

    public List<StaffAccountResponse> getStaffs(){
        return accountsRepository
                .findAccountsByRole(Account.Role.STAFF.name())
                .stream()
                .map(StaffAccountResponse::fromAccountEntity)
                .collect(Collectors.toList());
    }

    public List<AccountResponse> updateStatus(UUID id, AccountUpdateRequest accountUpdateRequest) throws ResourceNotFoundException {
        var account = accountsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find account with id: %s", id)));
        account.setStatus(accountUpdateRequest.getStatus());
        accountsRepository.save(account);
        return getAll();
    }

    public List<UUID> invalidateMentors(List<UUID> uuids) throws ResourceNotFoundException {
        var mentors = accountsRepository.findAllById(uuids);
        if(mentors.size() != uuids.size()){
            throw new ResourceNotFoundException(String.format("Requested ids: %s\n" +
                    "Found ids: %s",
                    Arrays.toString(uuids.toArray()),
                    Arrays.toString(mentors.stream().map(Account::getId).toArray())));
        }

        mentors.forEach(m -> m.setStatus(Account.Status.INVALIDATE.name()));
        accountsRepository.saveAll(mentors);
        return uuids;
    }
}
