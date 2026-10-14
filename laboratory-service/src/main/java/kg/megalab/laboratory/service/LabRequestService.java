package kg.megalab.laboratory.service;

import kg.megalab.laboratory.model.dto.request.LabRequestDto;
import kg.megalab.laboratory.model.dto.response.LabRequestResponseDto;

import java.util.List;

public interface LabRequestService {

    void saveLabRequest(LabRequestDto dto);

    List<LabRequestResponseDto> getAllLabRequests();

}