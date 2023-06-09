package com.example.mentoringapis.security;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Data
public class CustomUserDetails implements UserDetails {
    Account account;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(account.getRole()));
    }
    public Integer getDepartmentId(){
        return Optional.ofNullable(account.getDepartment())
                .map(Department::getId)
                .orElse(null);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return account.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getRole(){
        return account.getRole();
    }
}
