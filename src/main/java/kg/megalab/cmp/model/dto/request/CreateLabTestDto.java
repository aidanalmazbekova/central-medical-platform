package kg.megalab.cmp.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLabTestDto(
        @NotBlank(message = "Название анализа обязательно")
        @Size(max = 100, message = "Название не должно превышать 100 символов")
        String name,

        String description
) {
}