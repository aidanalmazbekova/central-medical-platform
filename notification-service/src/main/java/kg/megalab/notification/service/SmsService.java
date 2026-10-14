package kg.megalab.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @KafkaListener(topics = "${spring.kafka.reminder-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void processSmsReminder(String msg) {
        System.out.println("Получено SMS-уведомление: " + msg);
    }

}