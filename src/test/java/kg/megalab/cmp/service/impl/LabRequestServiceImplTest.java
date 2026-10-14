package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.model.dto.request.CreateLabRequestDto;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.LabTestRepo;
import kg.megalab.cmp.repo.PatientRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabRequestServiceImplTest {

    @Mock
    private DoctorRepo doctorRepo;

    @Mock
    private PatientRepo patientRepo;

    @Mock
    private LabTestRepo labTestRepo;

    @Mock
    private KafkaTemplate<Long, String> kafkaTemplate;

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private LabRequestServiceImpl sut;

    private Doctor testDoctor;
    private CreateLabRequestDto testDto;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setKeycloakId("doctor-123");
        testDto = new CreateLabRequestDto(1L, 2L, 3L);
        ReflectionTestUtils.setField(sut, "topic", "lab-requests");
        Jwt jwt = new Jwt("doctor-token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), Map.of("sub", "doctor-123"));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendLabRequest_success() {
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(patientRepo.existsById(2L)).thenReturn(true);
        when(labTestRepo.existsById(3L)).thenReturn(true);
        when(jsonMapper.writeValueAsString(any())).thenReturn("test-message");
        sut.sendLabRequest(testDto);
        verify(doctorRepo, times(1)).findById(1L);
        verify(patientRepo, times(1)).existsById(2L);
        verify(labTestRepo, times(1)).existsById(3L);
        verify(kafkaTemplate, times(1)).send(eq("lab-requests"), eq(2L), eq("test-message"));
    }

    @Test
    void sendLabRequest_doctorNotFound() {
        when(doctorRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> sut.sendLabRequest(testDto));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void sendLabRequest_accessDenied() {
        testDoctor.setKeycloakId("another-doctor");
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(testDoctor));
        assertThrows(AccessDeniedException.class, () -> sut.sendLabRequest(testDto));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void sendLabRequest_patientNotFound() {
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(patientRepo.existsById(2L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> sut.sendLabRequest(testDto));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void sendLabRequest_labTestNotFound() {
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(patientRepo.existsById(2L)).thenReturn(true);
        when(labTestRepo.existsById(3L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> sut.sendLabRequest(testDto));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

}
