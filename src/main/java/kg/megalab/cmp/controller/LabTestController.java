package kg.megalab.cmp.controller;

import jakarta.validation.Valid;
import kg.megalab.cmp.model.dto.request.CreateLabTestDto;
import kg.megalab.cmp.model.dto.response.LabTestResponseDto;
import kg.megalab.cmp.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lab-test")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    @PostMapping("/create")
    public ResponseEntity<LabTestResponseDto> createLabTest(@Valid @RequestBody CreateLabTestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labTestService.createLabTest(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<LabTestResponseDto>> getAllLabTests() {
        return ResponseEntity.ok(labTestService.getAllLabTests());
    }

}