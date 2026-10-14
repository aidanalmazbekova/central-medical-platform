package kg.megalab.cmp.service.impl;

import jakarta.transaction.Transactional;
import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateAppointmentDto;
import kg.megalab.cmp.model.dto.response.AppointmentResponseDto;
import kg.megalab.cmp.model.entity.Appointment;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.model.entity.Patient;
import kg.megalab.cmp.model.entity.Slot;
import kg.megalab.cmp.model.enums.AppointmentStatus;
import kg.megalab.cmp.model.enums.SlotStatus;
import kg.megalab.cmp.repo.AppointmentRepo;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.PatientRepo;
import kg.megalab.cmp.repo.SlotRepo;
import kg.megalab.cmp.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final PatientRepo patientRepo;
    private final SlotRepo slotRepo;
    private final DoctorRepo doctorRepo;


    @Override
    @Transactional
    public AppointmentResponseDto bookAppointment(CreateAppointmentDto dto) {
        Patient patient = patientRepo.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !authentication.getToken().getSubject().equals(patient.getKeycloakId())) {
            throw new AccessDeniedException("Нельзя записывать другого пациента");
        }

        Slot slot = slotRepo.findByIdForUpdate(dto.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Слот не найден"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new AlreadyExistsException("Слот уже занят");
        }

        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSchedule().getWorkDate(), slot.getStartTime());
        if (!slotDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя записаться на прошедшее время");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.CREATED);
        slot.setStatus(SlotStatus.BOOKED);
        slotRepo.save(slot);

        return toDto(appointmentRepo.save(appointment));
    }


    @Override
    public List<AppointmentResponseDto> getAllAppointments() {
        return appointmentRepo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public AppointmentResponseDto cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepo.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись не найдена"));

        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String keycloakId = auth.getToken().getSubject();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isPatient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        boolean isDoctor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        boolean ownPatient = isPatient &&
                keycloakId.equals(appointment.getPatient().getKeycloakId());
        boolean ownDoctor = isDoctor &&
                keycloakId.equals(appointment.getSlot().getSchedule().getDoctor().getKeycloakId());

        if (!isAdmin && !ownPatient && !ownDoctor) {
            throw new AccessDeniedException("Нельзя отменять чужую запись");
        }

        if (appointment.getStatus() != AppointmentStatus.CREATED) {
            throw new AlreadyExistsException("Можно отменить только активную запись");
        }

        Slot slot = slotRepo.findByIdForUpdate(appointment.getSlot().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Слот не найден"));

        appointment.setStatus(AppointmentStatus.CANCELED);
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepo.save(slot);

        return toDto(appointmentRepo.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDto completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepo.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись не найдена"));

        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String keycloakId = auth.getToken().getSubject();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isDoctor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        boolean ownDoctor = isDoctor && keycloakId.equals(appointment.getSlot().getSchedule().getDoctor().getKeycloakId());

        if (!isAdmin && !ownDoctor) {
            throw new AccessDeniedException("Нельзя завершать чужой приём");
        }

        if (appointment.getStatus() != AppointmentStatus.CREATED) {
            throw new AlreadyExistsException("Можно завершить только активную запись");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

        return toDto(appointmentRepo.save(appointment));
    }

    @Override
    public List<AppointmentResponseDto> getAppointmentsByPatient(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !auth.getToken().getSubject().equals(patient.getKeycloakId())) {
            throw new AccessDeniedException("Нельзя просматривать чужие записи");
        }

        return appointmentRepo.findByPatientId(patientId).stream().map(this::toDto).toList();
    }

    @Override
    public List<AppointmentResponseDto> getAppointmentsByDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !auth.getToken().getSubject().equals(doctor.getKeycloakId())) {
            throw new AccessDeniedException("Нельзя просматривать чужие записи врача");
        }

        return appointmentRepo.findBySlotScheduleDoctorId(doctorId).stream().map(this::toDto).toList();
    }

    @Override
    public List<AppointmentResponseDto> getAppointmentsByFilters(AppointmentStatus status, String specialty, String doctorName, String patientName) {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String keycloakId = auth.getToken().getSubject();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isDoctor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        Long patientId = null;
        Long doctorId = null;

        if (!isAdmin) {
            if (isDoctor) {
                Doctor doctor = doctorRepo.findByKeycloakId(keycloakId)
                        .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));
                doctorId = doctor.getId();
            } else {
                Patient patient = patientRepo.findByKeycloakId(keycloakId)
                        .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));
                patientId = patient.getId();
            }
        }

        return appointmentRepo.findByFilters(status, specialty, doctorName, patientName, patientId, doctorId).stream().map(this::toDto).toList();
    }

    private AppointmentResponseDto toDto(Appointment appointment) {
        Slot slot = appointment.getSlot();

        return new AppointmentResponseDto(appointment.getId(), appointment.getPatient().getId(), slot.getSchedule().getDoctor().getId(),
                slot.getId(), slot.getSchedule().getWorkDate(), slot.getStartTime(), slot.getEndTime(), appointment.getStatus(), appointment.getCreatedAt());
    }

}
