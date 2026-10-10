package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreateLabSlotDto;
import kg.megalab.cmp.model.dto.response.LabSlotResponseDto;

import java.util.List;

public interface LabSlotService {

    LabSlotResponseDto createLabSlot(CreateLabSlotDto dto);

    List<LabSlotResponseDto> getAvailableLabSlots();

}