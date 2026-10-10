package kg.megalab.cmp.scheduler;

import kg.megalab.cmp.model.entity.Appointment;
import kg.megalab.cmp.model.enums.AppointmentStatus;
import kg.megalab.cmp.repo.AppointmentRepo;
import kg.megalab.cmp.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final AppointmentRepo appointmentRepo;
    private final ReminderService reminderService;

    @Scheduled(fixedDelay = 60000)
    public void checkAppointments() {

        LocalDateTime now = LocalDateTime.now();

        List<Appointment> appointments = appointmentRepo.findAppointmentsForReminder(AppointmentStatus.CREATED, now.toLocalDate(), now.plusHours(5).toLocalDate());
        for (Appointment appointment : appointments) {
            LocalDateTime appointmentTime = LocalDateTime.of(
                    appointment.getSlot().getSchedule().getWorkDate(),
                    appointment.getSlot().getStartTime()
            );

            LocalDateTime reminderTime = appointmentTime.minusHours(5);

            if (!now.isBefore(reminderTime) && now.isBefore(appointmentTime)) {
                reminderService.sendReminder(appointment);
                appointment.setReminderSent(true);
                appointmentRepo.save(appointment);

                log.info("Reminder queued for appointment ID: {}", appointment.getId());
            }
        }
    }

}