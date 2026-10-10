package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.model.dto.request.CreatePatientDto;
import kg.megalab.cmp.model.dto.response.PatientResponseDto;
import kg.megalab.cmp.model.entity.Patient;
import kg.megalab.cmp.repo.PatientRepo;
import kg.megalab.cmp.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepo patientRepo;

    @Override
    public PatientResponseDto createPatient(CreatePatientDto dto) {
        if (dto.phone() != null && patientRepo.existsByPhone(dto.phone())) {
            throw new AlreadyExistsException("Пациент с таким номером телефона уже существует");
        }
        if (dto.email() != null && patientRepo.existsByEmailIgnoreCase(dto.email())) {
            throw new AlreadyExistsException("Пациент с таким email уже существует");
        }
        if (patientRepo.existsByKeycloakId(dto.keycloakId())) {
            throw new AlreadyExistsException("Пациент с таким keycloak ID уже существует");
        }

        Patient patient = new Patient();
        patient.setFirstName(dto.firstName().trim());
        patient.setLastName(dto.lastName().trim());
        patient.setMiddleName(dto.middleName());
        patient.setBirthDate(dto.birthDate());
        patient.setPhone(dto.phone());
        patient.setEmail(dto.email());
        patient.setAddress(dto.address());
        patient.setKeycloakId(dto.keycloakId());

        Patient savedPatient = patientRepo.save(patient);

        return new PatientResponseDto(savedPatient.getId(), savedPatient.getFirstName(), savedPatient.getLastName(), savedPatient.getMiddleName(), savedPatient.getEmail());
    }

    @Override
    public List<PatientResponseDto> getAllPatients() {
        return patientRepo.findAll().stream()
                .map(patient -> new PatientResponseDto(patient.getId(), patient.getFirstName(), patient.getLastName(), patient.getMiddleName(), patient.getEmail()))
                .toList();
    }

}
