package kg.megalab.cmp.model.dto.response;

import kg.megalab.cmp.model.enums.LabAppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record LabAppointmentResponseDto(
        Long id,
        Long patientId,
        Long labSlotId,
        String labTestName,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        LabAppointmentStatus status
) {
}