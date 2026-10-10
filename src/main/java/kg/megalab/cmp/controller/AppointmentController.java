package kg.megalab.cmp.controller;

import jakarta.validation.Valid;
import kg.megalab.cmp.model.dto.request.CreateAppointmentDto;
import kg.megalab.cmp.model.dto.response.AppointmentResponseDto;
import kg.megalab.cmp.model.enums.AppointmentStatus;
import kg.megalab.cmp.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<AppointmentResponseDto> bookAppointment(@Valid @RequestBody CreateAppointmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<AppointmentResponseDto>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDto> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByFilters(@RequestParam(required = false) AppointmentStatus status,
                                                                                 @RequestParam(required = false) String specialty,
                                                                                 @RequestParam(required = false) String doctorName,
                                                                                 @RequestParam(required = false) String patientName) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByFilters(status, specialty, doctorName, patientName));
    }

}