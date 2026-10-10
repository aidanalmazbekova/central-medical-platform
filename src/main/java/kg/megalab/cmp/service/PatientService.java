package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreatePatientDto;
import kg.megalab.cmp.model.dto.response.PatientResponseDto;

import java.util.List;

public interface PatientService {

    PatientResponseDto createPatient(CreatePatientDto dto);

    List<PatientResponseDto> getAllPatients();

}