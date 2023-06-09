package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.Seminar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface SeminarRepository extends JpaRepository<Seminar, Long> {
    List<Seminar> findAll();

    List<Seminar> findAllByStartTimeBetweenAndNameContainingOrStartTimeBetweenAndMentorsFullNameContaining(
            LocalDateTime startDate1, LocalDateTime endDate1, String name,
            LocalDateTime startDate2, LocalDateTime endDate2, String mentorFullName );
    List<Seminar> findAllByNameContainingOrMentorsFullNameContaining(
            String name, String mentorFullName );
    List<Seminar> findAllByDepartment(Department department);
    Optional<Seminar> findById(Long id);
    Page<Seminar> findAll(Pageable pageable);
    @Query(value = "select s.id, s.description, s.name, s.start_time, s.location, s.image_url, s.department_id" +
            "    from seminars s\n" +
//            "    left join seminars_mentors sm on (sm.seminar_id = s.id)\n" +
//            "    left join user_profiles up on (up.account_id = sm.user_profile_id)\n" +
            "    where start_time between ?1 \\:\\:timestamp and ?2 \\:\\:timestamp\n" +
            "    order by abs(DATE_PART('day',s.start_time - now())*24 + DATE_PART('hour',s.start_time - now()))",
            nativeQuery = true,
            countQuery = "select count(*)\n" +
                    "from seminars s\n" +
                    "where start_time between ?1 \\:\\:timestamp and ?2 \\:\\:timestamp"
    )
    Page<Seminar> sortByDate(String startTime, String endTime, Pageable pageable);
    @Query(value = "select  s.id" +
            "    from seminars s\n" +
            "    left join seminars_mentors sm on (sm.seminar_id = s.id)\n" +
            "    left join user_profiles up on (up.account_id = sm.user_profile_id)\n" +
            "    where start_time between ?1 \\:\\:timestamp and ?2 \\:\\:timestamp\n" +
            "    and department_id = ?4\n" +
            "    and (up.full_name like %?3% or s.name like %?3%) \n" +
            "    GROUP BY(s.id)" +
            "    order by abs(DATE_PART('day',s.start_time - now())*24 + DATE_PART('hour',s.start_time - now()))",
            nativeQuery = true)
    List<Long> byDate(String startTime, String endTime, String searchName, int departmentId);

    @Query(value = "select  s.id" +
            "    from seminars s\n" +
            "    left join seminars_mentors sm on (sm.seminar_id = s.id)\n" +
            "    left join user_profiles up on (up.account_id = sm.user_profile_id)\n" +
            "    where start_time between ?1 \\:\\:timestamp and ?2 \\:\\:timestamp\n" +
            "    and (up.full_name like %?3% or s.name like %?3%) \n" +
            "    GROUP BY(s.id)" +
            "    order by abs(DATE_PART('day',s.start_time - now())*24 + DATE_PART('hour',s.start_time - now()))",
            nativeQuery = true)
    List<Long> byDate(String startTime, String endTime, String searchName);
}
