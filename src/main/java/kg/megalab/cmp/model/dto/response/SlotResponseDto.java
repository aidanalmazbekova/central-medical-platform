package kg.megalab.cmp.model.dto.response;

import kg.megalab.cmp.model.enums.SlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record SlotResponseDto(
        Long id,
        Long doctorId,
        Long scheduleId,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        SlotStatus status
) {
}