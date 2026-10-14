package kg.megalab.laboratory.service.impl;

import kg.megalab.laboratory.model.dto.request.LabRequestDto;
import kg.megalab.laboratory.model.dto.response.LabRequestResponseDto;
import kg.megalab.laboratory.model.entity.LabRequest;
import kg.megalab.laboratory.repo.LabRequestRepo;
import kg.megalab.laboratory.service.LabRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabRequestServiceImpl implements LabRequestService {

    private final LabRequestRepo labRequestRepo;

    @Override
    @Transactional
    public void saveLabRequest(LabRequestDto dto) {

        if (labRequestRepo.existsByRequestId(dto.requestId())) {
            return;
        }

        LabRequest request = new LabRequest();
        request.setRequestId(dto.requestId());
        request.setDoctorId(dto.doctorId());
        request.setPatientId(dto.patientId());
        request.setLabTestId(dto.labTestId());

        labRequestRepo.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabRequestResponseDto> getAllLabRequests() {
        return labRequestRepo.findAll().stream().map(request -> new LabRequestResponseDto(request.getId(), request.getRequestId(),
                request.getDoctorId(), request.getPatientId(), request.getLabTestId(), request.getCreatedAt())).toList();
    }

}
