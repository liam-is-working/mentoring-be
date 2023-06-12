package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountsRepository extends CrudRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);
    @Query("select account from Account account left join fetch account.userProfile " +
            "where account.role like ?1")
    List<Account> findAccountsByRole(String role);
    @Override
    @Query("select account from Account account left join fetch account.userProfile " +
            "where account.id in ?1")
    List<Account> findAllById(Iterable<UUID> uuids);
    Optional<Account> findAccountsByIdAndRole(UUID id, String role);
}
