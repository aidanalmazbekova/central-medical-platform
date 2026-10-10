package kg.megalab.cmp.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateScheduleDto(
        @NotNull(message = "Дата работы обязательна")
        LocalDate workDate,

        @NotNull(message = "Время начала обязательно")
        LocalTime startTime,

        @NotNull(message = "Время окончания обязательно")
        LocalTime endTime,

        @NotNull(message = "Продолжительность слота обязательна")
        @Positive(message = "Продолжительность слота должна быть больше нуля")
        Integer slotDurationMinutes
) {
}