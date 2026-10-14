package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.model.dto.request.CreateLabTestDto;
import kg.megalab.cmp.model.dto.response.LabTestResponseDto;
import kg.megalab.cmp.model.entity.LabTest;
import kg.megalab.cmp.repo.LabTestRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabTestServiceImplTest {

    @Mock
    private LabTestRepo labTestRepo;

    @InjectMocks
    private LabTestServiceImpl sut;

    private CreateLabTestDto testDto;
    private LabTest testLabTest;

    @BeforeEach
    void setUp() {
        testDto = new CreateLabTestDto("Общий анализ крови", "Исследование показателей крови");
        testLabTest = new LabTest();
        testLabTest.setId(1L);
        testLabTest.setName("Общий анализ крови");
        testLabTest.setDescription("Исследование показателей крови");
    }

    @Test
    void createLabTest_success() {
        when(labTestRepo.save(any(LabTest.class))).thenReturn(testLabTest);
        LabTestResponseDto result = sut.createLabTest(testDto);
        verify(labTestRepo, times(1)).save(any(LabTest.class));
        assertEquals(1L, result.id());
        assertEquals("Общий анализ крови", result.name());
        assertEquals("Исследование показателей крови", result.description());
    }

    @Test
    void createLabTest_alreadyExists() {
        when(labTestRepo.existsByNameIgnoreCase("Общий анализ крови")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createLabTest(testDto));
        verify(labTestRepo, never()).save(any(LabTest.class));
    }

    @Test
    void getAllLabTests_success() {
        when(labTestRepo.findAll()).thenReturn(List.of(testLabTest));
        List<LabTestResponseDto> result = sut.getAllLabTests();
        verify(labTestRepo, times(1)).findAll();
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals("Общий анализ крови", result.getFirst().name());
    }

}
