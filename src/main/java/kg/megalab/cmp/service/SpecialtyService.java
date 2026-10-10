package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreateSpecialtyDto;
import kg.megalab.cmp.model.dto.response.SpecialtyResponseDto;

import java.util.List;

public interface SpecialtyService {

    SpecialtyResponseDto createSpecialty(CreateSpecialtyDto dto);

    List<SpecialtyResponseDto> getAllSpecialties();

}
