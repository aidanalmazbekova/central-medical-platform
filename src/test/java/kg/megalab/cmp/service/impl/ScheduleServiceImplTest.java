package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateScheduleDto;
import kg.megalab.cmp.model.dto.request.UpdateScheduleDto;
import kg.megalab.cmp.model.dto.response.ScheduleResponseDto;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.model.entity.Schedule;
import kg.megalab.cmp.model.entity.Slot;
import kg.megalab.cmp.repo.AppointmentRepo;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.ScheduleRepo;
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
class ScheduleServiceImplTest {

    @Mock
    private ScheduleRepo scheduleRepo;

    @Mock
    private DoctorRepo doctorRepo;

    @Mock
    private SlotRepo slotRepo;

    @Mock
    private AppointmentRepo appointmentRepo;

    @InjectMocks
    private ScheduleServiceImpl sut;

    private Doctor testDoctor;
    private Schedule testSchedule;
    private Slot testSlot;
    private CreateScheduleDto createDto;
    private UpdateScheduleDto updateDto;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor();
        testDoctor.setId(2L);
        testDoctor.setKeycloakId("doctor-123");

        testSchedule = new Schedule();
        testSchedule.setId(5L);
        testSchedule.setDoctor(testDoctor);
        testSchedule.setWorkDate(LocalDate.now().plusDays(1));
        testSchedule.setStartTime(LocalTime.of(9, 0));
        testSchedule.setEndTime(LocalTime.of(10, 0));
        testSchedule.setSlotDurationMinutes(30);

        testSlot = new Slot();
        testSlot.setId(10L);
        testSlot.setSchedule(testSchedule);

        createDto = new CreateScheduleDto(2L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), 30);
        updateDto = new UpdateScheduleDto(LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(11, 0), 30);
        Jwt jwt = new Jwt("doctor-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), Map.of("sub", "doctor-123"));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSchedule_success() {
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(testDoctor));
        when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
        ScheduleResponseDto result = sut.createSchedule(createDto);
        verify(doctorRepo, times(1)).findById(2L);
        verify(scheduleRepo, times(1)).save(any(Schedule.class));
        verify(slotRepo, times(2)).save(any(Slot.class));
        assertEquals(5L, result.id());
        assertEquals(2L, result.doctorId());
    }

    @Test
    void createSchedule_doctorNotFound() {
        when(doctorRepo.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.createSchedule(createDto));
        verify(scheduleRepo, never()).save(any(Schedule.class));
    }

    @Test
    void createSchedule_accessDenied() {
        testDoctor.setKeycloakId("another-doctor");
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(testDoctor));
        assertThrows(AccessDeniedException.class, () -> sut.createSchedule(createDto));
        verify(scheduleRepo, never()).save(any(Schedule.class));
    }

    @Test
    void createSchedule_overlappingSchedule() {
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(testDoctor));
        when(scheduleRepo.existsOverlappingSchedule(2L, createDto.workDate(), createDto.startTime(), createDto.endTime(), null)).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createSchedule(createDto));
        verify(scheduleRepo, never()).save(any(Schedule.class));
    }

    @Test
    void createSchedule_invalidTime() {
        createDto = new CreateScheduleDto(2L, LocalDate.now().plusDays(1), LocalTime.of(11, 0), LocalTime.of(10, 0), 30);
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(testDoctor));
        assertThrows(IllegalArgumentException.class, () -> sut.createSchedule(createDto));
        verify(scheduleRepo, never()).save(any(Schedule.class));
    }

    @Test
    void getAllSchedules_success() {
        when(scheduleRepo.findAll()).thenReturn(List.of(testSchedule));
        List<ScheduleResponseDto> result = sut.getAllSchedules();
        verify(scheduleRepo, times(1)).findAll();
        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().id());
    }


    @Test
    void updateSchedule_success() {
        when(scheduleRepo.findById(5L)).thenReturn(Optional.of(testSchedule));
        when(slotRepo.findByScheduleId(5L)).thenReturn(List.of(testSlot));
        when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
        ScheduleResponseDto result = sut.updateSchedule(5L, updateDto);
        verify(scheduleRepo, times(1)).findById(5L);
        verify(slotRepo, times(1)).deleteByScheduleId(5L);
        verify(slotRepo, times(1)).flush();
        verify(scheduleRepo, times(1)).save(any(Schedule.class));
        verify(slotRepo, times(2)).save(any(Slot.class));
        assertEquals(5L, result.id());
        assertEquals(LocalTime.of(10, 0), result.startTime());
    }


    @Test
    void updateSchedule_notFound() {
        when(scheduleRepo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.updateSchedule(5L, updateDto));
        verify(scheduleRepo, never()).save(any(Schedule.class));
    }

    @Test
    void updateSchedule_hasAppointments() {
        when(scheduleRepo.findById(5L)).thenReturn(Optional.of(testSchedule));
        when(slotRepo.findByScheduleId(5L)).thenReturn(List.of(testSlot));
        when(appointmentRepo.existsBySlotId(10L)).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.updateSchedule(5L, updateDto));
        verify(slotRepo, never()).deleteByScheduleId(5L);
        verify(scheduleRepo, never()).save(any(Schedule.class));
    }

}
