package kg.megalab.cmp.repo;

import kg.megalab.cmp.model.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface ScheduleRepo extends JpaRepository<Schedule, Long> {

    @Query("""
            SELECT COUNT(s) > 0 FROM Schedule s
            WHERE s.doctor.id = :doctorId
            AND s.workDate = :workDate
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            AND (:excludeId IS NULL OR s.id <> :excludeId)
            """)
    boolean existsOverlappingSchedule(@Param("doctorId") Long doctorId,
                                      @Param("workDate") LocalDate workDate,
                                      @Param("startTime") LocalTime startTime,
                                      @Param("endTime") LocalTime endTime,
                                      @Param("excludeId") Long excludeId);

}