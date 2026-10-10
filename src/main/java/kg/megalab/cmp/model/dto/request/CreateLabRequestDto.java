package kg.megalab.cmp.model.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateLabRequestDto(
        @NotNull Long doctorId,
        @NotNull Long patientId,
        @NotNull Long labTestId
) {
}