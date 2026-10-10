package kg.megalab.cmp.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateDoctorDto(
        @NotBlank(message = "Имя врача обязательно")
        String firstName,

        @NotBlank(message = "Фамилия врача обязательна")
        String lastName,

        String middleName,

        LocalDate birthDate,

        @Size(max = 30, message = "Номер телефона не должен превышать 30 символов")
        String phone,

        @Email(message = "Некорректный формат email")
        @Size(max = 150, message = "Email не должен превышать 150 символов")
        String email,

        @Size(max = 100, message = "Keycloak ID не должен превышать 100 символов")
        String keycloakId,

        @NotNull(message = "Специальность врача обязательна")
        Long specialtyId
) {
}