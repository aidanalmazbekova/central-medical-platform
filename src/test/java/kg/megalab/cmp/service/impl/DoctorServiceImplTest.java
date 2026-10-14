package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateDoctorDto;
import kg.megalab.cmp.model.dto.response.DoctorResponseDto;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.model.entity.Specialty;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.SpecialtyRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepo doctorRepo;

    @Mock
    private SpecialtyRepo specialtyRepo;

    @InjectMocks
    private DoctorServiceImpl sut;

    private CreateDoctorDto testDto;
    private Doctor testDoctor;
    private Specialty testSpecialty;

    @BeforeEach
    void setUp() {
        testSpecialty = new Specialty();
        testSpecialty.setId(1L);
        testSpecialty.setName("Кардиология");

        testDoctor = new Doctor();
        testDoctor.setId(2L);
        testDoctor.setFirstName("Айбек");
        testDoctor.setLastName("Асанов");
        testDoctor.setMiddleName("Бакытович");
        testDoctor.setBirthDate(LocalDate.of(1990, 5, 15));
        testDoctor.setPhone("0555123456");
        testDoctor.setEmail("doctor@gmail.com");
        testDoctor.setKeycloakId("doctor-123");
        testDoctor.setSpecialty(testSpecialty);

        testDto = new CreateDoctorDto("Айбек", "Асанов", "Бакытович",
                LocalDate.of(1990, 5, 15), "0555123456",
                "doctor@gmail.com", "doctor-123", 1L);
    }

    @Test
    void createDoctor_success() {
        when(specialtyRepo.findById(1L)).thenReturn(Optional.of(testSpecialty));
        when(doctorRepo.save(any(Doctor.class))).thenReturn(testDoctor);
        DoctorResponseDto result = sut.createDoctor(testDto);
        verify(specialtyRepo, times(1)).findById(1L);
        verify(doctorRepo, times(1)).save(any(Doctor.class));
        assertEquals(2L, result.id());
        assertEquals("Айбек", result.firstName());
        assertEquals("Асанов", result.lastName());
        assertEquals("Кардиология", result.specialtyName());
    }

    @Test
    void createDoctor_specialtyNotFound() {
        when(specialtyRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.createDoctor(testDto));
        verify(doctorRepo, never()).save(any(Doctor.class));
    }

    @Test
    void createDoctor_phoneAlreadyExists() {
        when(specialtyRepo.findById(1L)).thenReturn(Optional.of(testSpecialty));
        when(doctorRepo.existsByPhone("0555123456")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createDoctor(testDto));
        verify(doctorRepo, never()).save(any(Doctor.class));
    }

    @Test
    void createDoctor_emailAlreadyExists() {
        when(specialtyRepo.findById(1L)).thenReturn(Optional.of(testSpecialty));
        when(doctorRepo.existsByEmailIgnoreCase("doctor@gmail.com")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createDoctor(testDto));
        verify(doctorRepo, never()).save(any(Doctor.class));
    }

    @Test
    void createDoctor_keycloakIdAlreadyExists() {
        when(specialtyRepo.findById(1L)).thenReturn(Optional.of(testSpecialty));
        when(doctorRepo.existsByKeycloakId("doctor-123")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createDoctor(testDto));
        verify(doctorRepo, never()).save(any(Doctor.class));
    }

    @Test
    void getAllDoctors_success() {
        when(doctorRepo.findAll()).thenReturn(List.of(testDoctor));
        List<DoctorResponseDto> result = sut.getAllDoctors();
        verify(doctorRepo, times(1)).findAll();
        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().id());
        assertEquals("Кардиология", result.getFirst().specialtyName());
    }

}
