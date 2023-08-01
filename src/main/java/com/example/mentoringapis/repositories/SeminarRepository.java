package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.Seminar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeminarRepository extends JpaRepository<Seminar, Long> {
    @Query("select sem from Seminar  sem " +
            "left join fetch sem.department " +
            "left join fetch sem.mentors m " +
            "left join fetch m.account ")
    List<Seminar> findAll();

    List<Seminar> findAllByDepartment(Department department);
    Optional<Seminar> findById(Long id);
    Page<Seminar> findAll(Pageable pageable);

    @Query(value = "select  s.id" +
            "    from seminars s\n" +
            "    where start_time between ?1 \\:\\:timestamp and ?2 \\:\\:timestamp\n",
            nativeQuery = true)
    List<Long> findAllByStartTimeBetween(String startTime, String endTime);

    @Query(value = "select  s.id" +
            "    from seminars s\n" +
            "    inner join (select * from seminars_mentors sm where sm.user_profile_id\\:\\:text like %?5%) as sm on (sm.seminar_id = s.id) " +
            "    left join user_profiles up on (up.account_id = sm.user_profile_id)\n" +
            "    where start_time between ?1 \\:\\:timestamp and ?2 \\:\\:timestamp\n" +
            "    and department_id = ?4\n" +
            "    and (up.full_name like %?3% or s.name like %?3%) \n" +
            "    GROUP BY(s.id)" +
            "    order by abs(DATE_PART('day',s.start_time - now())*24 + DATE_PART('hour',s.start_time - now()))",
            nativeQuery = true)
    List<Long> byDate(String startTime, String endTime, String searchName, int departmentId, String mentorId);

    @Query(value = "select  s.id" +
            "    from seminars s\n" +
            "    inner join (select * from seminars_mentors sm left join user_profiles on (user_profiles.account_id = sm.user_profile_id) where sm.user_profile_id\\:\\:text like %?4% ) as sm on (sm.seminar_id = s.id) " +
            "    where start_time between ?1 \\:\\:timestamp and ?2 \\:\\:timestamp\n" +
            "    and (sm.full_name like %?3% or s.name like %?3%) \n" +
            "    GROUP BY(s.id)" +
            "    order by abs(DATE_PART('day',s.start_time - now())*24 + DATE_PART('hour',s.start_time - now()))",
            nativeQuery = true)
    List<Long> byDate(String startTime, String endTime, String searchName, String mentorId);

    @Query(value = "select seminar from Seminar seminar " +
            " left join fetch seminar.mentors m" +
            " left join fetch m.account a" +
            " left join fetch seminar.department" +
            " where seminar.id in ?1")
    List<Seminar> findAllById(Iterable<Long> ids);

    @Query(value = "select seminar from Seminar seminar " +
            " left join fetch seminar.mentors m" +
            " left join fetch m.account a" +
            " left join fetch seminar.department")
    List<Seminar> findAllByMentors();
}
