package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreateLabTestDto;
import kg.megalab.cmp.model.dto.response.LabTestResponseDto;

import java.util.List;

public interface LabTestService {

    LabTestResponseDto createLabTest(CreateLabTestDto dto);

    List<LabTestResponseDto> getAllLabTests();

}