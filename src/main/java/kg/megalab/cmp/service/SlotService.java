package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.response.SlotResponseDto;

import java.util.List;

public interface SlotService {

    List<SlotResponseDto> getAvailableSlots(Long doctorId);

    void deleteAvailableSlot(Long slotId);

}
