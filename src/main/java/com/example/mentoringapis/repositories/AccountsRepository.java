package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountsRepository extends CrudRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);
    List<Account> findAccountsByRole(String role);
    List<Account> findAllById(Iterable<UUID> uuids);
    Optional<Account> findAccountsByIdAndRole(UUID id, String role);
}
