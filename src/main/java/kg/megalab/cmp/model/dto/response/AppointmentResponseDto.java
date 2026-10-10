package kg.megalab.cmp.model.dto.response;

import kg.megalab.cmp.model.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record AppointmentResponseDto(
        Long id,
        Long patientId,
        Long doctorId,
        Long slotId,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        OffsetDateTime createdAt
) {
}