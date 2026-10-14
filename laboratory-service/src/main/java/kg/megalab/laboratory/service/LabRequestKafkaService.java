package kg.megalab.laboratory.service;

import kg.megalab.laboratory.model.dto.request.LabRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabRequestKafkaService {

    private final LabRequestService labRequestService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = "${spring.kafka.lab-request-topic}")
    public void processLabRequest(String message) {
        LabRequestDto dto = jsonMapper.readValue(message, LabRequestDto.class);
        labRequestService.saveLabRequest(dto);
        log.info("Получено направление: врач {}, пациент {}, анализ {}", dto.doctorId(), dto.patientId(), dto.labTestId());
    }

}