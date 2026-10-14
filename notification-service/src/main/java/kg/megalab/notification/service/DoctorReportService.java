package kg.megalab.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoctorReportService {

    @KafkaListener(topics = "${spring.kafka.report-topic}")
    public void receiveReport(String report) {

        log.info("Получен годовой отчет врачей:\n{}", report);
    }

}