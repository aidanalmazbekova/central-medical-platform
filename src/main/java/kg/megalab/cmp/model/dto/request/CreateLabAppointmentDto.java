package kg.megalab.cmp.model.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateLabAppointmentDto(
        @NotNull(message = "Укажите ID пациента")
        Long patientId,

        @NotNull(message = "Укажите ID лабораторного слота")
        Long labSlotId
) {
}