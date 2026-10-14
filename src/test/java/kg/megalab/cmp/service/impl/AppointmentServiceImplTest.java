package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateAppointmentDto;
import kg.megalab.cmp.model.dto.response.AppointmentResponseDto;
import kg.megalab.cmp.model.entity.*;
import kg.megalab.cmp.model.enums.AppointmentStatus;
import kg.megalab.cmp.model.enums.SlotStatus;
import kg.megalab.cmp.repo.AppointmentRepo;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.PatientRepo;
import kg.megalab.cmp.repo.SlotRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepo appointmentRepo;

    @Mock
    private PatientRepo patientRepo;

    @Mock
    private SlotRepo slotRepo;

    @Mock
    private DoctorRepo doctorRepo;

    @InjectMocks
    private AppointmentServiceImpl sut;

    private CreateAppointmentDto testDto;
    private Patient testPatient;
    private Doctor testDoctor;
    private Slot testSlot;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testDto = new CreateAppointmentDto(1L, 10L);

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setKeycloakId("patient-123");

        testDoctor = new Doctor();
        testDoctor.setId(2L);
        testDoctor.setKeycloakId("doctor-123");

        Schedule testSchedule = new Schedule();
        testSchedule.setDoctor(testDoctor);
        testSchedule.setWorkDate(LocalDate.now().plusDays(1));

        testSlot = new Slot();
        testSlot.setId(10L);
        testSlot.setSchedule(testSchedule);
        testSlot.setStartTime(LocalTime.of(10, 0));
        testSlot.setEndTime(LocalTime.of(10, 30));
        testSlot.setStatus(SlotStatus.AVAILABLE);

        testAppointment = new Appointment();
        testAppointment.setId(100L);
        testAppointment.setPatient(testPatient);
        testAppointment.setSlot(testSlot);
        testAppointment.setStatus(AppointmentStatus.CREATED);

        Jwt jwt = new Jwt("patient-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), Map.of("sub", "patient-123"));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateDoctor() {
        Jwt jwt = new Jwt("doctor-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), Map.of("sub", "doctor-123"));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bookAppointment_success() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        when(slotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testSlot));

        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(100L);
            return appointment;
        });

        AppointmentResponseDto result = sut.bookAppointment(testDto);

        verify(patientRepo, times(1)).findById(1L);
        verify(slotRepo, times(1)).findByIdForUpdate(10L);
        verify(slotRepo, times(1)).save(any(Slot.class));
        verify(appointmentRepo, times(1)).save(any(Appointment.class));

        assertEquals(AppointmentStatus.CREATED, result.status());
        assertEquals(SlotStatus.BOOKED, testSlot.getStatus());
        assertEquals(1L, result.patientId());
        assertEquals(10L, result.slotId());
    }

    @Test
    void bookAppointment_slotAlreadyBooked() {
        testSlot.setStatus(SlotStatus.BOOKED);
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        when(slotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testSlot));
        assertThrows(AlreadyExistsException.class, () -> sut.bookAppointment(testDto));
        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    void bookAppointment_accessDenied() {
        testPatient.setKeycloakId("another-patient");
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        assertThrows(AccessDeniedException.class, () -> sut.bookAppointment(testDto));
        verify(slotRepo, never()).findByIdForUpdate(10L);
        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_success() {
        testSlot.setStatus(SlotStatus.BOOKED);
        when(appointmentRepo.findByIdForUpdate(100L)).thenReturn(Optional.of(testAppointment));
        when(slotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testSlot));
        when(appointmentRepo.save(any(Appointment.class))).thenReturn(testAppointment);
        AppointmentResponseDto result = sut.cancelAppointment(100L);
        verify(appointmentRepo, times(1)).save(any(Appointment.class));
        verify(slotRepo, times(1)).save(any(Slot.class));
        assertEquals(AppointmentStatus.CANCELED, result.status());
        assertEquals(SlotStatus.AVAILABLE, testSlot.getStatus());
    }

    @Test
    void cancelAppointment_notFound() {
        when(appointmentRepo.findByIdForUpdate(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.cancelAppointment(100L));
        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_alreadyCanceled() {
        testAppointment.setStatus(AppointmentStatus.CANCELED);
        when(appointmentRepo.findByIdForUpdate(100L)).thenReturn(Optional.of(testAppointment));
        assertThrows(AlreadyExistsException.class, () -> sut.cancelAppointment(100L));
        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_accessDenied() {
        testPatient.setKeycloakId("another-patient");
        when(appointmentRepo.findByIdForUpdate(100L)).thenReturn(Optional.of(testAppointment));
        assertThrows(AccessDeniedException.class, () -> sut.cancelAppointment(100L));
        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    void completeAppointment_success() {
        authenticateDoctor();
        when(appointmentRepo.findByIdForUpdate(100L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepo.save(any(Appointment.class))).thenReturn(testAppointment);
        AppointmentResponseDto result = sut.completeAppointment(100L);
        verify(appointmentRepo, times(1)).save(any(Appointment.class));
        assertEquals(AppointmentStatus.COMPLETED, result.status());
    }

    @Test
    void completeAppointment_notFound() {
        when(appointmentRepo.findByIdForUpdate(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.completeAppointment(100L));
        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    void completeAppointment_accessDenied() {
        when(appointmentRepo.findByIdForUpdate(100L)).thenReturn(Optional.of(testAppointment));
        assertThrows(AccessDeniedException.class, () -> sut.completeAppointment(100L));
        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    void completeAppointment_alreadyCompleted() {
        authenticateDoctor();
        testAppointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepo.findByIdForUpdate(100L)).thenReturn(Optional.of(testAppointment));
        assertThrows(AlreadyExistsException.class, () -> sut.completeAppointment(100L));
        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    void getAllAppointments_success() {
        when(appointmentRepo.findAll()).thenReturn(List.of(testAppointment));
        List<AppointmentResponseDto> result = sut.getAllAppointments();
        verify(appointmentRepo, times(1)).findAll();
        assertEquals(1, result.size());
        assertEquals(100L, result.getFirst().id());
    }

    @Test
    void getAppointmentsByPatient_success() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepo.findByPatientId(1L)).thenReturn(List.of(testAppointment));
        List<AppointmentResponseDto> result = sut.getAppointmentsByPatient(1L);
        verify(patientRepo, times(1)).findById(1L);
        verify(appointmentRepo, times(1)).findByPatientId(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().patientId());
    }

    @Test
    void getAppointmentsByPatient_accessDenied() {
        testPatient.setKeycloakId("another-patient");
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        assertThrows(AccessDeniedException.class, () -> sut.getAppointmentsByPatient(1L));
        verify(appointmentRepo, never()).findByPatientId(1L);
    }


    @Test
    void getAppointmentsByDoctor_success() {
        authenticateDoctor();
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepo.findBySlotScheduleDoctorId(2L)).thenReturn(List.of(testAppointment));
        List<AppointmentResponseDto> result = sut.getAppointmentsByDoctor(2L);
        verify(doctorRepo, times(1)).findById(2L);
        verify(appointmentRepo, times(1)).findBySlotScheduleDoctorId(2L);
        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().doctorId());
    }

    @Test
    void getAppointmentsByFilters_success() {
        when(patientRepo.findByKeycloakId("patient-123")).thenReturn(Optional.of(testPatient));
        when(appointmentRepo.findByFilters(AppointmentStatus.CREATED, null, null, null, 1L, null)).thenReturn(List.of(testAppointment));
        List<AppointmentResponseDto> result = sut.getAppointmentsByFilters(AppointmentStatus.CREATED, null, null, null);
        verify(appointmentRepo, times(1)).findByFilters(AppointmentStatus.CREATED, null, null, null, 1L, null);
        assertEquals(1, result.size());
        assertEquals(AppointmentStatus.CREATED, result.getFirst().status());
    }

}
