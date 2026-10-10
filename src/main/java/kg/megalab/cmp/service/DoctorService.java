package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreateDoctorDto;
import kg.megalab.cmp.model.dto.response.DoctorResponseDto;

import java.util.List;

public interface DoctorService {

    DoctorResponseDto createDoctor(CreateDoctorDto dto);

    List<DoctorResponseDto> getAllDoctors();

}