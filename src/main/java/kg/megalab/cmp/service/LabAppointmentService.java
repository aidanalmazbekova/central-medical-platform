package kg.megalab.cmp.service;

import kg.megalab.cmp.model.dto.request.CreateLabAppointmentDto;
import kg.megalab.cmp.model.dto.response.LabAppointmentResponseDto;

import java.util.List;

public interface LabAppointmentService {

    LabAppointmentResponseDto bookLabAppointment(CreateLabAppointmentDto dto);

    LabAppointmentResponseDto cancelLabAppointment(Long id);

    List<LabAppointmentResponseDto> getPatientLabAppointments(Long patientId);

    LabAppointmentResponseDto completeLabAppointment(Long id);

}