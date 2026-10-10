package kg.megalab.cmp.controller;

import jakarta.validation.Valid;
import kg.megalab.cmp.model.dto.request.CreateSpecialtyDto;
import kg.megalab.cmp.model.dto.response.SpecialtyResponseDto;
import kg.megalab.cmp.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/specialty")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @PostMapping("/create")
    public ResponseEntity<SpecialtyResponseDto> createSpecialty(@Valid @RequestBody CreateSpecialtyDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialtyService.createSpecialty(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SpecialtyResponseDto>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

}
