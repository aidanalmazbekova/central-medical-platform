package kg.megalab.laboratory.model.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LabRequestResponseDto(
        Long id,
        UUID requestId,
        Long doctorId,
        Long patientId,
        Long labTestId,
        OffsetDateTime createdAt
) {
}