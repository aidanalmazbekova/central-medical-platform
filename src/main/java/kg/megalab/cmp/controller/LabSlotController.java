package kg.megalab.cmp.controller;

import jakarta.validation.Valid;
import kg.megalab.cmp.model.dto.request.CreateLabSlotDto;
import kg.megalab.cmp.model.dto.response.LabSlotResponseDto;
import kg.megalab.cmp.service.LabSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lab-slot")
@RequiredArgsConstructor
public class LabSlotController {

    private final LabSlotService labSlotService;

    @PostMapping("/create")
    public ResponseEntity<LabSlotResponseDto> createLabSlot(@Valid @RequestBody CreateLabSlotDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labSlotService.createLabSlot(dto));
    }

    @GetMapping("/available")
    public ResponseEntity<List<LabSlotResponseDto>> getAvailableLabSlots() {
        return ResponseEntity.ok(labSlotService.getAvailableLabSlots());
    }

}