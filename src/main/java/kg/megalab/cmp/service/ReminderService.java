package kg.megalab.cmp.service;

import kg.megalab.cmp.model.entity.Appointment;

public interface ReminderService {

    void sendReminder(Appointment appointment);

}