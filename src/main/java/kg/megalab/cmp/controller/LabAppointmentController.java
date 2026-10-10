package kg.megalab.cmp.controller;

import jakarta.validation.Valid;
import kg.megalab.cmp.model.dto.request.CreateLabAppointmentDto;
import kg.megalab.cmp.model.dto.response.LabAppointmentResponseDto;
import kg.megalab.cmp.service.LabAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lab-appointment")
@RequiredArgsConstructor
public class LabAppointmentController {

    private final LabAppointmentService labAppointmentService;

    @PostMapping("/book")
    public ResponseEntity<LabAppointmentResponseDto> bookLabAppointment(@Valid @RequestBody CreateLabAppointmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labAppointmentService.bookLabAppointment(dto));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LabAppointmentResponseDto> cancelLabAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(labAppointmentService.cancelLabAppointment(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<LabAppointmentResponseDto>> getPatientLabAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(labAppointmentService.getPatientLabAppointments(patientId));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<LabAppointmentResponseDto> completeLabAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(labAppointmentService.completeLabAppointment(id));
    }

}