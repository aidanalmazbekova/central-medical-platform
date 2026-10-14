package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.model.dto.request.CreateSpecialtyDto;
import kg.megalab.cmp.model.dto.response.SpecialtyResponseDto;
import kg.megalab.cmp.model.entity.Specialty;
import kg.megalab.cmp.repo.SpecialtyRepo;
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
class SpecialtyServiceImplTest {

    @Mock
    private SpecialtyRepo specialtyRepo;

    @InjectMocks
    private SpecialtyServiceImpl sut;

    private CreateSpecialtyDto testDto;
    private Specialty testSpecialty;

    @BeforeEach
    void setUp() {
        testDto = new CreateSpecialtyDto("Кардиология", "Диагностика и лечение заболеваний сердца");
        testSpecialty = new Specialty();
        testSpecialty.setId(1L);
        testSpecialty.setName("Кардиология");
        testSpecialty.setDescription("Диагностика и лечение заболеваний сердца");
    }

    @Test
    void createSpecialty_success() {
        when(specialtyRepo.save(any(Specialty.class))).thenReturn(testSpecialty);
        SpecialtyResponseDto result = sut.createSpecialty(testDto);
        verify(specialtyRepo, times(1)).save(any(Specialty.class));
        assertEquals(1L, result.id());
        assertEquals("Кардиология", result.name());
        assertEquals("Диагностика и лечение заболеваний сердца", result.description());
    }

    @Test
    void createSpecialty_alreadyExists() {
        when(specialtyRepo.existsByNameIgnoreCase("Кардиология")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> sut.createSpecialty(testDto));
        verify(specialtyRepo, never()).save(any(Specialty.class));
    }

    @Test
    void getAllSpecialties_success() {
        when(specialtyRepo.findAll()).thenReturn(List.of(testSpecialty));
        List<SpecialtyResponseDto> result = sut.getAllSpecialties();
        verify(specialtyRepo, times(1)).findAll();
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals("Кардиология", result.getFirst().name());
    }

}
