package kg.megalab.cmp.model.dto.response;

public record PatientResponseDto(
        Long id,
        String firstName,
        String lastName,
        String middleName,
        String email
) {
}