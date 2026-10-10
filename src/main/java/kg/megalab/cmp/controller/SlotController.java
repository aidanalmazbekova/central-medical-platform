package kg.megalab.cmp.controller;

import kg.megalab.cmp.model.dto.response.SlotResponseDto;
import kg.megalab.cmp.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/slot")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/available")
    public ResponseEntity<List<SlotResponseDto>> getAvailableSlots(@RequestParam Long doctorId) {
        return ResponseEntity.ok(slotService.getAvailableSlots(doctorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailableSlot(@PathVariable Long id) {
        slotService.deleteAvailableSlot(id);
        return ResponseEntity.noContent().build();
    }

}
