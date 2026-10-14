package kg.megalab.cmp.repo;

import jakarta.persistence.LockModeType;
import kg.megalab.cmp.model.entity.Appointment;
import kg.megalab.cmp.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findBySlotScheduleDoctorId(Long doctorId);

    boolean existsBySlotId(Long slotId);

    @Query("""
                        SELECT a FROM Appointment a
                        WHERE (:status IS NULL OR a.status = :status)
                        AND (CAST(:specialty AS string) IS NULL OR
                        LOWER(a.slot.schedule.doctor.specialty.name)
                        LIKE LOWER(CONCAT('%', CAST(:specialty AS string), '%')))
                        AND (CAST(:doctorName AS string) IS NULL OR
                        LOWER(CONCAT(
                a.slot.schedule.doctor.firstName, ' ',
                a.slot.schedule.doctor.lastName, ' ',
                COALESCE(a.slot.schedule.doctor.middleName, '')
            )) LIKE LOWER(CONCAT('%', CAST(:doctorName AS string), '%')))
                        AND (CAST(:patientName AS string) IS NULL OR
                             LOWER(CONCAT(
                                 a.patient.firstName, ' ',
                                 a.patient.lastName, ' ',
                                 COALESCE(a.patient.middleName, '')
                             )) LIKE LOWER(CONCAT('%', CAST(:patientName AS string), '%')))
                        AND (:patientId IS NULL OR a.patient.id = :patientId)
                        AND (:doctorId IS NULL OR a.slot.schedule.doctor.id = :doctorId)
            """)
    List<Appointment> findByFilters(
            @Param("status") AppointmentStatus status,
            @Param("specialty") String specialty,
            @Param("doctorName") String doctorName,
            @Param("patientName") String patientName,
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId
    );

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.status = :status
            AND a.slot.schedule.workDate = :date
            """)
    List<Appointment> findByStatusAndDate(
            @Param("status") AppointmentStatus status,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT a FROM Appointment a
            JOIN a.slot s
            JOIN s.schedule sch
            WHERE a.status = :status
            AND a.reminderSent = false
            AND sch.workDate BETWEEN :startDate AND :endDate
            """)
    List<Appointment> findAppointmentsForReminder(
            @Param("status") AppointmentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.slot.schedule.doctor.id = :doctorId
            AND a.status = :status
            AND a.slot.schedule.workDate BETWEEN :startDate AND :endDate
            """)
    long countAppointmentsForReport(
            @Param("doctorId") Long doctorId,
            @Param("status") AppointmentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.id = :id")
    Optional<Appointment> findByIdForUpdate(@Param("id") Long id);

}