package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query("select dep from Department dep " +
            "left join fetch dep.staffAccounts staffs " +
            "left join fetch dep.seminars sem " +
            "left join fetch sem.mentors men " +
            "left join fetch staffs.userProfile up")
    List<Department> findAllDepartments();
    List<Department> findByNameEquals(String name);
}
