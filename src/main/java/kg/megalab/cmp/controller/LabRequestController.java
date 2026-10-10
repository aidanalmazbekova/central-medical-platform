package kg.megalab.cmp.controller;

import jakarta.validation.Valid;
import kg.megalab.cmp.model.dto.request.CreateLabRequestDto;
import kg.megalab.cmp.service.LabRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lab-request")
@RequiredArgsConstructor
public class LabRequestController {

    private final LabRequestService labRequestService;

    @PostMapping("/send")
    public ResponseEntity<String> sendLabRequest(@Valid @RequestBody CreateLabRequestDto dto) {
        labRequestService.sendLabRequest(dto);
        return ResponseEntity.accepted().body("Направление принято к отправке");
    }

}