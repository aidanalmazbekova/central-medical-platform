package kg.megalab.cmp.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSpecialtyDto(

        @NotBlank(message = "Название специальности обязательно")
        @Size(max = 100, message = "Название не должно превышать 100 символов")
        String name,

        @Size(max = 500, message = "Описание не должно превышать 500 символов")
        String description
) {
}
