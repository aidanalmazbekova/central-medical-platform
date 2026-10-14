package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.model.entity.Appointment;
import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.model.entity.Patient;
import kg.megalab.cmp.model.entity.Schedule;
import kg.megalab.cmp.model.entity.Slot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock
    private KafkaTemplate<Long, String> kafkaTemplate;

    @InjectMocks
    private ReminderServiceImpl sut;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        Patient testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("Айдана");
        testPatient.setPhone("0555123456");

        Doctor testDoctor = new Doctor();
        testDoctor.setId(2L);
        testDoctor.setLastName("Асанов");

        Schedule testSchedule = new Schedule();
        testSchedule.setDoctor(testDoctor);
        testSchedule.setWorkDate(LocalDate.of(2026, 10, 20));

        Slot testSlot = new Slot();
        testSlot.setSchedule(testSchedule);
        testSlot.setStartTime(LocalTime.of(9, 0));

        testAppointment = new Appointment();
        testAppointment.setId(5L);
        testAppointment.setPatient(testPatient);
        testAppointment.setSlot(testSlot);

        ReflectionTestUtils.setField(sut, "topicName", "reminders");
    }

    @Test
    void sendReminder_success() {
        when(kafkaTemplate.send(eq("reminders"), eq(5L), anyString())).thenReturn(CompletableFuture.completedFuture(null));
        sut.sendReminder(testAppointment);
        String message = "Напоминание о приёме. Пациент: Айдана" + ". Телефон: 0555123456" + ". Врач: Асанов" + ". Дата: 2026-10-20" + ". Время: 09:00";
        verify(kafkaTemplate, times(1)).send("reminders", 5L, message);
    }

    @Test
    void sendReminder_kafkaError() {
        CompletableFuture<SendResult<Long, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka недоступна"));
        when(kafkaTemplate.send(eq("reminders"), eq(5L), anyString())).thenReturn(future);
        assertThrows(RuntimeException.class, () -> sut.sendReminder(testAppointment));
        verify(kafkaTemplate, times(1)).send(eq("reminders"), eq(5L), anyString());
    }

}
