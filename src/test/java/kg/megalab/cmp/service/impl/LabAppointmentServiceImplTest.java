package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateLabAppointmentDto;
import kg.megalab.cmp.model.dto.response.LabAppointmentResponseDto;
import kg.megalab.cmp.model.entity.LabAppointment;
import kg.megalab.cmp.model.entity.LabSlot;
import kg.megalab.cmp.model.entity.LabTest;
import kg.megalab.cmp.model.entity.Patient;
import kg.megalab.cmp.model.enums.LabAppointmentStatus;
import kg.megalab.cmp.model.enums.LabSlotStatus;
import kg.megalab.cmp.repo.LabAppointmentRepo;
import kg.megalab.cmp.repo.LabSlotRepo;
import kg.megalab.cmp.repo.PatientRepo;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabAppointmentServiceImplTest {

    @Mock
    private LabAppointmentRepo labAppointmentRepo;

    @Mock
    private LabSlotRepo labSlotRepo;

    @Mock
    private PatientRepo patientRepo;

    @InjectMocks
    private LabAppointmentServiceImpl sut;

    private Patient testPatient;
    private LabTest testLabTest;
    private LabSlot testLabSlot;
    private LabAppointment testAppointment;
    private CreateLabAppointmentDto testDto;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setKeycloakId("patient-123");

        testLabTest = new LabTest();
        testLabTest.setId(2L);
        testLabTest.setName("Общий анализ крови");

        testLabSlot = new LabSlot();
        testLabSlot.setId(10L);
        testLabSlot.setLabTest(testLabTest);
        testLabSlot.setWorkDate(LocalDate.now().plusDays(1));
        testLabSlot.setStartTime(LocalTime.of(9, 0));
        testLabSlot.setEndTime(LocalTime.of(9, 30));
        testLabSlot.setStatus(LabSlotStatus.AVAILABLE);

        testAppointment = new LabAppointment();
        testAppointment.setId(5L);
        testAppointment.setPatient(testPatient);
        testAppointment.setLabSlot(testLabSlot);
        testAppointment.setStatus(LabAppointmentStatus.CREATED);

        testDto = new CreateLabAppointmentDto(1L, 10L);

        Jwt jwt = new Jwt("patient-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), Map.of("sub", "patient-123"));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bookLabAppointment_success() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        when(labSlotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testLabSlot));
        when(labAppointmentRepo.save(any(LabAppointment.class))).thenReturn(testAppointment);
        LabAppointmentResponseDto result = sut.bookLabAppointment(testDto);
        verify(patientRepo, times(1)).findById(1L);
        verify(labSlotRepo, times(1)).findByIdForUpdate(10L);
        verify(labAppointmentRepo, times(1)).save(any(LabAppointment.class));
        assertEquals(5L, result.id());
        assertEquals(LabAppointmentStatus.CREATED, result.status());
        assertEquals(LabSlotStatus.BOOKED, testLabSlot.getStatus());
    }

    @Test
    void bookLabAppointment_patientNotFound() {
        when(patientRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.bookLabAppointment(testDto));
        verify(labAppointmentRepo, never()).save(any(LabAppointment.class));
    }

    @Test
    void bookLabAppointment_accessDenied() {
        testPatient.setKeycloakId("another-patient");
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        assertThrows(AccessDeniedException.class, () -> sut.bookLabAppointment(testDto));
        verify(labSlotRepo, never()).findByIdForUpdate(any());
    }

    @Test
    void bookLabAppointment_slotNotFound() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        when(labSlotRepo.findByIdForUpdate(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.bookLabAppointment(testDto));
        verify(labAppointmentRepo, never()).save(any(LabAppointment.class));
    }

    @Test
    void bookLabAppointment_slotAlreadyBooked() {
        testLabSlot.setStatus(LabSlotStatus.BOOKED);
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        when(labSlotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testLabSlot));
        assertThrows(AlreadyExistsException.class, () -> sut.bookLabAppointment(testDto));
        verify(labAppointmentRepo, never()).save(any(LabAppointment.class));
    }

    @Test
    void bookLabAppointment_pastTime() {
        testLabSlot.setWorkDate(LocalDate.now().minusDays(1));
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        when(labSlotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testLabSlot));
        assertThrows(IllegalArgumentException.class, () -> sut.bookLabAppointment(testDto));
        verify(labAppointmentRepo, never()).save(any(LabAppointment.class));
    }

    @Test
    void cancelLabAppointment_success() {
        when(labAppointmentRepo.findById(5L)).thenReturn(Optional.of(testAppointment));
        when(labSlotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testLabSlot));
        LabAppointmentResponseDto result = sut.cancelLabAppointment(5L);
        verify(labAppointmentRepo, times(1)).findById(5L);
        verify(labSlotRepo, times(1)).findByIdForUpdate(10L);
        assertEquals(LabAppointmentStatus.CANCELED, result.status());
        assertEquals(LabSlotStatus.AVAILABLE, testLabSlot.getStatus());
    }

    @Test
    void cancelLabAppointment_notFound() {
        when(labAppointmentRepo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.cancelLabAppointment(5L));
    }

    @Test
    void cancelLabAppointment_alreadyCanceled() {
        testAppointment.setStatus(LabAppointmentStatus.CANCELED);
        when(labAppointmentRepo.findById(5L)).thenReturn(Optional.of(testAppointment));
        when(labSlotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testLabSlot));
        assertThrows(AlreadyExistsException.class, () -> sut.cancelLabAppointment(5L));
    }

    @Test
    void getPatientLabAppointments_success() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(testPatient));
        when(labAppointmentRepo.findByPatientId(1L)).thenReturn(List.of(testAppointment));
        List<LabAppointmentResponseDto> result = sut.getPatientLabAppointments(1L);
        verify(patientRepo, times(1)).findById(1L);
        verify(labAppointmentRepo, times(1)).findByPatientId(1L);
        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().id());
    }

    @Test
    void completeLabAppointment_success() {
        when(labAppointmentRepo.findById(5L)).thenReturn(Optional.of(testAppointment));
        LabAppointmentResponseDto result = sut.completeLabAppointment(5L);
        verify(labAppointmentRepo, times(1)).findById(5L);
        assertEquals(LabAppointmentStatus.COMPLETED, result.status());
    }

    @Test
    void completeLabAppointment_alreadyCompleted() {
        testAppointment.setStatus(LabAppointmentStatus.COMPLETED);
        when(labAppointmentRepo.findById(5L)).thenReturn(Optional.of(testAppointment));
        assertThrows(AlreadyExistsException.class, () -> sut.completeLabAppointment(5L));
    }

}
