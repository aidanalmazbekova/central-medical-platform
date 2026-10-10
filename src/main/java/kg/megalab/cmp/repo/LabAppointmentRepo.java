package kg.megalab.cmp.repo;

import kg.megalab.cmp.model.entity.LabAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabAppointmentRepo extends JpaRepository<LabAppointment, Long> {

    List<LabAppointment> findByPatientId(Long patientId);

}