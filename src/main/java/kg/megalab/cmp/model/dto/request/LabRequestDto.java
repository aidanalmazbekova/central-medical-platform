package kg.megalab.cmp.model.dto.request;

import java.util.UUID;

public record LabRequestDto(
        UUID requestId,
        Long doctorId,
        Long patientId,
        Long labTestId
) {
}