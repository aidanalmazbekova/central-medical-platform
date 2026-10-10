package kg.megalab.cmp.repo;

import jakarta.persistence.LockModeType;
import kg.megalab.cmp.model.entity.LabSlot;
import kg.megalab.cmp.model.enums.LabSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabSlotRepo extends JpaRepository<LabSlot, Long> {

    List<LabSlot> findByStatus(LabSlotStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM LabSlot s WHERE s.id = :id")
    Optional<LabSlot> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT COUNT(s) > 0 FROM LabSlot s
            WHERE s.labTest.id = :labTestId
            AND s.workDate = :workDate
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            """)
    boolean existsOverlappingSlot(@Param("labTestId") Long labTestId,
                                  @Param("workDate") LocalDate workDate,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);

}