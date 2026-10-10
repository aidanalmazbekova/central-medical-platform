package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.model.dto.request.CreateLabRequestDto;
import kg.megalab.cmp.model.dto.request.LabRequestDto;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.repo.DoctorRepo;
import kg.megalab.cmp.repo.LabTestRepo;
import kg.megalab.cmp.repo.PatientRepo;
import kg.megalab.cmp.service.LabRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LabRequestServiceImpl implements LabRequestService {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;
    private final LabTestRepo labTestRepo;
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @Value("${spring.kafka.lab-request-topic}")
    private String topic;

    @Override
    public void sendLabRequest(CreateLabRequestDto dto) {

        Doctor doctor = doctorRepo.findById(dto.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Врач не найден"));

        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !auth.getToken().getSubject().equals(doctor.getKeycloakId())) {
            throw new AccessDeniedException("Нельзя отправлять направление от имени другого врача");
        }

        if (!patientRepo.existsById(dto.patientId())) {
            throw new IllegalArgumentException("Пациент не найден");
        }

        if (!labTestRepo.existsById(dto.labTestId())) {
            throw new IllegalArgumentException("Анализ не найден");
        }

        LabRequestDto request = new LabRequestDto(UUID.randomUUID(), dto.doctorId(), dto.patientId(), dto.labTestId());
        String message = jsonMapper.writeValueAsString(request);
        kafkaTemplate.send(topic, dto.patientId(), message);
    }

}