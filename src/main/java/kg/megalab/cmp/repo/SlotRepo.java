package kg.megalab.cmp.repo;

import jakarta.persistence.LockModeType;
import kg.megalab.cmp.model.entity.Slot;
import kg.megalab.cmp.model.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepo extends JpaRepository<Slot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);

    List<Slot> findByStatus(SlotStatus status);

    List<Slot> findByScheduleId(Long scheduleId);

    void deleteByScheduleId(Long scheduleId);

    List<Slot> findByStatusAndScheduleDoctorId(SlotStatus status, Long doctorId);

}