package kg.megalab.cmp.model.dto.response;

import kg.megalab.cmp.model.enums.LabSlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record LabSlotResponseDto(
        Long id,
        Long labTestId,
        String labTestName,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        LabSlotStatus status
) {
}