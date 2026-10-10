package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreateScheduleDto;
import kg.megalab.cmp.model.dto.request.UpdateScheduleDto;
import kg.megalab.cmp.model.dto.response.ScheduleResponseDto;

import java.util.List;

public interface ScheduleService {

    ScheduleResponseDto createSchedule(CreateScheduleDto dto);

    List<ScheduleResponseDto> getAllSchedules();

    ScheduleResponseDto updateSchedule(Long id, UpdateScheduleDto dto);

}