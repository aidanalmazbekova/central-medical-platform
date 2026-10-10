package kg.megalab.cmp.service.impl;


import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateLabAppointmentDto;
import kg.megalab.cmp.model.dto.response.LabAppointmentResponseDto;
import kg.megalab.cmp.model.entity.LabAppointment;
import kg.megalab.cmp.model.entity.LabSlot;
import kg.megalab.cmp.model.entity.Patient;
import kg.megalab.cmp.model.enums.LabAppointmentStatus;
import kg.megalab.cmp.model.enums.LabSlotStatus;
import kg.megalab.cmp.repo.LabAppointmentRepo;
import kg.megalab.cmp.repo.LabSlotRepo;
import kg.megalab.cmp.repo.PatientRepo;
import kg.megalab.cmp.service.LabAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabAppointmentServiceImpl implements LabAppointmentService {

    private final LabAppointmentRepo labAppointmentRepo;
    private final LabSlotRepo labSlotRepo;
    private final PatientRepo patientRepo;

    @Override
    @Transactional
    public LabAppointmentResponseDto bookLabAppointment(CreateLabAppointmentDto dto) {
        Patient patient = patientRepo.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        checkPatientAccess(patient);

        LabSlot slot = labSlotRepo.findByIdForUpdate(dto.labSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Лабораторный слот не найден"));

        if (slot.getStatus() != LabSlotStatus.AVAILABLE) {
            throw new AlreadyExistsException("Лабораторный слот уже занят");
        }

        LocalDateTime slotDateTime = LocalDateTime.of(slot.getWorkDate(), slot.getStartTime());

        if (!slotDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя записаться на прошедшее время");
        }

        slot.setStatus(LabSlotStatus.BOOKED);

        LabAppointment appointment = new LabAppointment();
        appointment.setPatient(patient);
        appointment.setLabSlot(slot);
        appointment.setStatus(LabAppointmentStatus.CREATED);

        return toResponse(labAppointmentRepo.save(appointment));
    }

    @Override
    @Transactional
    public LabAppointmentResponseDto cancelLabAppointment(Long id) {
        LabAppointment appointment = labAppointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись на анализ не найдена"));

        checkPatientAccess(appointment.getPatient());

        LabSlot slot = labSlotRepo.findByIdForUpdate(appointment.getLabSlot().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Лабораторный слот не найден"));

        if (appointment.getStatus() != LabAppointmentStatus.CREATED) {
            throw new AlreadyExistsException("Отменить можно только активную запись");
        }

        appointment.setStatus(LabAppointmentStatus.CANCELED);
        slot.setStatus(LabSlotStatus.AVAILABLE);

        return toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabAppointmentResponseDto> getPatientLabAppointments(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        checkPatientAccess(patient);

        return labAppointmentRepo.findByPatientId(patientId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public LabAppointmentResponseDto completeLabAppointment(Long id) {
        LabAppointment appointment = labAppointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись на анализ не найдена"));

        if (appointment.getStatus() != LabAppointmentStatus.CREATED) {
            throw new AlreadyExistsException("Завершить можно только активную запись");
        }

        appointment.setStatus(LabAppointmentStatus.COMPLETED);

        return toResponse(appointment);
    }

    private LabAppointmentResponseDto toResponse(LabAppointment appointment) {
        LabSlot slot = appointment.getLabSlot();

        return new LabAppointmentResponseDto(appointment.getId(), appointment.getPatient().getId(), slot.getId(), slot.getLabTest().getName(),
                slot.getWorkDate(), slot.getStartTime(), slot.getEndTime(), appointment.getStatus());
    }

    private void checkPatientAccess(Patient patient) {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !auth.getToken().getSubject().equals(patient.getKeycloakId())) {
            throw new AccessDeniedException("Нет доступа к чужим лабораторным записям");
        }
    }

}