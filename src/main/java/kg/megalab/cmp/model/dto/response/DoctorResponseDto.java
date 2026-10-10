package kg.megalab.cmp.model.dto.response;

public record DoctorResponseDto(
        Long id,
        String firstName,
        String lastName,
        String middleName,
        String specialtyName
) {
}