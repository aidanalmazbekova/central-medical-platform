package kg.megalab.cmp.service.impl;

import jakarta.transaction.Transactional;
import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.response.SlotResponseDto;
import kg.megalab.cmp.model.entity.Slot;
import kg.megalab.cmp.model.enums.SlotStatus;
import kg.megalab.cmp.repo.AppointmentRepo;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.SlotRepo;
import kg.megalab.cmp.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private final SlotRepo slotRepo;
    private final AppointmentRepo appointmentRepo;
    private final DoctorRepo doctorRepo;

    @Override
    public List<SlotResponseDto> getAvailableSlots(Long doctorId) {
        if (!doctorRepo.existsById(doctorId)) {
            throw new ResourceNotFoundException("Врач не найден");
        }

        return slotRepo.findByStatusAndScheduleDoctorId(SlotStatus.AVAILABLE, doctorId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void deleteAvailableSlot(Long slotId) {
        Slot slot = slotRepo.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Слот не найден"));

        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !auth.getToken().getSubject().equals(slot.getSchedule().getDoctor().getKeycloakId())) {
            throw new AccessDeniedException("Нельзя удалять слот другого врача");
        }

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new AlreadyExistsException("Нельзя удалить занятый слот");
        }

        if (appointmentRepo.existsBySlotId(slotId)) {
            throw new AlreadyExistsException("Нельзя удалить слот, если есть история записей");
        }

        slotRepo.delete(slot);
    }

    private SlotResponseDto toDto(Slot slot) {
        return new SlotResponseDto(slot.getId(), slot.getSchedule().getDoctor().getId(), slot.getSchedule().getId(),
                slot.getSchedule().getWorkDate(), slot.getStartTime(), slot.getEndTime(), slot.getStatus());
    }

}