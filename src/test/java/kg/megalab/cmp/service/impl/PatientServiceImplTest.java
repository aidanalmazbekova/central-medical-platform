package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.model.dto.request.CreatePatientDto;
import kg.megalab.cmp.model.dto.response.PatientResponseDto;
import kg.megalab.cmp.model.entity.Patient;
import kg.megalab.cmp.repo.PatientRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepo patientRepo;

    @InjectMocks
    private PatientServiceImpl sut;

    private CreatePatientDto testDto;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("Айдана");
        testPatient.setLastName("Асанова");
        testPatient.setMiddleName("Бакытовна");
        testPatient.setBirthDate(LocalDate.of(2000, 9, 17));
        testPatient.setPhone("0555123456");
        testPatient.setEmail("aidana@gmail.com");
        testPatient.setAddress("Бишкек");
        testPatient.setKeycloakId("patient-123");

        testDto = new CreatePatientDto("Айдана", "Асанова", "Бакытовна",
                LocalDate.of(2000, 9, 17), "0555123456",
                "aidana@gmail.com", "Бишкек", "patient-123");
    }

    @Test
    void createPatient_success() {
        when(patientRepo.save(any(Patient.class))).thenReturn(testPatient);
        PatientResponseDto result = sut.createPatient(testDto);
        verify(patientRepo, times(1)).save(any(Patient.class));
        assertEquals(1L, result.id());
        assertEquals("Айдана", result.firstName());
        assertEquals("Асанова", result.lastName());
        assertEquals("aidana@gmail.com", result.email());
    }

    @Test
    void createPatient_phoneAlreadyExists() {
        when(patientRepo.existsByPhone("0555123456")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createPatient(testDto));
        verify(patientRepo, never()).save(any(Patient.class));
    }

    @Test
    void createPatient_emailAlreadyExists() {
        when(patientRepo.existsByEmailIgnoreCase("aidana@gmail.com")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createPatient(testDto));
        verify(patientRepo, never()).save(any(Patient.class));
    }

    @Test
    void createPatient_keycloakIdAlreadyExists() {
        when(patientRepo.existsByKeycloakId("patient-123")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createPatient(testDto));
        verify(patientRepo, never()).save(any(Patient.class));
    }

    @Test
    void getAllPatients_success() {
        when(patientRepo.findAll()).thenReturn(List.of(testPatient));
        List<PatientResponseDto> result = sut.getAllPatients();
        verify(patientRepo, times(1)).findAll();
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals("Айдана", result.getFirst().firstName());
    }

}
