package kg.megalab.cmp.model.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleResponseDto(
        Long id,
        Long doctorId,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer slotDurationMinutes
) {
}