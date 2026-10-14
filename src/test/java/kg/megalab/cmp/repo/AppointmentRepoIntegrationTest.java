package kg.megalab.cmp.repo;

import kg.megalab.cmp.model.entity.*;
import kg.megalab.cmp.model.enums.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class AppointmentRepoIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private SpecialtyRepo specialtyRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private ScheduleRepo scheduleRepo;

    @Autowired
    private SlotRepo slotRepo;

    private Patient patient;
    private Doctor doctor;
    private Slot slot;

    @BeforeEach
    void setUp() {
        Specialty specialty = new Specialty();
        specialty.setName("Кардиология");
        specialty = specialtyRepo.save(specialty);

        doctor = new Doctor();
        doctor.setFirstName("Алина");
        doctor.setLastName("Иванова");
        doctor.setSpecialty(specialty);
        doctor = doctorRepo.save(doctor);

        patient = new Patient();
        patient.setFirstName("Айгерим");
        patient.setLastName("Асанова");
        patient = patientRepo.save(patient);

        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);
        schedule.setWorkDate(LocalDate.now().plusDays(1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setSlotDurationMinutes(30);
        schedule = scheduleRepo.save(schedule);

        slot = new Slot();
        slot.setSchedule(schedule);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(9, 30));
        slot = slotRepo.save(slot);
    }

    @Test
    void saveAppointment() {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        Appointment saved = appointmentRepo.save(appointment);
        assertNotNull(saved.getId());
        assertEquals(AppointmentStatus.CREATED, saved.getStatus());
        assertEquals(patient.getId(), saved.getPatient().getId());
    }

    @Test
    void existsBySlotId() {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointmentRepo.save(appointment);
        boolean exists = appointmentRepo.existsBySlotId(slot.getId());
        assertTrue(exists);
    }

    @Test
    void findByPatientId() {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointmentRepo.save(appointment);
        List<Appointment> appointments = appointmentRepo.findByPatientId(patient.getId());
        assertEquals(1, appointments.size());
    }

    @Test
    void findByFilters() {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointmentRepo.save(appointment);

        List<Appointment> appointments = appointmentRepo.findByFilters(AppointmentStatus.CREATED, "кардио",
                "Алина Иванова", null, patient.getId(), doctor.getId());
        assertEquals(1, appointments.size());
    }

    @Test
    void findByStatusAndDate() {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointmentRepo.save(appointment);

        List<Appointment> appointments = appointmentRepo.findByStatusAndDate(AppointmentStatus.CREATED, LocalDate.now().plusDays(1));
        assertEquals(1, appointments.size());
    }

}
