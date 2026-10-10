package kg.megalab.cmp.model.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateLabSlotDto(
        @NotNull(message = "Укажите ID анализа")
        Long labTestId,

        @NotNull(message = "Укажите дату")
        LocalDate workDate,

        @NotNull(message = "Укажите время начала")
        LocalTime startTime,

        @NotNull(message = "Укажите время окончания")
        LocalTime endTime
) {
}