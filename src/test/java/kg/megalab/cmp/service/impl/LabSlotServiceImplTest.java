package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.exception.exceptionType.ResourceNotFoundException;
import kg.megalab.cmp.model.dto.request.CreateLabSlotDto;
import kg.megalab.cmp.model.dto.response.LabSlotResponseDto;
import kg.megalab.cmp.model.entity.LabSlot;
import kg.megalab.cmp.model.entity.LabTest;
import kg.megalab.cmp.model.enums.LabSlotStatus;
import kg.megalab.cmp.repo.LabSlotRepo;
import kg.megalab.cmp.repo.LabTestRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabSlotServiceImplTest {

    @Mock
    private LabSlotRepo labSlotRepo;

    @Mock
    private LabTestRepo labTestRepo;

    @InjectMocks
    private LabSlotServiceImpl sut;

    private CreateLabSlotDto testDto;
    private LabTest testLabTest;
    private LabSlot testLabSlot;

    @BeforeEach
    void setUp() {
        testLabTest = new LabTest();
        testLabTest.setId(1L);
        testLabTest.setName("Общий анализ крови");

        testLabSlot = new LabSlot();
        testLabSlot.setId(10L);
        testLabSlot.setLabTest(testLabTest);
        testLabSlot.setWorkDate(LocalDate.now().plusDays(1));
        testLabSlot.setStartTime(LocalTime.of(9, 0));
        testLabSlot.setEndTime(LocalTime.of(9, 30));
        testLabSlot.setStatus(LabSlotStatus.AVAILABLE);

        testDto = new CreateLabSlotDto(1L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30));
    }

    @Test
    void createLabSlot_success() {
        when(labTestRepo.findById(1L)).thenReturn(Optional.of(testLabTest));
        when(labSlotRepo.save(any(LabSlot.class))).thenReturn(testLabSlot);
        LabSlotResponseDto result = sut.createLabSlot(testDto);
        verify(labTestRepo, times(1)).findById(1L);
        verify(labSlotRepo, times(1)).save(any(LabSlot.class));
        assertEquals(10L, result.id());
        assertEquals(1L, result.labTestId());
        assertEquals(LabSlotStatus.AVAILABLE, result.status());
    }

    @Test
    void createLabSlot_labTestNotFound() {
        when(labTestRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sut.createLabSlot(testDto));
        verify(labSlotRepo, never()).save(any(LabSlot.class));
    }

    @Test
    void createLabSlot_pastDate() {
        testDto = new CreateLabSlotDto(1L, LocalDate.now().minusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30));
        when(labTestRepo.findById(1L)).thenReturn(Optional.of(testLabTest));
        assertThrows(IllegalArgumentException.class, () -> sut.createLabSlot(testDto));
        verify(labSlotRepo, never()).save(any(LabSlot.class));
    }

    @Test
    void createLabSlot_invalidTime() {
        testDto = new CreateLabSlotDto(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(9, 0));
        when(labTestRepo.findById(1L)).thenReturn(Optional.of(testLabTest));
        assertThrows(IllegalArgumentException.class, () -> sut.createLabSlot(testDto));
        verify(labSlotRepo, never()).save(any(LabSlot.class));
    }

    @Test
    void createLabSlot_overlappingSlot() {
        when(labTestRepo.findById(1L)).thenReturn(Optional.of(testLabTest));
        when(labSlotRepo.existsOverlappingSlot(1L, testDto.workDate(), testDto.startTime(), testDto.endTime())).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createLabSlot(testDto));
        verify(labSlotRepo, never()).save(any(LabSlot.class));
    }

    @Test
    void getAvailableLabSlots_success() {
        when(labSlotRepo.findByStatus(LabSlotStatus.AVAILABLE)).thenReturn(List.of(testLabSlot));
        List<LabSlotResponseDto> result = sut.getAvailableLabSlots();
        verify(labSlotRepo, times(1)).findByStatus(LabSlotStatus.AVAILABLE);
        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().id());
        assertEquals("Общий анализ крови", result.getFirst().labTestName());
    }

}
