package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateLabSlotDto;
import kg.megalab.cmp.model.dto.response.LabSlotResponseDto;
import kg.megalab.cmp.model.entity.LabSlot;
import kg.megalab.cmp.model.entity.LabTest;
import kg.megalab.cmp.model.enums.LabSlotStatus;
import kg.megalab.cmp.repo.LabSlotRepo;
import kg.megalab.cmp.repo.LabTestRepo;
import kg.megalab.cmp.service.LabSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabSlotServiceImpl implements LabSlotService {

    private final LabSlotRepo labSlotRepo;
    private final LabTestRepo labTestRepo;

    @Override
    public LabSlotResponseDto createLabSlot(CreateLabSlotDto dto) {
        LabTest labTest = labTestRepo.findById(dto.labTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Анализ не найден"));

        if (dto.workDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Нельзя создать слот на прошедшую дату");
        }

        if (!dto.endTime().isAfter(dto.startTime())) {
            throw new IllegalArgumentException("Время окончания должно быть позже начала");
        }

        if (LocalDateTime.of(dto.workDate(), dto.startTime()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя создать слот на прошедшее время");
        }

        if (labSlotRepo.existsOverlappingSlot(dto.labTestId(), dto.workDate(),
                dto.startTime(), dto.endTime())) {
            throw new AlreadyExistsException("На это время уже существует слот");
        }

        LabSlot labSlot = new LabSlot();
        labSlot.setLabTest(labTest);
        labSlot.setWorkDate(dto.workDate());
        labSlot.setStartTime(dto.startTime());
        labSlot.setEndTime(dto.endTime());
        labSlot.setStatus(LabSlotStatus.AVAILABLE);

        return toResponse(labSlotRepo.save(labSlot));
    }

    @Override
    public List<LabSlotResponseDto> getAvailableLabSlots() {
        List<LabSlot> slots = labSlotRepo.findByStatus(LabSlotStatus.AVAILABLE);
        List<LabSlotResponseDto> responses = new ArrayList<>();
        for (LabSlot slot : slots) {
            responses.add(toResponse(slot));
        }
        return responses;
    }

    private LabSlotResponseDto toResponse(LabSlot slot) {
        return new LabSlotResponseDto(slot.getId(), slot.getLabTest().getId(), slot.getLabTest().getName(), slot.getWorkDate(), slot.getStartTime(), slot.getEndTime(), slot.getStatus());
    }

}
