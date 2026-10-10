package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreateAppointmentDto;
import kg.megalab.cmp.model.dto.response.AppointmentResponseDto;
import kg.megalab.cmp.model.enums.AppointmentStatus;

import java.util.List;

public interface AppointmentService {

    AppointmentResponseDto bookAppointment(CreateAppointmentDto dto);

    List<AppointmentResponseDto> getAllAppointments();

    AppointmentResponseDto cancelAppointment(Long appointmentId);

    AppointmentResponseDto completeAppointment(Long appointmentId);

    List<AppointmentResponseDto> getAppointmentsByPatient(Long patientId);

    List<AppointmentResponseDto> getAppointmentsByDoctor(Long doctorId);

    List<AppointmentResponseDto> getAppointmentsByFilters(AppointmentStatus status, String specialty, String doctorName, String patientName);

}