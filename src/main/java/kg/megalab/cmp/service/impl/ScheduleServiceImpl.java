package kg.megalab.cmp.service.impl;

import jakarta.transaction.Transactional;
import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateScheduleDto;
import kg.megalab.cmp.model.dto.request.UpdateScheduleDto;
import kg.megalab.cmp.model.dto.response.ScheduleResponseDto;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.model.entity.Schedule;
import kg.megalab.cmp.model.entity.Slot;
import kg.megalab.cmp.model.enums.SlotStatus;
import kg.megalab.cmp.repo.AppointmentRepo;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.ScheduleRepo;
import kg.megalab.cmp.repo.SlotRepo;
import kg.megalab.cmp.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepo scheduleRepo;
    private final DoctorRepo doctorRepo;
    private final SlotRepo slotRepo;
    private final AppointmentRepo appointmentRepo;

    @Override
    @Transactional
    public ScheduleResponseDto createSchedule(CreateScheduleDto dto) {
        Doctor doctor = doctorRepo.findById(dto.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        checkDoctorAccess(doctor);

        validateTime(dto.startTime(), dto.endTime(), dto.slotDurationMinutes());

        if (scheduleRepo.existsOverlappingSchedule(dto.doctorId(), dto.workDate(), dto.startTime(), dto.endTime(), null)) {
            throw new AlreadyExistsException("У врача уже есть график на это время");
        }

        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);
        schedule.setWorkDate(dto.workDate());
        schedule.setStartTime(dto.startTime());
        schedule.setEndTime(dto.endTime());
        schedule.setSlotDurationMinutes(dto.slotDurationMinutes());

        Schedule savedSchedule = scheduleRepo.save(schedule);
        createSlots(savedSchedule);

        return toDto(savedSchedule);
    }

    @Override
    public List<ScheduleResponseDto> getAllSchedules() {
        return scheduleRepo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public ScheduleResponseDto updateSchedule(Long id, UpdateScheduleDto dto) {
        Schedule schedule = scheduleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Расписание не найдено"));

        checkDoctorAccess(schedule.getDoctor());
        validateTime(dto.startTime(), dto.endTime(), dto.slotDurationMinutes());

        if (scheduleRepo.existsOverlappingSchedule(schedule.getDoctor().getId(), dto.workDate(), dto.startTime(), dto.endTime(), id)) {
            throw new AlreadyExistsException("У врача уже есть график на это время");
        }

        List<Slot> slots = slotRepo.findByScheduleId(id);

        for (Slot slot : slots) {
            if (appointmentRepo.existsBySlotId(slot.getId())) {
                throw new AlreadyExistsException("Нельзя изменить расписание, если есть история записей");
            }
        }

        slotRepo.deleteByScheduleId(id);
        slotRepo.flush();

        schedule.setWorkDate(dto.workDate());
        schedule.setStartTime(dto.startTime());
        schedule.setEndTime(dto.endTime());
        schedule.setSlotDurationMinutes(dto.slotDurationMinutes());

        Schedule savedSchedule = scheduleRepo.save(schedule);
        createSlots(savedSchedule);

        return toDto(savedSchedule);
    }

    private void validateTime(LocalTime startTime, LocalTime endTime, Integer duration) {
        if (startTime == null || endTime == null || duration == null) {
            throw new IllegalArgumentException("Заполните время и продолжительность слота");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Время окончания должно быть позже начала");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Продолжительность слота должна быть больше нуля");
        }
        if (Duration.between(startTime, endTime).toMinutes() < duration) {
            throw new IllegalArgumentException("Продолжительность слота больше рабочего времени");
        }
    }

    private void createSlots(Schedule schedule) {
        LocalTime currentTime = schedule.getStartTime();

        while (!currentTime.plusMinutes(schedule.getSlotDurationMinutes()).isAfter(schedule.getEndTime())) {
            Slot slot = new Slot();
            slot.setSchedule(schedule);
            slot.setStartTime(currentTime);
            slot.setEndTime(currentTime.plusMinutes(schedule.getSlotDurationMinutes()));
            slot.setStatus(SlotStatus.AVAILABLE);

            slotRepo.save(slot);
            currentTime = currentTime.plusMinutes(schedule.getSlotDurationMinutes());
        }
    }

    private ScheduleResponseDto toDto(Schedule schedule) {
        return new ScheduleResponseDto(schedule.getId(), schedule.getDoctor().getId(), schedule.getWorkDate(), schedule.getStartTime(), schedule.getEndTime(), schedule.getSlotDurationMinutes());
    }

    private void checkDoctorAccess(Doctor doctor) {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !auth.getToken().getSubject().equals(doctor.getKeycloakId())) {
            throw new AccessDeniedException("Нельзя изменять расписание другого врача");
        }
    }

}