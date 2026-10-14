package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.response.SlotResponseDto;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.model.entity.Schedule;
import kg.megalab.cmp.model.entity.Slot;
import kg.megalab.cmp.model.enums.SlotStatus;
import kg.megalab.cmp.repo.AppointmentRepo;
import kg.megalab.cmp.repo.DoctorRepo;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @Mock
    private SlotRepo slotRepo;

    @Mock
    private AppointmentRepo appointmentRepo;

    @Mock
    private DoctorRepo doctorRepo;

    @InjectMocks
    private SlotServiceImpl sut;

    private Doctor testDoctor;
    private Schedule testSchedule;
    private Slot testSlot;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor();
        testDoctor.setId(2L);
        testDoctor.setKeycloakId("doctor-123");

        testSchedule = new Schedule();
        testSchedule.setId(5L);
        testSchedule.setDoctor(testDoctor);
        testSchedule.setWorkDate(LocalDate.now().plusDays(1));

        testSlot = new Slot();
        testSlot.setId(10L);
        testSlot.setSchedule(testSchedule);
        testSlot.setStartTime(LocalTime.of(10, 0));
        testSlot.setEndTime(LocalTime.of(10, 30));
        testSlot.setStatus(SlotStatus.AVAILABLE);

        Jwt jwt = new Jwt("doctor-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), Map.of("sub", "doctor-123"));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAvailableSlots_success() {
        when(doctorRepo.existsById(2L)).thenReturn(true);
        when(slotRepo.findByStatusAndScheduleDoctorId(SlotStatus.AVAILABLE, 2L)).thenReturn(List.of(testSlot));
        List<SlotResponseDto> result = sut.getAvailableSlots(2L);
        verify(doctorRepo, times(1)).existsById(2L);
        verify(slotRepo, times(1)).findByStatusAndScheduleDoctorId(SlotStatus.AVAILABLE, 2L);
        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().id());
        assertEquals(SlotStatus.AVAILABLE, result.getFirst().status());
    }

    @Test
    void getAvailableSlots_doctorNotFound() {
        when(doctorRepo.existsById(2L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> sut.getAvailableSlots(2L));
        verify(slotRepo, never()).findByStatusAndScheduleDoctorId(SlotStatus.AVAILABLE, 2L);
    }

    @Test
    void getAvailableSlots_pastSlot() {
        testSchedule.setWorkDate(LocalDate.now().minusDays(1));
        when(doctorRepo.existsById(2L)).thenReturn(true);
        when(slotRepo.findByStatusAndScheduleDoctorId(SlotStatus.AVAILABLE, 2L)).thenReturn(List.of(testSlot));
        List<SlotResponseDto> result = sut.getAvailableSlots(2L);
        verify(slotRepo, times(1)).findByStatusAndScheduleDoctorId(SlotStatus.AVAILABLE, 2L);
        assertEquals(0, result.size());
    }

    @Test
    void deleteAvailableSlot_success() {
        when(slotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testSlot));
        when(appointmentRepo.existsBySlotId(10L)).thenReturn(false);
        sut.deleteAvailableSlot(10L);
        verify(slotRepo, times(1)).delete(testSlot);
    }

    @Test
    void deleteAvailableSlot_notFound() {
        when(slotRepo.findByIdForUpdate(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.deleteAvailableSlot(10L));
        verify(slotRepo, never()).delete(testSlot);
    }

    @Test
    void deleteAvailableSlot_alreadyBooked() {
        testSlot.setStatus(SlotStatus.BOOKED);
        when(slotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testSlot));
        assertThrows(AlreadyExistsException.class, () -> sut.deleteAvailableSlot(10L));
        verify(slotRepo, never()).delete(testSlot);
    }

    @Test
    void deleteAvailableSlot_hasAppointments() {
        when(slotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testSlot));
        when(appointmentRepo.existsBySlotId(10L)).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.deleteAvailableSlot(10L));
        verify(slotRepo, never()).delete(testSlot);
    }

    @Test
    void deleteAvailableSlot_accessDenied() {
        testDoctor.setKeycloakId("another-doctor");
        when(slotRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(testSlot));
        assertThrows(AccessDeniedException.class, () -> sut.deleteAvailableSlot(10L));
        verify(slotRepo, never()).delete(testSlot);
    }

}
