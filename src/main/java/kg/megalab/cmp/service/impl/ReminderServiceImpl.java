package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.model.entity.Appointment;
import kg.megalab.cmp.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    @Value("${spring.kafka.reminder-topic}")
    private String topicName;

    private final KafkaTemplate<Long, String> kafkaTemplate;

    @Override
    public void sendReminder(Appointment appointment) {

        String message = "Напоминание о приёме. Пациент: " + appointment.getPatient().getFirstName()
                + ". Телефон: " + appointment.getPatient().getPhone()
                + ". Врач: " + appointment.getSlot().getSchedule().getDoctor().getLastName()
                + ". Дата: " + appointment.getSlot().getSchedule().getWorkDate()
                + ". Время: " + appointment.getSlot().getStartTime();

        kafkaTemplate.send(topicName, appointment.getId(), message);
    }

}
