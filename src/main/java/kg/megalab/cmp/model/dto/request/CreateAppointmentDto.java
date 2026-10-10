package kg.megalab.cmp.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAppointmentDto(
        @NotNull @Positive Long patientId,
        @NotNull @Positive Long slotId
) {
}
