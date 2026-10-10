package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateDoctorDto;
import kg.megalab.cmp.model.dto.response.DoctorResponseDto;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.model.entity.Specialty;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.SpecialtyRepo;
import kg.megalab.cmp.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepo doctorRepo;
    private final SpecialtyRepo specialtyRepo;

    @Override
    public DoctorResponseDto createDoctor(CreateDoctorDto dto) {
        Specialty specialty = specialtyRepo.findById(dto.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Специальность не найдена"));

        if (dto.phone() != null && doctorRepo.existsByPhone(dto.phone())) {
            throw new AlreadyExistsException("Врач с таким номером телефона уже существует");
        }
        if (dto.email() != null && doctorRepo.existsByEmailIgnoreCase(dto.email())) {
            throw new AlreadyExistsException("Врач с таким email уже существует");
        }
        if (dto.keycloakId() != null && doctorRepo.existsByKeycloakId(dto.keycloakId())) {
            throw new AlreadyExistsException("Врач с таким Keycloak ID уже существует");
        }

        Doctor doctor = new Doctor();
        doctor.setFirstName(dto.firstName().trim());
        doctor.setLastName(dto.lastName().trim());
        doctor.setMiddleName(dto.middleName());
        doctor.setBirthDate(dto.birthDate());
        doctor.setPhone(dto.phone());
        doctor.setEmail(dto.email());
        doctor.setKeycloakId(dto.keycloakId());
        doctor.setSpecialty(specialty);

        Doctor savedDoctor = doctorRepo.save(doctor);

        return new DoctorResponseDto(savedDoctor.getId(), savedDoctor.getFirstName(), savedDoctor.getLastName(), savedDoctor.getMiddleName(), savedDoctor.getSpecialty().getName());
    }

    @Override
    public List<DoctorResponseDto> getAllDoctors() {
        return doctorRepo.findAll().stream()
                .map(doctor -> new DoctorResponseDto(doctor.getId(), doctor.getFirstName(), doctor.getLastName(), doctor.getMiddleName(), doctor.getSpecialty().getName()))
                .toList();
    }

}
