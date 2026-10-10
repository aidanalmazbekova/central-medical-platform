package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.model.dto.request.CreateLabTestDto;
import kg.megalab.cmp.model.dto.response.LabTestResponseDto;
import kg.megalab.cmp.model.entity.LabTest;
import kg.megalab.cmp.repo.LabTestRepo;
import kg.megalab.cmp.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabTestServiceImpl implements LabTestService {

    private final LabTestRepo labTestRepo;

    @Override
    public LabTestResponseDto createLabTest(CreateLabTestDto dto) {
        String name = dto.name().trim();

        if (labTestRepo.existsByNameIgnoreCase(name)) {
            throw new AlreadyExistsException("Такой анализ уже существует");
        }

        LabTest labTest = new LabTest();
        labTest.setName(name);
        labTest.setDescription(dto.description());

        return toDto(labTestRepo.save(labTest));
    }

    @Override
    public List<LabTestResponseDto> getAllLabTests() {
        return labTestRepo.findAll().stream().map(this::toDto).toList();
    }

    private LabTestResponseDto toDto(LabTest labTest) {
        return new LabTestResponseDto(labTest.getId(), labTest.getName(), labTest.getDescription());
    }

}
