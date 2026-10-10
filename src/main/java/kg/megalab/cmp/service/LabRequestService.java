package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreateLabRequestDto;

public interface LabRequestService {

    void sendLabRequest(CreateLabRequestDto dto);

}